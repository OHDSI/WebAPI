package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterables;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFilenameUtils;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.analysis.WithId;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.ExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.GenerationResults;
import org.ohdsi.webapi.cohortcharacterization.report.ExportItem;
import org.ohdsi.webapi.cohortcharacterization.report.Report;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.TempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.InternalServerErrorException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnBean(ShinyService.class)
public class CohortCharacterizationShinyPackagingService implements ShinyPackagingService {
    private static final Logger LOG = LoggerFactory.getLogger(CohortCharacterizationShinyPackagingService.class);
    private static final Float DEFAULT_THRESHOLD_VALUE = 0.01f;
    private static final String SHINY_COHORT_CHARACTERIZATIONS_APP_PATH = "/shiny/shiny-cohortCharacterizations.zip";
    @Value("${shiny.atlas.url}")
    private String atlasUrl;
    @Autowired
    private CcService ccService;
    @Autowired
    private FileWriter fileWriter;
    @Autowired
    private ManifestUtils manifestUtils;
    @Autowired
    private CohortCharacterizationAnalysisHeaderToFieldMapper cohortCharacterizationAnalysisHeaderToFieldMapper;

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.COHORT_CHARACTERIZATION;
    }

    @Override
    @Transactional
    public TemporaryFile packageApp(Integer generationId, String sourceKey, PackagingStrategy packaging) {
        return TempFileUtils.doInDirectory(path -> {
            CohortCharacterization cohortCharacterization = ccService.findDesignByGenerationId(Long.valueOf(generationId));
            GenerationResults generationResults = fetchGenerationResults(generationId, cohortCharacterization);
            ExceptionUtils.throwNotFoundExceptionIfNull(generationResults, String.format("There are no analysis generation results with generationId = %d.", generationId));
            try {
                File templateArchive = TempFileUtils.copyResourceToTempFile(SHINY_COHORT_CHARACTERIZATIONS_APP_PATH, "shiny", ".zip");
                CommonFileUtils.unzipFiles(templateArchive, path.toFile());
                Path manifestPath = path.resolve("manifest.json");
                if (!Files.exists(manifestPath)) {
                    throw new PositConnectClientException("manifest.json is not found in the Shiny Application");
                }
                JsonNode manifest = manifestUtils.parseManifest(manifestPath);
                Path dataDir = path.resolve("data");
                Files.createDirectory(dataDir);

                Stream<Path> generatedCsvPaths = generationResults.getReports()
                        .stream()
                        .map(this::convertReportToCSV)
                        .map(contentsByFilename -> fileWriter.writeTextFile(dataDir.resolve(contentsByFilename.getLeft()), pw -> pw.print(contentsByFilename.getRight())));

                Stream<Path> additionalMetadataFilesPaths = Stream.of(
                        fileWriter.writeTextFile(dataDir.resolve("atlas_link.txt"), pw -> pw.printf("%s/#/cc/characterizations/%s", atlasUrl, cohortCharacterization.getId()))
                );

                Stream.concat(generatedCsvPaths, additionalMetadataFilesPaths)
                        .forEach(manifestUtils.addDataToManifest(manifest, path));

                fileWriter.writeJsonNodeToFile(manifest, manifestPath);
                Path appArchive = packaging.apply(path);
                return new TemporaryFile(String.format("%s_%s_%s.zip", sourceKey, new SimpleDateFormat("yyyy_MM_dd").format(Date.from(Instant.now())),
                        CommonFilenameUtils.sanitizeFilename(cohortCharacterization.getName())), appArchive);
            } catch (IOException e) {
                LOG.error("Failed to prepare Shiny application", e);
                throw new InternalServerErrorException();
            }
        });
    }

    //Pair.left == CSV filename
    //Pair.right == CSV contents
    private Pair<String, String> convertReportToCSV(Report report) {
        boolean isComparativeAnalysis = report.isComparative;
        String analysisName = report.analysisName;
        String fileNameFormat = "Export %s(%s).csv";
        String fileName = String.format(fileNameFormat, isComparativeAnalysis ? "comparison " : "", analysisName);
        List<ExportItem> exportItems = report.items.stream()
                .sorted()
                .collect(Collectors.toList());

        String[] header = Iterables.getOnlyElement(report.header);

        String outCsv = prepareCsv(header, exportItems);
        return Pair.of(fileName, outCsv);
    }

    private String prepareCsv(String[] headers, List<ExportItem> exportItems) {
        try (StringWriter stringWriter = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(stringWriter,
                     CSVFormat.Builder
                             .create()
                             .setQuoteMode(QuoteMode.NON_NUMERIC)
                             .setHeader(headers)
                             .build())) {
            for (ExportItem<?> item : exportItems) {
                List<String> record = new ArrayList<>();
                for (String header : headers) {
                    String fieldName = cohortCharacterizationAnalysisHeaderToFieldMapper.getHeaderFieldMapping().get(header); // get the corresponding Java field name
                    Field field;
                    try {
                        if (fieldName != null) {
                            field = findFieldInClassHierarchy(item.getClass(), fieldName);
                            if (field != null) {
                                field.setAccessible(true);
                                record.add(String.valueOf(field.get(item)));
                            } else {
                                record.add(null);
                            }
                        }
                    } catch (IllegalAccessException ex) {
                        LOG.error("Error occurred while accessing field value", ex);
                        record.add("");
                    }
                }
                csvPrinter.printRecord(record);
            }
            return stringWriter.toString();
        } catch (IOException e) {
            LOG.error("Failed to create a CSV file with Cohort Characterization analysis details", e);
            throw new InternalServerErrorException();
        }
    }

    private Field findFieldInClassHierarchy(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            field = findFieldInClassHierarchy(clazz.getSuperclass(), fieldName);
        }
        return field;
    }

    private GenerationResults fetchGenerationResults(Integer generationId, CohortCharacterization cohortCharacterization) {
        ExecutionResultRequest executionResultRequest = new ExecutionResultRequest();
        List<Integer> cohortIds = cohortCharacterization.getCohorts()
                .stream()
                .map(CohortMetadata::getId)
                .collect(Collectors.toList());
        List<Integer> analysisIds = cohortCharacterization.getFeatureAnalyses()
                .stream()
                .map(WithId::getId)
                .map(Number::intValue)
                .collect(Collectors.toList());
        List<String> domainIds = cohortCharacterization.getFeatureAnalyses()
                .stream()
                .map(featureAnalysis -> featureAnalysis.getDomain().getName().toUpperCase())
                .distinct()
                .collect(Collectors.toList());
        executionResultRequest.setAnalysisIds(analysisIds);
        executionResultRequest.setCohortIds(cohortIds);
        executionResultRequest.setDomainIds(domainIds);
        executionResultRequest.setShowEmptyResults(Boolean.TRUE);
        executionResultRequest.setThresholdValuePct(DEFAULT_THRESHOLD_VALUE);
        return ccService.findResult(Long.valueOf(generationId), executionResultRequest);
    }

    @Override
    @Transactional
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        CohortCharacterization cohortCharacterization = ccService.findDesignByGenerationId(Long.valueOf(generationId));
        CohortCharacterizationEntity cohortCharacterizationEntity = ccService.findById(cohortCharacterization.getId());
        ApplicationBrief applicationBrief = new ApplicationBrief();
        applicationBrief.setName(MessageFormat.format("cohort_characterization_analysis_{0}_{1}", generationId, sourceKey));
        applicationBrief.setTitle(String.format("%s (%s)", cohortCharacterizationEntity.getName(), sourceKey));
        applicationBrief.setDescription(cohortCharacterizationEntity.getDescription());
        return applicationBrief;
    }
}
