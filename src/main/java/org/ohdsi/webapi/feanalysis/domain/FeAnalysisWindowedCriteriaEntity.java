package org.ohdsi.webapi.feanalysis.domain;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.cohortcharacterization.design.WindowedCriteriaFeature;
import org.ohdsi.circe.cohortdefinition.WindowedCriteria;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("WINDOWED_CRITERIA")
public class FeAnalysisWindowedCriteriaEntity extends FeAnalysisDistributionCriteriaEntity<WindowedCriteria> implements WindowedCriteriaFeature {

  @Override
  public WindowedCriteria getExpression() {

    return Utils.deserialize(this.getExpressionString(), WindowedCriteria.class);
  }

}
