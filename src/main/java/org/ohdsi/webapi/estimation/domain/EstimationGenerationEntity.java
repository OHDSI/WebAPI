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

  @Column(name = "update_password")
  private String updatePassword;

  public Estimation getEstimationAnalysis() {
    return estimationAnalysis;
  }

  public String getUpdatePassword() {
    return updatePassword;
  }

  public void setUpdatePassword(String updatePassword) {
    this.updatePassword = updatePassword;
  }

  @Override
  public <T> T getDesign() {
    return null;
  }
}
