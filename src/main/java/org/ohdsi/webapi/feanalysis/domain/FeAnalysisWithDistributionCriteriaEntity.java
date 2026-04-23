package org.ohdsi.webapi.feanalysis.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRITERIA_SET_DISTRIBUTION")
public class FeAnalysisWithDistributionCriteriaEntity extends FeAnalysisWithCriteriaEntity<FeAnalysisDistributionCriteriaEntity> {

  public FeAnalysisWithDistributionCriteriaEntity() {
  }

  public FeAnalysisWithDistributionCriteriaEntity(FeAnalysisWithCriteriaEntity analysis) {
    super(analysis);
  }
}
