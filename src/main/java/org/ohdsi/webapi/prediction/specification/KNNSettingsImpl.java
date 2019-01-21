package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.KNNSettings;

/**
 *
 * @author asena5
 */
public class KNNSettingsImpl extends ModelSettingsImpl implements KNNSettings {
  private Integer k = 1000;
  
  /**
   * The number of neighbors to consider 
   * @return k
   **/
  @Override
  public Integer getK() {
    return k;
  }

    /**
     *
     * @param k
     */
    public void setK(Integer k) {
    this.k = k;
  }
}
