package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

public class StratifyByPsAndCovariatesArgs {
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

  @JsonProperty("covariateIds")
  private List<Integer> covariateIds = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public StratifyByPsAndCovariatesArgs numberOfStrata(Integer numberOfStrata) {
    this.numberOfStrata = numberOfStrata;
    return this;
  }

  /**
   * Into how many strata should the propensity score be divided? The boundaries of the strata are automatically defined to contain equal numbers of target persons. 
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

  public StratifyByPsAndCovariatesArgs baseSelection(BaseSelectionEnum baseSelection) {
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

  public StratifyByPsAndCovariatesArgs covariateIds(List<Integer> covariateIds) {
    this.covariateIds = covariateIds;
    return this;
  }

  public StratifyByPsAndCovariatesArgs addCovariateIdsItem(Integer covariateIdsItem) {
    if (this.covariateIds == null) {
      this.covariateIds = new ArrayList<Integer>();
    }
    this.covariateIds.add(covariateIdsItem);
    return this;
  }

  /**
   * One or more covariate IDs in the cohortMethodData object on which subjects should also be stratified. 
   * @return covariateIds
   **/
  @JsonProperty("covariateIds")
  public List<Integer> getCovariateIds() {
    return covariateIds;
  }

  public void setCovariateIds(List<Integer> covariateIds) {
    this.covariateIds = covariateIds;
  }

  public StratifyByPsAndCovariatesArgs attrClass(String attrClass) {
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
    StratifyByPsAndCovariatesArgs stratifyByPsAndCovariatesArgs = (StratifyByPsAndCovariatesArgs) o;
    return Objects.equals(this.numberOfStrata, stratifyByPsAndCovariatesArgs.numberOfStrata) &&
        Objects.equals(this.baseSelection, stratifyByPsAndCovariatesArgs.baseSelection) &&
        Objects.equals(this.covariateIds, stratifyByPsAndCovariatesArgs.covariateIds) &&
        Objects.equals(this.attrClass, stratifyByPsAndCovariatesArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numberOfStrata, baseSelection, covariateIds, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StratifyByPsAndCovariatesArgs {\n");
    
    sb.append("    numberOfStrata: ").append(toIndentedString(numberOfStrata)).append("\n");
    sb.append("    baseSelection: ").append(toIndentedString(baseSelection)).append("\n");
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
