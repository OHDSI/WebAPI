package org.ohdsi.webapi.shiny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.ohdsi.webapi.pathway.dto.PathwayPopulationResultsDTO;
import org.ohdsi.webapi.pathway.dto.TargetCohortPathwaysDTO;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.ShinyService;
import org.ohdsi.webapi.shiny.summary.DataSourceSummaryConverter;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
            ObjectMapper objectMapper, PathwayService pathwayService,
            SourceRepository sourceRepository,
            CDMResultsService cdmResultsService,
            DataSourceSummaryConverter dataSourceSummaryConverter) {
        super(atlasUrl, repoLink, fileWriter, manifestUtils, objectMapper, sourceRepository, cdmResultsService, dataSourceSummaryConverter);
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
        String designJSON = pathwayService.findDesignByGenerationId(generationId.longValue());
        PathwayPopulationResultsDTO generationResults = pathwayService.getGenerationResults(generationId.longValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(generationResults, String.format("There are no pathway analysis generation results with generation id = %d.", generationId));
        ExceptionUtils.throwNotFoundExceptionIfNull(designJSON, String.format("There is no pathway analysis design with generation id = %d.", generationId));
        dataConsumers.getTextFiles().accept("design.json", designJSON);
        dataConsumers.getJsonObjects().accept("chartData.json", generationResults);

        PathwayAnalysisDTO pathwayAnalysisDTO = pathwayService.getByGenerationId(generationId);
        PathwayAnalysisGenerationEntity generationEntity = pathwayService.getGeneration(generationId.longValue());

        int totalCount = generationResults.getPathwayGroups().stream().mapToInt(TargetCohortPathwaysDTO::getTargetCohortCount).sum();
        int personCount = generationResults.getPathwayGroups().stream().mapToInt(TargetCohortPathwaysDTO::getTotalPathwaysCount).sum();

        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_AUTHOR.getValue(), getAuthor(pathwayAnalysisDTO));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_ASSET_NAME.getValue(), pathwayAnalysisDTO.getName());
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_ASSET_ID.getValue(), pathwayAnalysisDTO.getId().toString());
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_GENERATED_DATE.getValue(), getGenerationStartTime(generationEntity));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_RECORD_COUNT.getValue(), Integer.toString(totalCount));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_PERSON_COUNT.getValue(), Integer.toString(personCount));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_AUTHOR_NOTES.getValue(), getDescription(pathwayAnalysisDTO));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_REFERENCED_COHORTS.getValue(), prepareReferencedCohorts(pathwayAnalysisDTO));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_VERSION_ID.getValue(), Integer.toString(generationId));
        dataConsumers.getAppProperties().accept(ShinyConstants.PROPERTY_NAME_GENERATION_ID.getValue(), Integer.toString(generationId));
    }

    private String getAuthor(PathwayAnalysisDTO pathwayAnalysisDTO) {
        if (pathwayAnalysisDTO.getCreatedBy() != null) {
            return pathwayAnalysisDTO.getCreatedBy().getLogin();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }

    private String getDescription(PathwayAnalysisDTO pathwayAnalysisDTO) {
        if (pathwayAnalysisDTO != null && pathwayAnalysisDTO.getDescription() != null) {
            return pathwayAnalysisDTO.getDescription();
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
    }


    private String prepareReferencedCohorts(PathwayAnalysisDTO pathwayAnalysisDTO) {
        if (pathwayAnalysisDTO == null) {
            return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
        }
        Set<String> referencedCohortNames = new HashSet<>();
        for (PathwayCohortDTO eventCohort : pathwayAnalysisDTO.getEventCohorts()) {
            referencedCohortNames.add(eventCohort.getName());
        }
        for (PathwayCohortDTO targetCohort : pathwayAnalysisDTO.getTargetCohorts()) {
            referencedCohortNames.add(targetCohort.getName());
        }
        return String.join("; ", referencedCohortNames);
    }

    private String getGenerationStartTime(PathwayAnalysisGenerationEntity generationEntity) {
        if (generationEntity != null) {
            return dateToString(generationEntity.getStartTime());
        }
        return ShinyConstants.VALUE_NOT_AVAILABLE.getValue();
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
