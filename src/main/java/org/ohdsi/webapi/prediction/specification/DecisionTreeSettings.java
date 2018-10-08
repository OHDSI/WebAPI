package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specification for a Decision Tree Model 
 */
public class DecisionTreeSettings   {
  @JsonProperty("maxDepth")
  private Integer maxDepth = 10;

  @JsonProperty("minSampleSplit")
  private Integer minSampleSplit = 2;

  @JsonProperty("minSampleLeaf")
  private Integer minSampleLeaf = 10;

  @JsonProperty("minImpurityDecrease")
  private Float minImpurityDecrease = 1.0E-7f;

  @JsonProperty("seed")
  private Float seed = null;

  @JsonProperty("classWeight")
  private String classWeight = "None";

  @JsonProperty("plot")
  private Boolean plot = false;

  public DecisionTreeSettings maxDepth(Integer maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  /**
   * The maximum depth of the tree 
   * @return maxDepth
   **/
  @JsonProperty("maxDepth")
  public Integer getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(Integer maxDepth) {
    this.maxDepth = maxDepth;
  }

  public DecisionTreeSettings minSampleSplit(Integer minSampleSplit) {
    this.minSampleSplit = minSampleSplit;
    return this;
  }

  /**
   * The minimum samples per split 
   * @return minSampleSplit
   **/
  @JsonProperty("minSampleSplit")
  public Integer getMinSampleSplit() {
    return minSampleSplit;
  }

  public void setMinSampleSplit(Integer minSampleSplit) {
    this.minSampleSplit = minSampleSplit;
  }

  public DecisionTreeSettings minSampleLeaf(Integer minSampleLeaf) {
    this.minSampleLeaf = minSampleLeaf;
    return this;
  }

  /**
   * The minimum number of samples per leaf 
   * @return minSampleLeaf
   **/
  @JsonProperty("minSampleLeaf")
  public Integer getMinSampleLeaf() {
    return minSampleLeaf;
  }

  public void setMinSampleLeaf(Integer minSampleLeaf) {
    this.minSampleLeaf = minSampleLeaf;
  }

  public DecisionTreeSettings minImpurityDecrease(Float minImpurityDecrease) {
    this.minImpurityDecrease = minImpurityDecrease;
    return this;
  }

  /**
   * Threshold for early stopping in tree growth. A node will split if its impurity is above the threshold, otherwise it is a leaf.  
   * @return minImpurityDecrease
   **/
  @JsonProperty("minImpurityDecrease")
  public Float getMinImpurityDecrease() {
    return minImpurityDecrease;
  }

  public void setMinImpurityDecrease(Float minImpurityDecrease) {
    this.minImpurityDecrease = minImpurityDecrease;
  }

  public DecisionTreeSettings seed(Float seed) {
    this.seed = seed;
    return this;
  }

  /**
   * An option to add a seed when training the final model
   * @return seed
   **/
  @JsonProperty("seed")
  public Float getSeed() {
    return seed;
  }

  public void setSeed(Float seed) {
    this.seed = seed;
  }

  public DecisionTreeSettings classWeight(String classWeight) {
    this.classWeight = classWeight;
    return this;
  }

  /**
   * Balance or None 
   * @return classWeight
   **/
  @JsonProperty("classWeight")
  public String getClassWeight() {
    return classWeight;
  }

  public void setClassWeight(String classWeight) {
    this.classWeight = classWeight;
  }

  public DecisionTreeSettings plot(Boolean plot) {
    this.plot = plot;
    return this;
  }

  /**
   * Boolean whether to plot the tree (requires python pydotplus module) 
   * @return plot
   **/
  @JsonProperty("plot")
  public Boolean isisPlot() {
    return plot;
  }

  public void setPlot(Boolean plot) {
    this.plot = plot;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DecisionTreeSettings decisionTree = (DecisionTreeSettings) o;
    return Objects.equals(this.maxDepth, decisionTree.maxDepth) &&
        Objects.equals(this.minSampleSplit, decisionTree.minSampleSplit) &&
        Objects.equals(this.minSampleLeaf, decisionTree.minSampleLeaf) &&
        Objects.equals(this.minImpurityDecrease, decisionTree.minImpurityDecrease) &&
        Objects.equals(this.seed, decisionTree.seed) &&
        Objects.equals(this.classWeight, decisionTree.classWeight) &&
        Objects.equals(this.plot, decisionTree.plot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxDepth, minSampleSplit, minSampleLeaf, minImpurityDecrease, seed, classWeight, plot);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DecisionTree {\n");
    
    sb.append("    maxDepth: ").append(toIndentedString(maxDepth)).append("\n");
    sb.append("    minSampleSplit: ").append(toIndentedString(minSampleSplit)).append("\n");
    sb.append("    minSampleLeaf: ").append(toIndentedString(minSampleLeaf)).append("\n");
    sb.append("    minImpurityDecrease: ").append(toIndentedString(minImpurityDecrease)).append("\n");
    sb.append("    seed: ").append(toIndentedString(seed)).append("\n");
    sb.append("    classWeight: ").append(toIndentedString(classWeight)).append("\n");
    sb.append("    plot: ").append(toIndentedString(plot)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
