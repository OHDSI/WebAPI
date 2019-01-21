package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import org.ohdsi.analysis.prediction.design.LassoLogisticRegressionSettings;

/**
 *
 * @author asena5
 */
public class LassoLogisticRegressionSettingsImpl extends SeedSettingsImpl implements LassoLogisticRegressionSettings {
  private BigDecimal variance = BigDecimal.valueOf(0.01);

  /**
   * A single value used as the starting value for the automatic lambda search 
   * @return variance
   **/
  @Override
  public BigDecimal getVariance() {
    return variance;
  }

    /**
     *
     * @param variance
     */
    public void setVariance(BigDecimal variance) {
    this.variance = variance;
  }
}
