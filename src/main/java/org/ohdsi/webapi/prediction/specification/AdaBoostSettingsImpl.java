package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.AdaBoostSettings;

/**
 * Specification for a Ada Boost Model 
 */
public class AdaBoostSettingsImpl extends SeedSettingsImpl implements AdaBoostSettings {
  private Integer nEstimators = 50;
  private Integer learningRate = 1;

  /**
   * The maximum number of estimators at which boosting is terminated 
   * @return nEstimators
   **/
  @Override
  public Integer getNEstimators() {
    return nEstimators;
  }

  public void setNEstimators(Integer nEstimators) {
    this.nEstimators = nEstimators;
  }

  /**
   * Learning rate shrinks the contribution of each classifier by learningRate. There is a trade-off between learningRate and nEstimators . 
   * @return learningRate
   **/
  @Override
  public Integer getLearningRate() {
    return learningRate;
  }

  public void setLearningRate(Integer learningRate) {
    this.learningRate = learningRate;
  }
}
