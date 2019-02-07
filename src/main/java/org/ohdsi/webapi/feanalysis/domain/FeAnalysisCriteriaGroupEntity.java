package org.ohdsi.webapi.feanalysis.domain;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.CriteriaFeature;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CRITERIA_GROUP")
public class FeAnalysisCriteriaGroupEntity extends FeAnalysisCriteriaEntity implements CriteriaFeature {

  @Override
  public CriteriaGroup getExpression() {
    return getCriteriaGroup();
  }

  private CriteriaGroup getCriteriaGroup() {
    return Utils.deserialize(this.getExpressionString(), CriteriaGroup.class);
  }

}
