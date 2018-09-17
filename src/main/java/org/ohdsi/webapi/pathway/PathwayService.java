package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGeneration;
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

    void generatePathways(final Integer pathwayAnalysisId, final Integer sourceId);

    List<PathwayAnalysisGeneration> getPathwayGenerations(final Integer pathwayAnalysisId);

    PathwayAnalysisGeneration getGeneration(Long generationId);

    PathwayAnalysisResult getResultingPathways(final Long generationId);
}
