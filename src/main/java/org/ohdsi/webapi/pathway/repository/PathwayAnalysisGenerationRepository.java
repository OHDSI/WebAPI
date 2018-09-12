package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGeneration;

public interface PathwayAnalysisGenerationRepository extends EntityGraphJpaRepository<PathwayAnalysisGeneration, Long> {
}
