package org.ohdsi.webapi.feanalysis.domain;

import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DEMOGRAPHIC_CRITERIA")
public class FeAnalysisDemographicCriteriaEntity extends FeAnalysisDistributionCriteriaEntity<DemographicCriteria> {

  @Override
  public DemographicCriteria getExpression() {

    return Utils.deserialize(this.getExpressionString(), DemographicCriteria.class);
  }
}
