package org.ohdsi.webapi.feanalysis.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRITERIA_SET_PREVALENCE")
public class FeAnalysisWithPrevalenceCriteriaEntity extends FeAnalysisWithCriteriaEntity<FeAnalysisCriteriaGroupEntity> {

  public FeAnalysisWithPrevalenceCriteriaEntity() {
  }

  public FeAnalysisWithPrevalenceCriteriaEntity(FeAnalysisWithCriteriaEntity analysis) {
    super(analysis);
  }
}
