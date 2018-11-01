package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.MLPSettings;

public class MLPSettingsImpl extends SeedSettingsImpl implements MLPSettings {
  private Integer size = 4;
  private Float alpha = 0.000010f;

  /**
   * The number of hidden nodes
   * @return size
   **/
  @Override
  public Integer getSize() {
    return size;
  }

  public void setAlpha(Integer size) {
    this.size = size;
  }
  
  /**
   * The L2 regularisation 
   * @return alpha
   **/
  @Override
  public Float getAlpha() {
    return alpha;
  }

  public void setAlpha(Float alpha) {
    this.alpha = alpha;
  }
}