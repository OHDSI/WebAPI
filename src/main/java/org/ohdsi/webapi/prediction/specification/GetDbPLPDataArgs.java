package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 *
 * @author asena5
 */
public class GetDbPLPDataArgs {
  @JsonProperty("maxSampleSize")
  private BigDecimal maxSampleSize = null;

  @JsonProperty("washoutPeriod")
  private Integer washoutPeriod = 0;

  public GetDbPLPDataArgs maxSampleSize(BigDecimal maxSampleSize) {
    this.maxSampleSize = maxSampleSize;
    return this;
  }

  /**
   * Max sample size 
   * @return maxSampleSize
   **/
  @JsonProperty("maxSampleSize")
  public BigDecimal getMaxSampleSize() {
    return maxSampleSize;
  }

  public void setMaxSampleSize(BigDecimal maxSampleSize) {
    this.maxSampleSize = maxSampleSize;
  }

  public GetDbPLPDataArgs washoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
    return this;
  }

  /**
   * The mininum required continuous observation time prior to index date for a person to be included in the cohort. Note that this is typically done in the createStudyPopulation function,but can already be done here for efficiency reasons. 
   * @return washoutPeriod
   **/
  @JsonProperty("washoutPeriod")
  public Integer getWashoutPeriod() {
    return washoutPeriod;
  }

  public void setWashoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetDbPLPDataArgs getDbPLPDataArgs = (GetDbPLPDataArgs) o;
    return Objects.equals(this.maxSampleSize, getDbPLPDataArgs.maxSampleSize) &&
        Objects.equals(this.washoutPeriod, getDbPLPDataArgs.washoutPeriod);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxSampleSize, washoutPeriod);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetDbPLPDataArgs {\n");
    
    sb.append("    maxSampleSize: ").append(toIndentedString(maxSampleSize)).append("\n");
    sb.append("    washoutPeriod: ").append(toIndentedString(washoutPeriod)).append("\n");
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
