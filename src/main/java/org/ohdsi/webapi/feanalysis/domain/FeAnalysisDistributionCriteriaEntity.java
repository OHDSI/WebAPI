package org.ohdsi.webapi.feanalysis.domain;

import jakarta.persistence.Entity;

@Entity
public abstract class FeAnalysisDistributionCriteriaEntity<T> extends FeAnalysisCriteriaEntity {
  public abstract T getExpression();
}
