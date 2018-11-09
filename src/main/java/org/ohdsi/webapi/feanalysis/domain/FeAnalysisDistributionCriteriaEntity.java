package org.ohdsi.webapi.feanalysis.domain;

import javax.persistence.Entity;

@Entity
public abstract class FeAnalysisDistributionCriteriaEntity<T> extends FeAnalysisCriteriaEntity {
  public abstract T getExpression();
}
