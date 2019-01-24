package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PathwayService {

    PathwayAnalysisEntity create(PathwayAnalysisEntity pathwayAnalysisEntity);

    PathwayAnalysisEntity importAnalysis(PathwayAnalysisEntity toImport);

    Page<PathwayAnalysisEntity> getPage(final Pageable pageable);

    PathwayAnalysisEntity getById(Integer id);

    PathwayAnalysisEntity update(PathwayAnalysisEntity pathwayAnalysisEntity);

    void delete(Integer id);

    Map<Integer, Integer> getEventCohortCodes(PathwayAnalysisEntity pathwayAnalysis);

    String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId);

    String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId, String cohortTable);

    void generatePathways(final Integer pathwayAnalysisId, final Integer sourceId);

    List<PathwayAnalysisGenerationEntity> getPathwayGenerations(final Integer pathwayAnalysisId);

    PathwayAnalysisGenerationEntity getGeneration(Long generationId);

    PathwayAnalysisResult getResultingPathways(final Long generationId);
}
