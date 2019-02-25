package org.ohdsi.webapi.prediction.domain;

import org.ohdsi.webapi.common.generation.CommonGeneration;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.prediction.PredictionAnalysis;

import javax.persistence.*;

@Entity
@Table(name = "prediction_analysis_generation")
public class PredictionGenerationEntity extends CommonGeneration {

  @ManyToOne(targetEntity = PredictionAnalysis.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "prediction_id")
  private PredictionAnalysis predictionAnalysis;

  @OneToOne(targetEntity = ExecutionEngineAnalysisStatus.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "analysis_execution_id")
  private ExecutionEngineAnalysisStatus analysisExecution;

  @Column(name = "update_password")
  private String updatePassword;

  public PredictionAnalysis getPredictionAnalysis() {
    return predictionAnalysis;
  }

  public ExecutionEngineAnalysisStatus getAnalysisExecution() {
    return analysisExecution;
  }

  public String getUpdatePassword() {
    return updatePassword;
  }

  public void setUpdatePassword(String updatePassword) {
    this.updatePassword = updatePassword;
  }

  @Override
  public PredictionAnalysis getDesign() {
    return null;
  }
}
