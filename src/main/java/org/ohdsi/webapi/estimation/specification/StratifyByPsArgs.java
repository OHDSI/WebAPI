package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

public class StratifyByPsArgs {
  @JsonProperty("numberOfStrata")
  private Integer numberOfStrata = 5;

  /**
   * What is the base selection of subjects where the strata bounds are to be determined? Strata are defined as equally-sized strata inside this selection. Possible values are \&quot;all\&quot;, \&quot;target\&quot;, and \&quot;comparator\&quot;. 
   */
  public enum BaseSelectionEnum {
    ALL("all"),
    
    TARGET("target"),
    
    COMPARATOR("comparator");

    private String value;

    BaseSelectionEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static BaseSelectionEnum fromValue(String text) {
      for (BaseSelectionEnum b : BaseSelectionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("baseSelection")
  private BaseSelectionEnum baseSelection = BaseSelectionEnum.ALL;

  @JsonProperty("stratificationColumns")
  private List<String> stratificationColumns = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public StratifyByPsArgs numberOfStrata(Integer numberOfStrata) {
    this.numberOfStrata = numberOfStrata;
    return this;
  }

  /**
   * How many strata? The boundaries of the strata are automatically defined to contain equal numbers of target persons. 
   * @return numberOfStrata
   **/
  @JsonProperty("numberOfStrata")
  @NotNull
  public Integer getNumberOfStrata() {
    return numberOfStrata;
  }

  public void setNumberOfStrata(Integer numberOfStrata) {
    this.numberOfStrata = numberOfStrata;
  }

  public StratifyByPsArgs baseSelection(BaseSelectionEnum baseSelection) {
    this.baseSelection = baseSelection;
    return this;
  }

  /**
   * What is the base selection of subjects where the strata bounds are to be determined? Strata are defined as equally-sized strata inside this selection. Possible values are \&quot;all\&quot;, \&quot;target\&quot;, and \&quot;comparator\&quot;. 
   * @return baseSelection
   **/
  @JsonProperty("baseSelection")
  public BaseSelectionEnum getBaseSelection() {
    return baseSelection;
  }

  public void setBaseSelection(BaseSelectionEnum baseSelection) {
    this.baseSelection = baseSelection;
  }

  public StratifyByPsArgs stratificationColumns(List<String> stratificationColumns) {
    this.stratificationColumns = stratificationColumns;
    return this;
  }

  public StratifyByPsArgs addStratificationColumnsItem(String stratificationColumnsItem) {
    if (this.stratificationColumns == null) {
      this.stratificationColumns = new ArrayList<String>();
    }
    this.stratificationColumns.add(stratificationColumnsItem);
    return this;
  }

  /**
   * Names of one or more columns in the data data.frame on which subjects should also be stratified in addition to stratification on propensity score. 
   * @return stratificationColumns
   **/
  @JsonProperty("stratificationColumns")
  public List<String> getStratificationColumns() {
    return stratificationColumns;
  }

  public void setStratificationColumns(List<String> stratificationColumns) {
    this.stratificationColumns = stratificationColumns;
  }

  public StratifyByPsArgs attrClass(String attrClass) {
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
    StratifyByPsArgs stratifyByPsArgs = (StratifyByPsArgs) o;
    return Objects.equals(this.numberOfStrata, stratifyByPsArgs.numberOfStrata) &&
        Objects.equals(this.baseSelection, stratifyByPsArgs.baseSelection) &&
        Objects.equals(this.stratificationColumns, stratifyByPsArgs.stratificationColumns) &&
        Objects.equals(this.attrClass, stratifyByPsArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numberOfStrata, baseSelection, stratificationColumns, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StratifyByPsArgs {\n");
    
    sb.append("    numberOfStrata: ").append(toIndentedString(numberOfStrata)).append("\n");
    sb.append("    baseSelection: ").append(toIndentedString(baseSelection)).append("\n");
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
