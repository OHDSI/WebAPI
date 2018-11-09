package org.ohdsi.webapi.feanalysis.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CRITERIA_SET_PREVALENCE")
public class FeAnalysisWithPrevalenceCriteriaEntity extends FeAnalysisWithCriteriaEntity<FeAnalysisCriteriaGroupEntity> {

  public FeAnalysisWithPrevalenceCriteriaEntity() {
  }

  public FeAnalysisWithPrevalenceCriteriaEntity(FeAnalysisWithCriteriaEntity analysis) {
    super(analysis);
  }
}
