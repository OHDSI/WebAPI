package org.ohdsi.webapi.prediction.specification;

import org.ohdsi.analysis.prediction.design.DecisionTreeSettings;

/**
 * Specification for a Decision Tree Model 
 */
public class DecisionTreeSettingsImpl extends SeedSettingsImpl implements DecisionTreeSettings {
  private Integer maxDepth = 10;
  private Integer minSampleSplit = 2;
  private Integer minSampleLeaf = 10;
  private Float minImpurityDecrease = 1.0E-7f;
  private String classWeight = "None";
  private Boolean plot = false;

  /**
   * The maximum depth of the tree 
   * @return maxDepth
   **/
  @Override
  public Integer getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(Integer maxDepth) {
    this.maxDepth = maxDepth;
  }

  /**
   * The minimum samples per split 
   * @return minSampleSplit
   **/
  @Override
  public Integer getMinSampleSplit() {
    return minSampleSplit;
  }

  public void setMinSampleSplit(Integer minSampleSplit) {
    this.minSampleSplit = minSampleSplit;
  }

  /**
   * The minimum number of samples per leaf 
   * @return minSampleLeaf
   **/
  @Override
  public Integer getMinSampleLeaf() {
    return minSampleLeaf;
  }

  public void setMinSampleLeaf(Integer minSampleLeaf) {
    this.minSampleLeaf = minSampleLeaf;
  }

  /**
   * Threshold for early stopping in tree growth. A node will split if its impurity is above the threshold, otherwise it is a leaf.  
   * @return minImpurityDecrease
   **/
  @Override
  public Float getMinImpurityDecrease() {
    return minImpurityDecrease;
  }

  public void setMinImpurityDecrease(Float minImpurityDecrease) {
    this.minImpurityDecrease = minImpurityDecrease;
  }

  /**
   * Balance or None 
   * @return classWeight
   **/
  @Override
  public String getClassWeight() {
    return classWeight;
  }

  public void setClassWeight(String classWeight) {
    this.classWeight = classWeight;
  }

  /**
   * Boolean whether to plot the tree (requires python pydotplus module) 
   * @return plot
   **/
  @Override
  public Boolean getPlot() {
    return plot;
  }

  public void setPlot(Boolean plot) {
    this.plot = plot;
  }
}
