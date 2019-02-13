package org.ohdsi.webapi.prediction;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;

public interface PredictionAnalysisRepository extends EntityGraphJpaRepository<PredictionAnalysis, Integer> {
    
}
