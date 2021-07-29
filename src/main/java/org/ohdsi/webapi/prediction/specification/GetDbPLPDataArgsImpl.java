package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import org.ohdsi.analysis.prediction.design.GetDbPLPDataArgs;

/**
 *
 * @author asena5
 */
public class GetDbPLPDataArgsImpl implements GetDbPLPDataArgs {
  private BigDecimal maxSampleSize = null;
  private Integer washoutPeriod = 0;

  /**
   * Max sample size 
   * @return maxSampleSize
   **/
  @Override
  public BigDecimal getMaxSampleSize() {
    return maxSampleSize;
  }

    /**
     *
     * @param maxSampleSize
     */
    public void setMaxSampleSize(BigDecimal maxSampleSize) {
    this.maxSampleSize = maxSampleSize;
  }

  /**
   * The mininum required continuous observation time prior to index date for a person to be included in the cohort. Note that this is typically done in the createStudyPopulation function,but can already be done here for efficiency reasons. 
   * @return washoutPeriod
   **/
  @Override
  public Integer getWashoutPeriod() {
    return washoutPeriod;
  }

    /**
     *
     * @param washoutPeriod
     */
    public void setWashoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
  }
}
