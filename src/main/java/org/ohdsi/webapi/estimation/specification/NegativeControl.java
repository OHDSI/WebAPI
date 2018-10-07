package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class NegativeControl {
  @JsonProperty("targetId")
  private Long targetId = null;

  @JsonProperty("comparatorId")
  private Long comparatorId = null;

  @JsonProperty("outcomeId")
  private Long outcomeId = null;

  @JsonProperty("outcomeName")
  private String outcomeName = null;

  /**
   * The type of negative control 
   */
  public enum TypeEnum {
    OUTCOME("outcome"),
    
    EXPOSURE("exposure");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("type")
  private TypeEnum type = TypeEnum.OUTCOME;

  public NegativeControl targetId(Long targetId) {
    this.targetId = targetId;
    return this;
  }

  /**
   * The identifier for the target cohort 
   * @return targetId
   **/
  @JsonProperty("targetId")
  public Long getTargetId() {
    return targetId;
  }

  public void setTargetId(Long targetId) {
    this.targetId = targetId;
  }

  public NegativeControl comparatorId(Long comparatorId) {
    this.comparatorId = comparatorId;
    return this;
  }

  /**
   * The identifier for the comparator cohort 
   * @return comparatorId
   **/
  @JsonProperty("comparatorId")
  public Long getComparatorId() {
    return comparatorId;
  }

  public void setComparatorId(Long comparatorId) {
    this.comparatorId = comparatorId;
  }

  public NegativeControl outcomeId(Long outcomeId) {
    this.outcomeId = outcomeId;
    return this;
  }

  /**
   * The identifier for the negative control cohort 
   * @return outcomeId
   **/
  @JsonProperty("outcomeId")
  public Long getOutcomeId() {
    return outcomeId;
  }

  public void setOutcomeId(Long outcomeId) {
    this.outcomeId = outcomeId;
  }

  public NegativeControl outcomeName(String outcomeName) {
    this.outcomeName = outcomeName;
    return this;
  }

  /**
   * The name of the negative control cohort 
   * @return outcomeName
   **/
  @JsonProperty("outcomeName")
  public String getOutcomeName() {
    return outcomeName;
  }

  public void setOutcomeName(String outcomeName) {
    this.outcomeName = outcomeName;
  }

  public NegativeControl type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * The type of negative control 
   * @return type
   **/
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NegativeControl negativeControl = (NegativeControl) o;
    return Objects.equals(this.targetId, negativeControl.targetId) &&
        Objects.equals(this.comparatorId, negativeControl.comparatorId) &&
        Objects.equals(this.outcomeId, negativeControl.outcomeId) &&
        Objects.equals(this.outcomeName, negativeControl.outcomeName) &&
        Objects.equals(this.type, negativeControl.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetId, comparatorId, outcomeId, outcomeName, type);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NegativeControl {\n");
    
    sb.append("    targetId: ").append(toIndentedString(targetId)).append("\n");
    sb.append("    comparatorId: ").append(toIndentedString(comparatorId)).append("\n");
    sb.append("    outcomeId: ").append(toIndentedString(outcomeId)).append("\n");
    sb.append("    outcomeName: ").append(toIndentedString(outcomeName)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
