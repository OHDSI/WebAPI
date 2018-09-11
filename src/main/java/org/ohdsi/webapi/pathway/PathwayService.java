package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PathwayService {

    PathwayAnalysisEntity create(PathwayAnalysisEntity pathwayAnalysisEntity);

    Page<PathwayAnalysisEntity> getPage(final Pageable pageable);

    PathwayAnalysisEntity getById(Integer id);

    PathwayAnalysisEntity update(PathwayAnalysisEntity pathwayAnalysisEntity);

    void delete(Integer id);

    Map<Integer, Integer> getEventCohortCodes(Integer pathwayAnalysisId);

    String buildAnalysisSql(Integer generationId, Integer pathwayAnalysisId, String sourceKey);

    PathwayAnalysisResult getResultingPathways(final Integer id);

    List<PathwayEventCohort> getEventCohortsByComboCode(PathwayAnalysisEntity pathwayAnalysis, Map<Integer, Integer> eventCodes, Integer comboCode);
}
