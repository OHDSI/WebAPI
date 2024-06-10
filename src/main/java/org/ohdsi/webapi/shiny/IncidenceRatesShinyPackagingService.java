package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFilenameUtils;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExportExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.TempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.ws.rs.InternalServerErrorException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Service
@ConditionalOnBean(ShinyService.class)
public class IncidenceRatesShinyPackagingService implements ShinyPackagingService {

    private static final Logger LOG = LoggerFactory.getLogger(IncidenceRatesShinyPackagingService.class);

    private static final String SHINY_INCIDENCE_RATES_APP_PATH = "/shiny/shiny-incidenceRates.zip";
    private static final String COHORT_TYPE_TARGET = "target";
    private static final String COHORT_TYPE_OUTCOME = "outcome";


    @Autowired
    private FileWriter fileWriter;
    @Autowired
    private ManifestUtils manifestUtils;
    @Autowired
    private IncidenceRateAnalysisRepository incidenceRateAnalysisRepository;
    @Autowired
    private IRAnalysisResource irAnalysisResource;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${shiny.atlas.url}")
    private String atlasUrl;
    @Value("${shiny.repo.link}")
    private String repoLink;

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.INCIDENCE;
    }

    @Override
    public TemporaryFile packageApp(Integer analysisId, String sourceKey, PackagingStrategy packaging) {
        return TempFileUtils.doInDirectory(path -> {
            IncidenceRateAnalysis analysis = incidenceRateAnalysisRepository.findOne(analysisId);
            ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format("There is no incidence rate analysis with id = %d.", analysisId));
            try {
                File templateArchive = TempFileUtils.copyResourceToTempFile(SHINY_INCIDENCE_RATES_APP_PATH, "shiny", ".zip");
                CommonFileUtils.unzipFiles(templateArchive, path.toFile());
                Path manifestPath = path.resolve("manifest.json");
                if (!Files.exists(manifestPath)) {
                    throw new PositConnectClientException("manifest.json is not found in the Shiny Application");
                }
                JsonNode manifest = manifestUtils.parseManifest(manifestPath);
                Path dataDir = path.resolve("data");
                Files.createDirectory(dataDir);
                IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(
                        analysis.getDetails().getExpression(), IncidenceRateAnalysisExportExpression.class);
                String csvWithCohortDetails = prepareCsvWithCohorts(expression);

                Stream<Path> analysisReportPaths = streamAnalysisReportsForAllCohortCombinations(expression, analysisId, sourceKey)
                        .map(analysisReport -> fileWriter.writeObjectAsJsonFile(dataDir, analysisReport, String.format(
                                "%s_targetId%s_outcomeId%s.json", sourceKey, analysisReport.summary.targetId, analysisReport.summary.outcomeId)));

                Stream<Path> additionalMetadataFilesPaths = Stream.of(
                        fileWriter.writeTextFile(dataDir.resolve("cohorts.csv"), pw -> pw.print(csvWithCohortDetails)),
                        fileWriter.writeTextFile(dataDir.resolve("atlas_link.txt"), pw -> pw.printf("%s/#/iranalysis/%s", atlasUrl, analysisId)),
                        fileWriter.writeTextFile(dataDir.resolve("repo_link.txt"), pw -> pw.print(repoLink)),
                        fileWriter.writeTextFile(dataDir.resolve("datasource.txt"), pw -> pw.print(sourceKey))
                );

                Stream.concat(analysisReportPaths, additionalMetadataFilesPaths)
                        .forEach(manifestUtils.addDataToManifest(manifest, path));

                fileWriter.writeJsonNodeToFile(manifest, manifestPath);
                Path appArchive = packaging.apply(path);
                return new TemporaryFile(String.format("%s_%s_%s.zip", sourceKey, new SimpleDateFormat("yyyy_MM_dd").format(Date.from(Instant.now())),
                        CommonFilenameUtils.sanitizeFilename(analysis.getName())), appArchive);
            } catch (IOException e) {
                LOG.error("Failed to prepare Shiny application", e);
                throw new InternalServerErrorException();
            }
        });
    }

    private Stream<AnalysisReport> streamAnalysisReportsForAllCohortCombinations(IncidenceRateAnalysisExportExpression expression, Integer analysisId, String sourceKey) {
        List<CohortDTO> targetCohorts = expression.targetCohorts;
        List<CohortDTO> outcomeCohorts = expression.outcomeCohorts;
        return targetCohorts.stream()
                .map(CohortDTO::getId)
                .flatMap(targetCohortId -> streamAnalysisReportsForOneCohortCombination(targetCohortId, outcomeCohorts, analysisId, sourceKey));
    }

    private Stream<AnalysisReport> streamAnalysisReportsForOneCohortCombination(Integer targetCohortId, List<CohortDTO> outcomeCohorts, Integer analysisId, String sourceKey) {
        return outcomeCohorts.stream()
                .map(outcomeCohort -> irAnalysisResource.getAnalysisReport(analysisId, sourceKey, targetCohortId, outcomeCohort.getId()));
    }

    @Override
    public ApplicationBrief getBrief(Integer analysisId, String sourceKey) {
        IncidenceRateAnalysis analysis = incidenceRateAnalysisRepository.findOne(analysisId);
        ApplicationBrief applicationBrief = new ApplicationBrief();
        applicationBrief.setName(MessageFormat.format("incidence_rates_analysis_{0}_{1}", analysisId, sourceKey));
        applicationBrief.setTitle(analysis.getName());
        applicationBrief.setDescription(analysis.getDescription());
        return applicationBrief;
    }


    private String prepareCsvWithCohorts(IncidenceRateAnalysisExportExpression expression) {
        final String[] HEADER = {"cohort_id", "cohort_name", "type"};
        List<CohortDTO> targetCohorts = expression.targetCohorts;
        List<CohortDTO> outcomeCohorts = expression.outcomeCohorts;
        try (StringWriter stringWriter = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(stringWriter,
                     CSVFormat.Builder.create()
                             .setQuoteMode(QuoteMode.NON_NUMERIC)
                             .setHeader(HEADER)
                             .build())) {

            for (CohortDTO targetCohort : targetCohorts) {
                csvPrinter.printRecord(targetCohort.getId(), targetCohort.getName(), COHORT_TYPE_TARGET);
            }
            for (CohortDTO outcomeCohort : outcomeCohorts) {
                csvPrinter.printRecord(outcomeCohort.getId(), outcomeCohort.getName(), COHORT_TYPE_OUTCOME);
            }
            return stringWriter.toString();
        } catch (IOException e) {
            LOG.error("Failed to create a CSV file with Cohort details", e);
            throw new InternalServerErrorException();
        }
    }
}
