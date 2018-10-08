package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;

public class MatchOnPsArgs {
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

  @JsonProperty("stratificationColumns")
  private List<String> stratificationColumns = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public MatchOnPsArgs caliper(Float caliper) {
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

  public MatchOnPsArgs caliperScale(CaliperScaleEnum caliperScale) {
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

  public MatchOnPsArgs maxRatio(Integer maxRatio) {
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

  public MatchOnPsArgs stratificationColumns(List<String> stratificationColumns) {
    this.stratificationColumns = stratificationColumns;
    return this;
  }

  public MatchOnPsArgs addStratificationColumnsItem(String stratificationColumnsItem) {
    if (this.stratificationColumns == null) {
      this.stratificationColumns = new ArrayList<String>();
    }
    this.stratificationColumns.add(stratificationColumnsItem);
    return this;
  }

  /**
   * Names or numbers of one or more columns in the data data.frame on which subjects should be stratified prior to matching. No personswill be matched with persons outside of the strata identified by thevalues in these columns. 
   * @return stratificationColumns
   **/
  @JsonProperty("stratificationColumns")
  public List<String> getStratificationColumns() {
    return stratificationColumns;
  }

  public void setStratificationColumns(List<String> stratificationColumns) {
    this.stratificationColumns = stratificationColumns;
  }

  public MatchOnPsArgs attrClass(String attrClass) {
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
    MatchOnPsArgs matchOnPsArgs = (MatchOnPsArgs) o;
    return Objects.equals(this.caliper, matchOnPsArgs.caliper) &&
        Objects.equals(this.caliperScale, matchOnPsArgs.caliperScale) &&
        Objects.equals(this.maxRatio, matchOnPsArgs.maxRatio) &&
        Objects.equals(this.stratificationColumns, matchOnPsArgs.stratificationColumns) &&
        Objects.equals(this.attrClass, matchOnPsArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(caliper, caliperScale, maxRatio, stratificationColumns, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MatchOnPsArgs {\n");
    
    sb.append("    caliper: ").append(toIndentedString(caliper)).append("\n");
    sb.append("    caliperScale: ").append(toIndentedString(caliperScale)).append("\n");
    sb.append("    maxRatio: ").append(toIndentedString(maxRatio)).append("\n");
    sb.append("    stratificationColumns: ").append(toIndentedString(stratificationColumns)).append("\n");
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
