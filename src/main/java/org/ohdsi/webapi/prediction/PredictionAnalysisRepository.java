package org.ohdsi.webapi.prediction;

import org.springframework.data.repository.CrudRepository;

public interface PredictionAnalysisRepository extends CrudRepository<PredictionAnalysis, Integer> {
    
}
