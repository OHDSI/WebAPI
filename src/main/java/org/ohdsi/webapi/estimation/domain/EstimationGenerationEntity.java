package org.ohdsi.webapi.estimation.domain;

import org.ohdsi.webapi.common.generation.CommonGeneration;
import org.ohdsi.webapi.estimation.Estimation;

import javax.persistence.*;

@Entity
@Table(name = "estimation_analysis_generation")
public class EstimationGenerationEntity extends CommonGeneration {

  @ManyToOne(targetEntity = Estimation.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "estimation_id")
  private Estimation estimationAnalysis;

  public Estimation getEstimationAnalysis() {
    return estimationAnalysis;
  }

  @Override
  public <T> T getDesign() {
    return null;
  }
}
