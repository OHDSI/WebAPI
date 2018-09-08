package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PathwayService {

    PathwayAnalysisEntity create(PathwayAnalysisEntity pathwayAnalysisEntity);

    Page<PathwayAnalysisEntity> getPage(final Pageable pageable);

    PathwayAnalysisEntity getById(Long id);

    PathwayAnalysisEntity update(PathwayAnalysisEntity pathwayAnalysisEntity);

    void delete(Long id);
}
