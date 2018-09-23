package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;

import java.util.List;

public interface PathwayAnalysisGenerationRepository extends EntityGraphJpaRepository<PathwayAnalysisGenerationEntity, Long> {

    List<PathwayAnalysisGenerationEntity> findAllByPathwayAnalysisId(Integer pathwayAnalysisId, EntityGraph entityGraph);
}
