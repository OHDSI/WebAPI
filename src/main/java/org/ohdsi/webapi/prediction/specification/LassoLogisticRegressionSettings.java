package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LassoLogisticRegressionSettings {
  @JsonProperty("variance")
  private Float variance = 0.01f;

  @JsonProperty("seed")
  private Float seed = null;

  public LassoLogisticRegressionSettings variance(Float variance) {
    this.variance = variance;
    return this;
  }

  /**
   * A single value used as the starting value for the automatic lambda search 
   * @return variance
   **/
  @JsonProperty("variance")
  public Float getVariance() {
    return variance;
  }

  public void setVariance(Float variance) {
    this.variance = variance;
  }

  public LassoLogisticRegressionSettings seed(Float seed) {
    this.seed = seed;
    return this;
  }

  /**
   * An option to add a seed when training the model
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
    LassoLogisticRegressionSettings lassoLogisticRegression = (LassoLogisticRegressionSettings) o;
    return Objects.equals(this.variance, lassoLogisticRegression.variance) &&
        Objects.equals(this.seed, lassoLogisticRegression.seed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variance, seed);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LassoLogisticRegression {\n");
    
    sb.append("    variance: ").append(toIndentedString(variance)).append("\n");
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
