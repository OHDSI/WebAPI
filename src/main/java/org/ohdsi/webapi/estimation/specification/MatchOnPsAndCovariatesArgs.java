package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class MatchOnPsAndCovariatesArgs {
  @JsonProperty("caliper")
  private Float caliper = 0.2f;

  /**
   * The scale on which the caliper is defined. Three scales are supported are &#x27;propensity score&#x27;, &#x27;standardized&#x27;, or &#x27;standardized logit&#x27;. On the standardized scale, the caliper is interpreted in standard deviations of the propensity score distribution. &#x27;standardized logit&#x27; is similar, except that the propensity score is transformed to the logit scale because the PS is more likely to be normally distributed on that scale(Austin, 2011). 
   */
  public enum CaliperScaleEnum {
    PROPENSITY_SCORE("propensity score"),
    
    STANDARDIZED("standardized"),
    
    STANDARDIZED_LOGIT("standardized logit");

    private String value;

    CaliperScaleEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static CaliperScaleEnum fromValue(String text) {
      for (CaliperScaleEnum b : CaliperScaleEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("caliperScale")
  private CaliperScaleEnum caliperScale = CaliperScaleEnum.STANDARDIZED_LOGIT;

  @JsonProperty("maxRatio")
  private Integer maxRatio = 1;

  @JsonProperty("covariateIds")
  private List<Integer> covariateIds = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public MatchOnPsAndCovariatesArgs caliper(Float caliper) {
    this.caliper = caliper;
    return this;
  }

  /**
   * The caliper for matching. A caliper is the distance which is acceptable for any match. Observations which are outside of the caliper are dropped. A caliper of 0 means no caliper is used. 
   * @return caliper
   **/
  @JsonProperty("caliper")
  public Float getCaliper() {
    return caliper;
  }

  public void setCaliper(Float caliper) {
    this.caliper = caliper;
  }

  public MatchOnPsAndCovariatesArgs caliperScale(CaliperScaleEnum caliperScale) {
    this.caliperScale = caliperScale;
    return this;
  }

  /**
   * The scale on which the caliper is defined. Three scales are supported are &#x27;propensity score&#x27;, &#x27;standardized&#x27;, or &#x27;standardized logit&#x27;. On the standardized scale, the caliper is interpreted in standard deviations of the propensity score distribution. &#x27;standardized logit&#x27; is similar, except that the propensity score is transformed to the logit scale because the PS is more likely to be normally distributed on that scale(Austin, 2011). 
   * @return caliperScale
   **/
  @JsonProperty("caliperScale")
  public CaliperScaleEnum getCaliperScale() {
    return caliperScale;
  }

  public void setCaliperScale(CaliperScaleEnum caliperScale) {
    this.caliperScale = caliperScale;
  }

  public MatchOnPsAndCovariatesArgs maxRatio(Integer maxRatio) {
    this.maxRatio = maxRatio;
    return this;
  }

  /**
   * The maximum number of persons int the comparator arm to be matched to each person in the target arm. A maxRatio of 0 means no maximum - all comparators will be assigned to a target person. 
   * @return maxRatio
   **/
  @JsonProperty("maxRatio")
  public Integer getMaxRatio() {
    return maxRatio;
  }

  public void setMaxRatio(Integer maxRatio) {
    this.maxRatio = maxRatio;
  }

  public MatchOnPsAndCovariatesArgs covariateIds(List<Integer> covariateIds) {
    this.covariateIds = covariateIds;
    return this;
  }

  public MatchOnPsAndCovariatesArgs addCovariateIdsItem(Integer covariateIdsItem) {
    if (this.covariateIds == null) {
      this.covariateIds = new ArrayList<Integer>();
    }
    this.covariateIds.add(covariateIdsItem);
    return this;
  }

  /**
   * One or more covariate IDs in the cohortMethodData object on whichsubjects should be also matched. 
   * @return covariateIds
   **/
  @JsonProperty("covariateIds")
  public List<Integer> getCovariateIds() {
    return covariateIds;
  }

  public void setCovariateIds(List<Integer> covariateIds) {
    this.covariateIds = covariateIds;
  }

  public MatchOnPsAndCovariatesArgs attrClass(String attrClass) {
    this.attrClass = attrClass;
    return this;
  }

  /**
   * Get attrClass
   * @return attrClass
   **/
  @JsonProperty("attr_class")
  public String getAttrClass() {
    return attrClass;
  }

  public void setAttrClass(String attrClass) {
    this.attrClass = attrClass;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MatchOnPsAndCovariatesArgs matchOnPsAndCovariatesArgs = (MatchOnPsAndCovariatesArgs) o;
    return Objects.equals(this.caliper, matchOnPsAndCovariatesArgs.caliper) &&
        Objects.equals(this.caliperScale, matchOnPsAndCovariatesArgs.caliperScale) &&
        Objects.equals(this.maxRatio, matchOnPsAndCovariatesArgs.maxRatio) &&
        Objects.equals(this.covariateIds, matchOnPsAndCovariatesArgs.covariateIds) &&
        Objects.equals(this.attrClass, matchOnPsAndCovariatesArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(caliper, caliperScale, maxRatio, covariateIds, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MatchOnPsAndCovariatesArgs {\n");
    
    sb.append("    caliper: ").append(toIndentedString(caliper)).append("\n");
    sb.append("    caliperScale: ").append(toIndentedString(caliperScale)).append("\n");
    sb.append("    maxRatio: ").append(toIndentedString(maxRatio)).append("\n");
    sb.append("    covariateIds: ").append(toIndentedString(covariateIds)).append("\n");
    sb.append("    attrClass: ").append(toIndentedString(attrClass)).append("\n");
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
