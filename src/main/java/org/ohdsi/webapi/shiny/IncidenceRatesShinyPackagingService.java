package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

@Service
@ConditionalOnBean(ShinyService.class)
public class IncidenceRatesShinyPackagingService extends CommonShinyPackagingService implements ShinyPackagingService {
    private static final Logger LOG = LoggerFactory.getLogger(IncidenceRatesShinyPackagingService.class);
    private static final String SHINY_INCIDENCE_RATES_APP_TEMPLATE_FILE_PATH = "/shiny/shiny-incidenceRates.zip";
    private static final String COHORT_TYPE_TARGET = "target";
    private static final String COHORT_TYPE_OUTCOME = "outcome";
    private static final String APP_NAME_FORMAT = "Incidence_%s_%s";
    private final IncidenceRateAnalysisRepository incidenceRateAnalysisRepository;
    private final IRAnalysisResource irAnalysisResource;

    @Autowired
    public IncidenceRatesShinyPackagingService(
            @Value("${shiny.atlas.url}") String atlasUrl,
            @Value("${shiny.repo.link}") String repoLink,
            FileWriter fileWriter,
            ManifestUtils manifestUtils,
            ObjectMapper objectMapper,
            IncidenceRateAnalysisRepository incidenceRateAnalysisRepository,
            IRAnalysisResource irAnalysisResource) {
        super(atlasUrl, repoLink, fileWriter, manifestUtils, objectMapper);
        this.incidenceRateAnalysisRepository = incidenceRateAnalysisRepository;
        this.irAnalysisResource = irAnalysisResource;
    }

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.INCIDENCE;
    }

    @Override
    public String getAppTemplateFilePath() {
        return SHINY_INCIDENCE_RATES_APP_TEMPLATE_FILE_PATH;
    }

    @Override
    public void populateAppData(Integer generationId, String sourceKey, ShinyAppDataConsumers dataConsumers) {
        IncidenceRateAnalysis analysis = incidenceRateAnalysisRepository.findOne(generationId);
        ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format("There is no incidence rate analysis with id = %d.", generationId));
        try {
            dataConsumers.getAppProperties().accept("atlas_link", String.format("%s/#/iranalysis/%s", atlasUrl, generationId));
            dataConsumers.getAppProperties().accept("analysis_name", analysis.getName());

            IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(analysis.getDetails().getExpression(), IncidenceRateAnalysisExportExpression.class);
            String csvWithCohortDetails = prepareCsvWithCohorts(expression);

            dataConsumers.getTextFiles().accept("cohorts.csv", csvWithCohortDetails);

            streamAnalysisReportsForAllCohortCombinations(expression, generationId, sourceKey)
                    .forEach(analysisReport ->
                            dataConsumers.getJsonObjects().accept(
                                    String.format("%s_targetId%s_outcomeId%s.json", sourceKey, analysisReport.summary.targetId, analysisReport.summary.outcomeId),
                                    analysisReport
                            )
                    );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
                .map(outcomeCohort -> {
                    AnalysisReport analysisReport = irAnalysisResource.getAnalysisReport(analysisId, sourceKey, targetCohortId, outcomeCohort.getId());
                    if (analysisReport.summary == null) {
                        analysisReport.summary = new AnalysisReport.Summary();
                        analysisReport.summary.targetId = targetCohortId;
                        analysisReport.summary.outcomeId = outcomeCohort.getId();
                    }
                    return analysisReport;
                });
    }

    @Override
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        IncidenceRateAnalysis analysis = incidenceRateAnalysisRepository.findOne(generationId);
        ApplicationBrief applicationBrief = new ApplicationBrief();
        applicationBrief.setName(String.format("%s_%s_%s", CommonAnalysisType.INCIDENCE.getCode(), generationId, sourceKey));
        applicationBrief.setTitle(prepareAppTitle(generationId, sourceKey));
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

    private String prepareAppTitle(Integer generationId, String sourceKey) {
        return String.format(APP_NAME_FORMAT, generationId, sourceKey);
    }
}
