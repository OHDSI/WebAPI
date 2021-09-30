package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.shiro.annotations.PathwayAnalysisGenerationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PathwayService {

    PathwayAnalysisEntity create(PathwayAnalysisEntity pathwayAnalysisEntity);

    PathwayAnalysisEntity importAnalysis(PathwayAnalysisEntity toImport);

    String getNameForCopy(String dtoName);
    
    String getNameWithSuffix(String dtoName);

    Page<PathwayAnalysisEntity> getPage(final Pageable pageable);

    int getCountPAWithSameName(Integer id, String name);

    PathwayAnalysisEntity getById(Integer id);

    PathwayAnalysisEntity update(PathwayAnalysisEntity pathwayAnalysisEntity);

    void delete(Integer id);

    Map<Integer, Integer> getEventCohortCodes(PathwayAnalysisEntity pathwayAnalysis);

    String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId);

    String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId, String cohortTable, String sessionId);

    JobExecutionResource generatePathways(final Integer pathwayAnalysisId, final Integer sourceId);

    List<PathwayAnalysisGenerationEntity> getPathwayGenerations(final Integer pathwayAnalysisId);

    PathwayAnalysisGenerationEntity getGeneration(Long generationId);

    PathwayAnalysisResult getResultingPathways(final Long generationId);

    void cancelGeneration(Integer pathwayAnalysisId, Integer sourceId);

    String findDesignByGenerationId(@PathwayAnalysisGenerationId final Long id);
}
