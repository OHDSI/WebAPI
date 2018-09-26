package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specification for a Ada Boost Model 
 */
public class AdaBoostSettings   {
  @JsonProperty("nEstimators")
  private Integer nEstimators = 50;

  @JsonProperty("learningRate")
  private Integer learningRate = 1;

  @JsonProperty("seed")
  private Float seed = null;

  public AdaBoostSettings nEstimators(Integer nEstimators) {
    this.nEstimators = nEstimators;
    return this;
  }

  /**
   * The maximum number of estimators at which boosting is terminated 
   * @return nEstimators
   **/
  @JsonProperty("nEstimators")
  public Integer getNEstimators() {
    return nEstimators;
  }

  public void setNEstimators(Integer nEstimators) {
    this.nEstimators = nEstimators;
  }

  public AdaBoostSettings learningRate(Integer learningRate) {
    this.learningRate = learningRate;
    return this;
  }

  /**
   * Learning rate shrinks the contribution of each classifier by learningRate. There is a trade-off between learningRate and nEstimators . 
   * @return learningRate
   **/
  @JsonProperty("learningRate")
  public Integer getLearningRate() {
    return learningRate;
  }

  public void setLearningRate(Integer learningRate) {
    this.learningRate = learningRate;
  }

  public AdaBoostSettings seed(Float seed) {
    this.seed = seed;
    return this;
  }

  /**
   * A seed for the model
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
    AdaBoostSettings adaBoost = (AdaBoostSettings) o;
    return Objects.equals(this.nEstimators, adaBoost.nEstimators) &&
        Objects.equals(this.learningRate, adaBoost.learningRate) &&
        Objects.equals(this.seed, adaBoost.seed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nEstimators, learningRate, seed);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdaBoost {\n");
    
    sb.append("    nEstimators: ").append(toIndentedString(nEstimators)).append("\n");
    sb.append("    learningRate: ").append(toIndentedString(learningRate)).append("\n");
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
