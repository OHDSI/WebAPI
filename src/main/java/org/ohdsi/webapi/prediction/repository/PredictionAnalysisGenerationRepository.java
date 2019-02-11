package org.ohdsi.webapi.prediction.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;

public interface PredictionAnalysisGenerationRepository extends EntityGraphJpaRepository<PredictionGenerationEntity, Long> {
}
