package org.ohdsi.webapi.estimation.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;

public interface EstimationAnalysisGenerationRepository extends EntityGraphJpaRepository<EstimationGenerationEntity, Long> {
}
