package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGeneration;

import java.util.List;

public interface PathwayAnalysisGenerationRepository extends EntityGraphJpaRepository<PathwayAnalysisGeneration, Long> {

    List<PathwayAnalysisGeneration> findAllByPathwayAnalysisId(Integer pathwayAnalysisId, EntityGraph entityGraph);
}
