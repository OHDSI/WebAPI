package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(ShinyService.class)
public class CohortPathwaysShinyPackagingService extends CommonShinyPackagingService implements ShinyPackagingService {
    private static final String SHINY_COHORT_PATHWAYS_APP_TEMPLATE_FILE_PATH = "/shiny/shiny-cohortPathways.zip";
    private static final String APP_TITLE_FORMAT = "Pathway_%s_gv%sx_%s";

    private final PathwayService pathwayService;

    @Autowired
    public CohortPathwaysShinyPackagingService(
            @Value("${shiny.atlas.url}") String atlasUrl,
            @Value("${shiny.repo.link}") String repoLink,
            FileWriter fileWriter,
            ManifestUtils manifestUtils,
            ObjectMapper objectMapper, PathwayService pathwayService) {
        super(atlasUrl, repoLink, fileWriter, manifestUtils, objectMapper);
        this.pathwayService = pathwayService;
    }

    @Override
    public CommonAnalysisType getType() {
        return CommonAnalysisType.COHORT_PATHWAY;
    }

    @Override
    public String getAppTemplateFilePath() {
        return SHINY_COHORT_PATHWAYS_APP_TEMPLATE_FILE_PATH;
    }

    @Override
    public void populateAppData(Integer generationId, String sourceKey, ShinyAppDataConsumers dataConsumers) {
        PathwayAnalysisDTO pathwayAnalysis = pathwayService.getByGenerationId(generationId);
        PathwayAnalysisResult pathwayAnalysisResult = pathwayService.getResultingPathways(generationId.longValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(pathwayAnalysis, String.format("There is no pathway analysis definition with generation id = %d.", generationId));
        ExceptionUtils.throwNotFoundExceptionIfNull(pathwayAnalysisResult, String.format("There is no pathway analysis result definition with generation id = %d.", generationId));

        dataConsumers.getJsonObjects().accept("pathwayAnalysis.json", pathwayAnalysis);
        dataConsumers.getJsonObjects().accept("pathwayAnalysisResult.json", pathwayAnalysisResult);
    }

    @Override
    public ApplicationBrief getBrief(Integer generationId, String sourceKey) {
        PathwayAnalysisDTO pathwayAnalysis = pathwayService.getByGenerationId(generationId);
        ApplicationBrief applicationBrief = new ApplicationBrief();
        applicationBrief.setName(String.format("%s_%s_%s", CommonAnalysisType.COHORT_PATHWAY.getCode(), generationId, sourceKey));
        applicationBrief.setTitle(prepareAppTitle(pathwayAnalysis.getId(), generationId, sourceKey));
        applicationBrief.setDescription(pathwayAnalysis.getDescription());
        return applicationBrief;
    }

    private String prepareAppTitle(Integer studyAssetId, Integer generationId, String sourceKey) {
        return String.format(APP_TITLE_FORMAT, studyAssetId, generationId, sourceKey);
    }
}
