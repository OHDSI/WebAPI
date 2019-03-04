package org.ohdsi.webapi.prediction.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.prediction.PredictionAnalysis;

public interface PredictionAnalysisRepository extends EntityGraphJpaRepository<PredictionAnalysis, Integer> {
    int countByNameStartsWith(String pattern);   
}