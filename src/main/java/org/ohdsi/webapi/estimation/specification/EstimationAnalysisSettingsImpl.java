package org.ohdsi.webapi.estimation.specification;

import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.analysis.estimation.design.EstimationAnalysisSettings;

public class EstimationAnalysisSettingsImpl implements EstimationAnalysisSettings {
  private EstimationTypeEnum estimationType = EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
  private Object analysisSpecification = null;

  /**
   * The type of estimation analysis to execute 
   * @return estimationType
   **/
  @Override
  public EstimationTypeEnum getEstimationType() {
    return estimationType;
  }

  public void setEstimationType(EstimationTypeEnum estimationType) {
    this.estimationType = estimationType;
  }

  /**
   * Get analysisSpecification
   * @return analysisSpecification
   **/
  @Override
  public Object getAnalysisSpecification() {
    return analysisSpecification;
  }

  public void setAnalysisSpecification(Object analysisSpecification) {
    this.analysisSpecification = analysisSpecification;
  }
}
