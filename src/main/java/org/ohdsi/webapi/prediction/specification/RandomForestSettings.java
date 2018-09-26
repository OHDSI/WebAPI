package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RandomForestSettings {
  @JsonProperty("mtries")
  private Integer mtries = -1;

  @JsonProperty("ntrees")
  private Integer ntrees = 500;

  @JsonProperty("maxDepth")
  private List<BigDecimal> maxDepth = null;

  @JsonProperty("varImp")
  private Boolean varImp = true;

  @JsonProperty("seed")
  private Float seed = null;

  public RandomForestSettings mtries(Integer mtries) {
    this.mtries = mtries;
    return this;
  }

  /**
   * The number of features to include in each tree (-1 defaults to square root of total features) 
   * @return mtries
   **/
  @JsonProperty("mtries")
  public Integer getMtries() {
    return mtries;
  }

  public void setMtries(Integer mtries) {
    this.mtries = mtries;
  }

  public RandomForestSettings ntrees(Integer ntrees) {
    this.ntrees = ntrees;
    return this;
  }

  /**
   * The number of trees to build 
   * @return ntrees
   **/
  @JsonProperty("ntrees")
  public Integer getNtrees() {
    return ntrees;
  }

  public void setNtrees(Integer ntrees) {
    this.ntrees = ntrees;
  }

  public RandomForestSettings maxDepth(List<BigDecimal> maxDepth) {
    this.maxDepth = maxDepth;
    return this;
  }

  public RandomForestSettings addMaxDepthItem(BigDecimal maxDepthItem) {
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

  public RandomForestSettings varImp(Boolean varImp) {
    this.varImp = varImp;
    return this;
  }

  /**
   * Perform an initial variable selection prior to fitting the model to select the useful variables 
   * @return varImp
   **/
  @JsonProperty("varImp")
  public Boolean isisVarImp() {
    return varImp;
  }

  public void setVarImp(Boolean varImp) {
    this.varImp = varImp;
  }

  public RandomForestSettings seed(Float seed) {
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
    RandomForestSettings randomForest = (RandomForestSettings) o;
    return Objects.equals(this.mtries, randomForest.mtries) &&
        Objects.equals(this.ntrees, randomForest.ntrees) &&
        Objects.equals(this.maxDepth, randomForest.maxDepth) &&
        Objects.equals(this.varImp, randomForest.varImp) &&
        Objects.equals(this.seed, randomForest.seed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mtries, ntrees, maxDepth, varImp, seed);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RandomForest {\n");
    
    sb.append("    mtries: ").append(toIndentedString(mtries)).append("\n");
    sb.append("    ntrees: ").append(toIndentedString(ntrees)).append("\n");
    sb.append("    maxDepth: ").append(toIndentedString(maxDepth)).append("\n");
    sb.append("    varImp: ").append(toIndentedString(varImp)).append("\n");
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
