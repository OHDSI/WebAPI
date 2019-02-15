package org.ohdsi.webapi.prediction.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;

import java.util.List;

public interface PredictionAnalysisGenerationRepository extends EntityGraphJpaRepository<PredictionGenerationEntity, Long> {

  List<PredictionGenerationEntity> findByPredictionAnalysisId(Integer predictionAnalysisId, EntityGraph entityGraph);
}
