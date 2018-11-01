package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.analysis.prediction.design.RandomForestSettings;

public class RandomForestSettingsImpl extends SeedSettingsImpl implements RandomForestSettings {
  private Integer mtries = -1;
  private Integer ntrees = 500;
  private List<BigDecimal> maxDepth = null;
  private Boolean varImp = true;
  
  /**
   * The number of features to include in each tree (-1 defaults to square root of total features) 
   * @return mtries
   **/
  @Override
  public Integer getMtries() {
    return mtries;
  }

  public void setMtries(Integer mtries) {
    this.mtries = mtries;
  }

  /**
   * The number of trees to build 
   * @return ntrees
   **/
  @Override
  public Integer getNtrees() {
    return ntrees;
  }

  public void setNtrees(Integer ntrees) {
    this.ntrees = ntrees;
  }

  public RandomForestSettingsImpl addMaxDepthItem(BigDecimal maxDepthItem) {
    if (this.maxDepth == null) {
      this.maxDepth = new ArrayList<>();
    }
    this.maxDepth.add(maxDepthItem);
    return this;
  }

  /**
   * Maximum number of interactions - a large value will lead to slow model training 
   * @return maxDepth
   **/
  @Override
  public List<BigDecimal> getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(List<BigDecimal> maxDepth) {
    this.maxDepth = maxDepth;
  }

  /**
   * Perform an initial variable selection prior to fitting the model to select the useful variables 
   * @return varImp
   **/
  @Override
  public Boolean getVarImp() {
    return varImp;
  }

  public void setVarImp(Boolean varImp) {
    this.varImp = varImp;
  }
}
