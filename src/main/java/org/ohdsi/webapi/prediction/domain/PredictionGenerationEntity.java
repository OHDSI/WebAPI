package org.ohdsi.webapi.prediction.domain;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;
import org.ohdsi.webapi.prediction.PredictionAnalysis;

import jakarta.persistence.*;

@Entity
@Table(name = "prediction_analysis_generation")
public class PredictionGenerationEntity extends ExecutionEngineGenerationEntity {

  @ManyToOne(targetEntity = PredictionAnalysis.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "prediction_id")
  private PredictionAnalysis predictionAnalysis;
}
