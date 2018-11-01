package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.analysis.prediction.design.GradientBoostingMachineSettings;

public class GradientBoostingMachineSettingsImpl extends SeedSettingsImpl implements GradientBoostingMachineSettings {
  private List<BigDecimal> ntrees = null;
  private Integer nthread = 20;
  private List<BigDecimal> maxDepth = null;
  private Integer minRows = 20;
  private List<Float> learnRate = null;

  public GradientBoostingMachineSettingsImpl addNtreesItem(BigDecimal ntreesItem) {
    if (this.ntrees == null) {
      this.ntrees = new ArrayList<>();
    }
    this.ntrees.add(ntreesItem);
    return this;
  }

  /**
   * The number of trees to build 
   * @return ntrees
   **/
  @Override
  public List<BigDecimal> getNtrees() {
    return ntrees;
  }

  public void setNtrees(List<BigDecimal> ntrees) {
    this.ntrees = ntrees;
  }

  /**
   * The number of computer threads to (how many cores do you have?) 
   * @return nthread
   **/
  @Override
  public Integer getNthread() {
    return nthread;
  }

  public void setNthread(Integer nthread) {
    this.nthread = nthread;
  }

  public GradientBoostingMachineSettingsImpl addMaxDepthItem(BigDecimal maxDepthItem) {
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
   * The minimum number of rows required at each end node of the tree 
   * @return minRows
   **/
  @Override
  public Integer getMinRows() {
    return minRows;
  }

  public void setMinRows(Integer minRows) {
    this.minRows = minRows;
  }

  public GradientBoostingMachineSettingsImpl addLearnRateItem(Float learnRateItem) {
    if (this.learnRate == null) {
      this.learnRate = new ArrayList<>();
    }
    this.learnRate.add(learnRateItem);
    return this;
  }

  /**
   * The boosting learn rate 
   * @return learnRate
   **/
  @Override
  public List<Float> getLearnRate() {
    return learnRate;
  }

  public void setLearnRate(List<Float> learnRate) {
    this.learnRate = learnRate;
  }
}
