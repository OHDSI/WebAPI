package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoId;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.shiny.summary.DataSourceSummaryConverter;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(ShinyService.class)
public class CohortCountsShinyPackagingService extends CommonShinyPackagingService implements ShinyPackagingService {
    private static final String SHINY_COHORT_COUNTS_APP_TEMPLATE_FILE_PATH = "/shiny/shiny-cohortCounts.zip";
    private static final String APP_TITLE_FORMAT = "Cohort_%s_gv%sx%s_%s";
    private final CohortDefinitionService cohortDefinitionService;
    private final CohortDefinitionRepository cohortDefinitionRepository;
    private final CohortGenerationInfoRepository cohortGenerationInfoRepository;

    @Autowired
    public CohortCountsShinyPackagingService(
            @Value("${shiny.atlas.url}") String atlasUrl,
            @Value("${shiny.repo.link}") String repoLink,
            FileWriter fileWriter,
            ManifestUtils manifestUtils,
            ObjectMapper objectMapper,
            CohortDefinitionService cohortDefinitionService,
            CohortDefinitionRepository cohortDefinitionRepository,
            SourceRepository sourceRepository,
            CohortGenerationInfoRepository cohortGenerationInfoRepository,
            CDMResultsService cdmResultsService,
            DataSourceSummaryConverter dataSourceSummaryConverter
    ) {
        super(atlasUrl, repoLink, fileWriter, manifestUtils, objectMapper, sourceRepository, cdmResultsService, dataSourceSummaryConverter);
        this.cohortDefinitionService = cohortDefinitionService;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
        this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
    }

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.COHORT;
    }

    @Override
    public String getAppTemplateFilePath() {
        return SHINY_COHORT_COUNTS_APP_TEMPLATE_FILE_PATH;
    }

    @Override
    @Transactional
    public void populateAppData(Integer generationId, String sourceKey, ShinyAppDataConsumers dataConsumers) {
        CohortDefinition cohort = cohortDefinitionRepository.findOne(generationId);
        ExceptionUtils.throwNotFoundExceptionIfNull(cohort, String.format("There is no cohort definition with id = %d.", generationId));

        int sourceId = getSourceRepository().findBySourceKey(sourceKey).getId();
        CohortGenerationInfo cohortGenerationInfo = cohortGenerationInfoRepository.findOne(new CohortGenerationInfoId(cohort.getId(), sourceId));

        CohortExpression cohortExpression = cohort.getExpression();

        String cohortSummaryAsMarkdown = cohortDefinitionService.convertCohortExpressionToMarkdown(cohortExpression);

        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_COHORT_LINK.getValue(), String.format("%s/#/cohortdefinition/%s", atlasUrl, generationId));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_COHORT_NAME.getValue(), cohort.getName());
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_AUTHOR.getValue(), getAuthor(cohort));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_ASSET_ID.getValue(), cohort.getId().toString());
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_GENERATED_DATE.getValue(), getGenerationStartTime(cohortGenerationInfo));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_RECORD_COUNT.getValue(), getRecordCount(cohortGenerationInfo));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_PERSON_COUNT.getValue(), getPersonCount(cohortGenerationInfo));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_AUTHOR_NOTES.getValue(), getDescription(cohort));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_REFERENCED_COHORTS.getValue(), cohort.getName());
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_VERSION_ID.getValue(), getGenerationId(cohortGenerationInfo.getId()));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_GENERATION_ID.getValue(), getGenerationId(cohortGenerationInfo.getId()));

        dataConsumers.getTextFiles().accept("cohort_summary_markdown.txt", cohortSummaryAsMarkdown);

        InclusionRuleReport byEventReport = cohortDefinitionService.getInclusionRuleReport(generationId, sourceKey, 0, null); //by event
        InclusionRuleReport byPersonReport = cohortDefinitionService.getInclusionRuleReport(generationId, sourceKey, 1, null); //by person

        dataConsumers.getJsonObjects().accept(sourceKey + "_by_event.json", byEventReport);
        dataConsumers.getJsonObjects().accept(sourceKey + "_by_person.json", byPersonReport);
    }

    private String getGenerationId(CohortGenerationInfoId id) {
        return id == null ? "" : Integer.toString(id.getCohortDefinitionId()).concat("x").concat(Integer.toString(id.getSourceId()));
    }

    private String getDescription(CohortDefinition cohort) {
        if (cohort != null && cohort.getDescription() != null) {
            return escapeLineBreaks(cohort.getDescription());
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getPersonCount(CohortGenerationInfo cohortGenerationInfo) {
        if (cohortGenerationInfo != null && cohortGenerationInfo.getPersonCount() != null) {
            return cohortGenerationInfo.getPersonCount().toString();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getRecordCount(CohortGenerationInfo cohortGenerationInfo) {
        if (cohortGenerationInfo != null && cohortGenerationInfo.getRecordCount() != null) {
            return cohortGenerationInfo.getRecordCount().toString();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getGenerationStartTime(CohortGenerationInfo cohortGenerationInfo) {
        if (cohortGenerationInfo != null && cohortGenerationInfo.getStartTime() != null) {
            return dateToString(cohortGenerationInfo.getStartTime());
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getAuthor(CohortDefinition cohort) {
        if (cohort.getCreatedBy() != null) {
            return cohort.getCreatedBy().getLogin();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    @Override
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        CohortDefinition cohort = cohortDefinitionRepository.findOne(generationId);
        Integer assetId = cohort.getId();
        Integer sourceId = sourceRepository.findBySourceKey(sourceKey).getSourceId();

        ApplicationBrief brief = new ApplicationBrief();
        brief.setName(String.format("%s_%s_%s", CommonAnalysisType.COHORT.getCode(), generationId, sourceKey));
        brief.setTitle(prepareAppTitle(generationId, assetId, sourceId, sourceKey));
        brief.setDescription(cohort.getDescription());
        return brief;
    }

    private String prepareAppTitle(Integer generationId, Integer assetId, Integer sourceId, String sourceKey) {
        return String.format(APP_TITLE_FORMAT, generationId, assetId, sourceId, sourceKey);
    }
}
