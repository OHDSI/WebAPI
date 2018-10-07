package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TrimByPsArgs {
  @JsonProperty("trimFraction")
  private Float trimFraction = 0.05f;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public TrimByPsArgs trimFraction(Float trimFraction) {
    this.trimFraction = trimFraction;
    return this;
  }

  /**
   * This fraction will be removed from each target group. In the target group, persons with the highest propensity scores will be removed, in the comparator group person with the lowest scores will be removed. 
   * @return trimFraction
   **/
  @JsonProperty("trimFraction")
  public Float getTrimFraction() {
    return trimFraction;
  }

  public void setTrimFraction(Float trimFraction) {
    this.trimFraction = trimFraction;
  }

  public TrimByPsArgs attrClass(String attrClass) {
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
    TrimByPsArgs trimByPsArgs = (TrimByPsArgs) o;
    return Objects.equals(this.trimFraction, trimByPsArgs.trimFraction) &&
        Objects.equals(this.attrClass, trimByPsArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trimFraction, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrimByPsArgs {\n");
    
    sb.append("    trimFraction: ").append(toIndentedString(trimFraction)).append("\n");
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
