package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GradientBoostingMachineSettings {
  @JsonProperty("ntrees")
  private List<BigDecimal> ntrees = null;

  @JsonProperty("nthread")
  private Integer nthread = 20;

  @JsonProperty("maxDepth")
  private List<BigDecimal> maxDepth = null;

  @JsonProperty("minRows")
  private Integer minRows = 20;

  @JsonProperty("learnRate")
  private List<Float> learnRate = null;

  @JsonProperty("seed")
  private Float seed = null;

  public GradientBoostingMachineSettings ntrees(List<BigDecimal> ntrees) {
    this.ntrees = ntrees;
    return this;
  }

  public GradientBoostingMachineSettings addNtreesItem(BigDecimal ntreesItem) {
    if (this.ntrees == null) {
      this.ntrees = new ArrayList<BigDecimal>();
    }
    this.ntrees.add(ntreesItem);
    return this;
  }

  /**
   * The number of trees to build 
   * @return ntrees
   **/
  @JsonProperty("ntrees")
  public List<BigDecimal> getNtrees() {
    return ntrees;
  }

  public void setNtrees(List<BigDecimal> ntrees) {
    this.ntrees = ntrees;
  }

  public GradientBoostingMachineSettings nthread(Integer nthread) {
    this.nthread = nthread;
    return this;
  }

  /**
   * The number of computer threads to (how many cores do you have?) 
   * @return nthread
   **/
  @JsonProperty("nthread")
  public Integer getNthread() {
    return nthread;
  }

  public void setNthread(Integer nthread) {
    this.nthread = nthread;
  }

  public GradientBoostingMachineSettings maxDepth(List<BigDecimal> maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  public GradientBoostingMachineSettings addMaxDepthItem(BigDecimal maxDepthItem) {
    if (this.maxDepth == null) {
      this.maxDepth = new ArrayList<BigDecimal>();
    }
    this.maxDepth.add(maxDepthItem);
    return this;
  }

  /**
   * Maximum number of interactions - a large value will lead to slow model training 
   * @return maxDepth
   **/
  @JsonProperty("maxDepth")
  public List<BigDecimal> getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(List<BigDecimal> maxDepth) {
    this.maxDepth = maxDepth;
  }

  public GradientBoostingMachineSettings minRows(Integer minRows) {
    this.minRows = minRows;
    return this;
  }

  /**
   * The minimum number of rows required at each end node of the tree 
   * @return minRows
   **/
  @JsonProperty("minRows")
  public Integer getMinRows() {
    return minRows;
  }

  public void setMinRows(Integer minRows) {
    this.minRows = minRows;
  }

  public GradientBoostingMachineSettings learnRate(List<Float> learnRate) {
    this.learnRate = learnRate;
    return this;
  }

  public GradientBoostingMachineSettings addLearnRateItem(Float learnRateItem) {
    if (this.learnRate == null) {
      this.learnRate = new ArrayList<Float>();
    }
    this.learnRate.add(learnRateItem);
    return this;
  }

  /**
   * The boosting learn rate 
   * @return learnRate
   **/
  @JsonProperty("learnRate")
  public List<Float> getLearnRate() {
    return learnRate;
  }

  public void setLearnRate(List<Float> learnRate) {
    this.learnRate = learnRate;
  }

  public GradientBoostingMachineSettings seed(Float seed) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GradientBoostingMachineSettings gradientBoostingMachine = (GradientBoostingMachineSettings) o;
    return Objects.equals(this.ntrees, gradientBoostingMachine.ntrees) &&
        Objects.equals(this.nthread, gradientBoostingMachine.nthread) &&
        Objects.equals(this.maxDepth, gradientBoostingMachine.maxDepth) &&
        Objects.equals(this.minRows, gradientBoostingMachine.minRows) &&
        Objects.equals(this.learnRate, gradientBoostingMachine.learnRate) &&
        Objects.equals(this.seed, gradientBoostingMachine.seed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ntrees, nthread, maxDepth, minRows, learnRate, seed);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GradientBoostingMachine {\n");
    
    sb.append("    ntrees: ").append(toIndentedString(ntrees)).append("\n");
    sb.append("    nthread: ").append(toIndentedString(nthread)).append("\n");
    sb.append("    maxDepth: ").append(toIndentedString(maxDepth)).append("\n");
    sb.append("    minRows: ").append(toIndentedString(minRows)).append("\n");
    sb.append("    learnRate: ").append(toIndentedString(learnRate)).append("\n");
    sb.append("    seed: ").append(toIndentedString(seed)).append("\n");
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
