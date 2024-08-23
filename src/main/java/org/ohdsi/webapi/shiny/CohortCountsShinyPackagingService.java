package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
@ConditionalOnBean(ShinyService.class)
public class CohortCountsShinyPackagingService extends CommonShinyPackagingService implements ShinyPackagingService {
    private static final String SHINY_COHORT_COUNTS_APP_TEMPLATE_FILE_PATH = "/shiny/shiny-cohortCounts.zip";
    private static final String APP_TITLE_FORMAT = "Cohort_%s_%s";
    private final CohortDefinitionService cohortDefinitionService;
    private final CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    public CohortCountsShinyPackagingService(
            @Value("${shiny.atlas.url}") String atlasUrl,
            @Value("${shiny.repo.link}") String repoLink,
            FileWriter fileWriter,
            ManifestUtils manifestUtils,
            ObjectMapper objectMapper, CohortDefinitionService cohortDefinitionService, CohortDefinitionRepository cohortDefinitionRepository) {
        super(atlasUrl, repoLink, fileWriter, manifestUtils, objectMapper);
        this.cohortDefinitionService = cohortDefinitionService;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
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
    public void populateAppData(Integer generationId, String sourceKey, ShinyAppDataConsumers dataConsumers) {
        CohortDefinition cohort = cohortDefinitionRepository.findOne(generationId);
        ExceptionUtils.throwNotFoundExceptionIfNull(cohort, String.format("There is no cohort definition with id = %d.", generationId));

        dataConsumers.getAppProperties().accept("cohort_link", String.format("%s/#/cohortdefinition/%s", atlasUrl, generationId));
        dataConsumers.getAppProperties().accept("cohort_name", cohort.getName());

        InclusionRuleReport byEventReport = cohortDefinitionService.getInclusionRuleReport(generationId, sourceKey, 0); //by event
        InclusionRuleReport byPersonReport = cohortDefinitionService.getInclusionRuleReport(generationId, sourceKey, 1); //by person

        dataConsumers.getJsonObjects().accept(sourceKey + "_by_event.json", byEventReport);
        dataConsumers.getJsonObjects().accept(sourceKey + "_by_person.json", byPersonReport);
    }

    @Override
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        CohortDefinition cohort = cohortDefinitionRepository.findOne(generationId);
        ApplicationBrief brief = new ApplicationBrief();
        brief.setName(MessageFormat.format("cohort_{0}_{1}", cohort.getId(), sourceKey));
        brief.setTitle(prepareAppTitle(generationId, sourceKey));
        brief.setDescription(cohort.getDescription());
        return brief;
    }

    private String prepareAppTitle(Integer generationId, String sourceKey) {
        return String.format(APP_TITLE_FORMAT, generationId, sourceKey);
    }
}
