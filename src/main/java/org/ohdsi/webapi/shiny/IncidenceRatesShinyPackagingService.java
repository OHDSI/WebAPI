package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExportExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.service.dto.AnalysisInfoDTO;
import org.ohdsi.webapi.shiny.summary.DataSourceSummaryConverter;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.InternalServerErrorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@ConditionalOnBean(ShinyService.class)
public class IncidenceRatesShinyPackagingService extends CommonShinyPackagingService implements ShinyPackagingService {
    private static final Logger LOG = LoggerFactory.getLogger(IncidenceRatesShinyPackagingService.class);
    private static final String SHINY_INCIDENCE_RATES_APP_TEMPLATE_FILE_PATH = "/shiny/shiny-incidenceRates.zip";
    private static final String COHORT_TYPE_TARGET = "target";
    private static final String COHORT_TYPE_OUTCOME = "outcome";
    private static final String APP_NAME_FORMAT = "Incidence_%s_gv%sx%s_%s";
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
            IRAnalysisResource irAnalysisResource,
            SourceRepository sourceRepository,
            CDMResultsService cdmResultsService,
            DataSourceSummaryConverter dataSourceSummaryConverter) {
        super(atlasUrl, repoLink, fileWriter, manifestUtils, objectMapper, sourceRepository, cdmResultsService, dataSourceSummaryConverter);
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
    @Transactional
    public void populateAppData(Integer generationId, String sourceKey, ShinyAppDataConsumers dataConsumers) {
        IncidenceRateAnalysis analysis = incidenceRateAnalysisRepository.findOne(generationId);
        ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format("There is no incidence rate analysis with id = %d.", generationId));
        try {
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_ATLAS_LINK.getValue(), String.format("%s/#/iranalysis/%s", atlasUrl, generationId));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_ANALYSIS_NAME.getValue(), analysis.getName());

            IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(analysis.getDetails().getExpression(), IncidenceRateAnalysisExportExpression.class);
            AnalysisInfoDTO analysisInfoDTO = irAnalysisResource.getAnalysisInfo(analysis.getId(), sourceKey);

            Integer assetId = analysis.getId();
            Integer sourceId = sourceRepository.findBySourceKey(sourceKey).getSourceId();

            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_AUTHOR.getValue(), getAuthor(analysis));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_ASSET_ID.getValue(), analysis.getId().toString());
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_GENERATED_DATE.getValue(), getGenerationStartTime(analysis));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_RECORD_COUNT.getValue(), getRecordCount(analysisInfoDTO));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_PERSON_COUNT.getValue(), getPersonCount(analysisInfoDTO));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_AUTHOR_NOTES.getValue(), getDescription(analysis));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_REFERENCED_COHORTS.getValue(), prepareReferencedCohorts(expression));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_VERSION_ID.getValue(), getGenerationId(assetId, sourceId));
            dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_GENERATION_ID.getValue(), getGenerationId(assetId, sourceId));

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

    private String getAuthor(IncidenceRateAnalysis analysis) {
        if (analysis.getCreatedBy() != null) {
            return analysis.getCreatedBy().getLogin();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getGenerationStartTime(IncidenceRateAnalysis analysis) {
        if (analysis != null) {
            if (CollectionUtils.isNotEmpty(analysis.getExecutionInfoList())) {
                return dateToString(Iterables.getLast(analysis.getExecutionInfoList()).getStartTime());
            }
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getDescription(IncidenceRateAnalysis analysis) {
        if (analysis != null && analysis.getDescription() != null) {
            return analysis.getDescription();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getPersonCount(AnalysisInfoDTO analysisInfo) {
        if (analysisInfo != null && CollectionUtils.isNotEmpty(analysisInfo.getSummaryList())) {
            return Long.toString(Iterables.getLast(analysisInfo.getSummaryList()).cases);
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getRecordCount(AnalysisInfoDTO analysisInfo) {
        if (analysisInfo != null && CollectionUtils.isNotEmpty(analysisInfo.getSummaryList())) {
            return Long.toString(Iterables.getLast(analysisInfo.getSummaryList()).totalPersons);
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getGenerationId(Integer assetId, Integer sourceId) {
        return assetId == null || sourceId == null ? "" : Integer.toString(assetId).concat("x").concat(Integer.toString(sourceId));
    }

    private String prepareReferencedCohorts(IncidenceRateAnalysisExportExpression expression) {
        if (expression == null) {
            return "";
        }
        Set<String> referencedCohortNames = new HashSet<>();
        for (CohortDTO targetCohort : expression.targetCohorts) {
            referencedCohortNames.add(targetCohort.getName());
        }
        for (CohortDTO outcomeCohort : expression.outcomeCohorts) {
            referencedCohortNames.add(outcomeCohort.getName());
        }
        return String.join("; ", referencedCohortNames);
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
    @Transactional
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        IncidenceRateAnalysis analysis = incidenceRateAnalysisRepository.findOne(generationId);
        Integer assetId = analysis.getId();
        Integer sourceId = sourceRepository.findBySourceKey(sourceKey).getSourceId();
        ApplicationBrief applicationBrief = new ApplicationBrief();
        applicationBrief.setName(String.format("%s_%s_%s", CommonAnalysisType.INCIDENCE.getCode(), generationId, sourceKey));
        applicationBrief.setTitle(prepareAppTitle(generationId, assetId, sourceId, sourceKey));
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

    private String prepareAppTitle(Integer generationId, Integer assetId, Integer sourceId, String sourceKey) {
        return String.format(APP_NAME_FORMAT, generationId, assetId, sourceId, sourceKey);
    }
}
