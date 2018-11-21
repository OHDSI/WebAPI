package org.ohdsi.webapi.feanalysis.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CRITERIA_SET_DISTRIBUTION")
public class FeAnalysisWithDistributionCriteriaEntity extends FeAnalysisWithCriteriaEntity<FeAnalysisDistributionCriteriaEntity> {

  public FeAnalysisWithDistributionCriteriaEntity() {
  }

  public FeAnalysisWithDistributionCriteriaEntity(FeAnalysisWithCriteriaEntity analysis) {
    super(analysis);
  }
}
