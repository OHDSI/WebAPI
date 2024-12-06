package org.ohdsi.webapi.executionengine.entity;

import org.ohdsi.webapi.common.generation.CommonGeneration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ExecutionEngineGenerationEntity extends CommonGeneration {

  @Column(name = "update_password")
  private String updatePassword;

  @OneToOne(targetEntity = ExecutionEngineAnalysisStatus.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "analysis_execution_id")
  private ExecutionEngineAnalysisStatus analysisExecution;

  public String getUpdatePassword() {
    return updatePassword;
  }

  public void setUpdatePassword(String updatePassword) {
    this.updatePassword = updatePassword;
  }

  public ExecutionEngineAnalysisStatus getAnalysisExecution() {
    return analysisExecution;
  }

}
