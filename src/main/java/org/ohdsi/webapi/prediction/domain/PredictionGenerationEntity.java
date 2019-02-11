package org.ohdsi.webapi.prediction.domain;

import org.ohdsi.webapi.common.generation.CommonGeneration;
import org.ohdsi.webapi.prediction.PredictionAnalysis;

import javax.persistence.*;

@Entity
@Table(name = "prediction_analysis_generation")
public class PredictionGenerationEntity extends CommonGeneration {

  @ManyToOne(targetEntity = PredictionAnalysis.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "prediction_id")
  private PredictionAnalysis predictionAnalysis;

  public PredictionAnalysis getPredictionAnalysis() {
    return predictionAnalysis;
  }

  @Override
  public PredictionAnalysis getDesign() {
    return null;
  }
}
