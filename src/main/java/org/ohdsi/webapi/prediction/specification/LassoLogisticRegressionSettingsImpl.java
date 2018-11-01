package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.LassoLogisticRegressionSettings;

public class LassoLogisticRegressionSettingsImpl extends SeedSettingsImpl implements LassoLogisticRegressionSettings {
  private Float variance = 0.01f;

  /**
   * A single value used as the starting value for the automatic lambda search 
   * @return variance
   **/
  @Override
  public Float getVariance() {
    return variance;
  }

  public void setVariance(Float variance) {
    this.variance = variance;
  }
}
