package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MLPSettings {
  @JsonProperty("size")
  private Integer size = 4;

  @JsonProperty("alpha")
  private Float alpha = 0.000010f;

  @JsonProperty("seed")
  private Float seed = null;

  public MLPSettings size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * The number of hidden nodes 
   * @return size
   **/
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public MLPSettings alpha(Float alpha) {
    this.alpha = alpha;
    return this;
  }

  /**
   * The L2 regularisation 
   * @return alpha
   **/
  @JsonProperty("alpha")
  public Float getAlpha() {
    return alpha;
  }

  public void setAlpha(Float alpha) {
    this.alpha = alpha;
  }

  public MLPSettings seed(Float seed) {
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
    MLPSettings multilayerPerceptionModel = (MLPSettings) o;
    return Objects.equals(this.size, multilayerPerceptionModel.size) &&
        Objects.equals(this.alpha, multilayerPerceptionModel.alpha) &&
        Objects.equals(this.seed, multilayerPerceptionModel.seed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, alpha, seed);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MultilayerPerceptionModel {\n");
    
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    alpha: ").append(toIndentedString(alpha)).append("\n");
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
