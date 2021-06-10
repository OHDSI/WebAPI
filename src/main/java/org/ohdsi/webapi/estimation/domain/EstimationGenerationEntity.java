package org.ohdsi.webapi.estimation.domain;

import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;

import javax.persistence.*;

@Entity
@Table(name = "estimation_analysis_generation")
public class EstimationGenerationEntity extends ExecutionEngineGenerationEntity {

  @ManyToOne(targetEntity = Estimation.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "estimation_id")
  private Estimation estimationAnalysis;

  public Estimation getEstimationAnalysis() {
    return estimationAnalysis;
  }
}
