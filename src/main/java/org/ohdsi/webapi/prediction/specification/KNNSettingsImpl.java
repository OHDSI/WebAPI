package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.KNNSettings;

public class KNNSettingsImpl implements KNNSettings {
  private Integer k = 1000;
  
  /**
   * The number of neighbors to consider 
   * @return k
   **/
  @Override
  public Integer getK() {
    return k;
  }

  public void setK(Integer k) {
    this.k = k;
  }
}
