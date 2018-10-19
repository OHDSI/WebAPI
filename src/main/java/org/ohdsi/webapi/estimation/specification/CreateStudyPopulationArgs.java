package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class CreateStudyPopulationArgs {
  @JsonProperty("firstExposureOnly")
  private Boolean firstExposureOnly = false;

  @JsonProperty("restrictToCommonPeriod")
  private Boolean restrictToCommonPeriod = false;

  @JsonProperty("washoutPeriod")
  private Integer washoutPeriod = 0;

  /**
   * Remove subjects that are in both the target and comparator cohort? 
   */
  public enum RemoveDuplicateSubjectsEnum {
    KEEP_ALL("keep all"),
    
    KEEP_FIRST("keep first"),
    
    REMOVE_ALL("remove all");

    private String value;

    RemoveDuplicateSubjectsEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static RemoveDuplicateSubjectsEnum fromValue(String text) {
      for (RemoveDuplicateSubjectsEnum b : RemoveDuplicateSubjectsEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("removeDuplicateSubjects")
  private RemoveDuplicateSubjectsEnum removeDuplicateSubjects = RemoveDuplicateSubjectsEnum.KEEP_ALL;

  @JsonProperty("removeSubjectsWithPriorOutcome")
  private Boolean removeSubjectsWithPriorOutcome = false;

  @JsonProperty("priorOutcomeLookback")
  private Integer priorOutcomeLookback = 99999;

  @JsonProperty("minDaysAtRisk")
  private Integer minDaysAtRisk = 1;

  @JsonProperty("riskWindowStart")
  private Integer riskWindowStart = 0;

  @JsonProperty("addExposureDaysToStart")
  private Boolean addExposureDaysToStart = false;

  @JsonProperty("riskWindowEnd")
  private Integer riskWindowEnd = 0;

  @JsonProperty("addExposureDaysToEnd")
  private Boolean addExposureDaysToEnd = true;

  @JsonProperty("censorAtNewRiskWindow")
  private Boolean censorAtNewRiskWindow = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public CreateStudyPopulationArgs firstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
    return this;
  }

  /**
   * Should only the first exposure per subject be included? Note that this is typically done in the createStudyPopulation function 
   * @return firstExposureOnly
   **/
  @JsonProperty("firstExposureOnly")
  public Boolean isisFirstExposureOnly() {
    return firstExposureOnly;
  }

  public void setFirstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
  }

  public CreateStudyPopulationArgs restrictToCommonPeriod(Boolean restrictToCommonPeriod) {
    this.restrictToCommonPeriod = restrictToCommonPeriod;
    return this;
  }

  /**
   * Restrict the analysis to the period when both exposures are observed? 
   * @return restrictToCommonPeriod
   **/
  @JsonProperty("restrictToCommonPeriod")
  public Boolean isisRestrictToCommonPeriod() {
    return restrictToCommonPeriod;
  }

  public void setRestrictToCommonPeriod(Boolean restrictToCommonPeriod) {
    this.restrictToCommonPeriod = restrictToCommonPeriod;
  }

  public CreateStudyPopulationArgs washoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
    return this;
  }

  /**
   * The minimum required continuous observation time prior to index date for a person to be included in the cohort. 
   * @return washoutPeriod
   **/
  @JsonProperty("washoutPeriod")
  public Integer getWashoutPeriod() {
    return washoutPeriod;
  }

  public void setWashoutPeriod(Integer washoutPeriod) {
    this.washoutPeriod = washoutPeriod;
  }

  public CreateStudyPopulationArgs removeDuplicateSubjects(RemoveDuplicateSubjectsEnum removeDuplicateSubjects) {
    this.removeDuplicateSubjects = removeDuplicateSubjects;
    return this;
  }

  /**
   * Remove subjects that are in both the target and comparator cohort? 
   * @return removeDuplicateSubjects
   **/
  @JsonProperty("removeDuplicateSubjects")
  public RemoveDuplicateSubjectsEnum getRemoveDuplicateSubjects() {
    return removeDuplicateSubjects;
  }

  public void setRemoveDuplicateSubjects(RemoveDuplicateSubjectsEnum removeDuplicateSubjects) {
    this.removeDuplicateSubjects = removeDuplicateSubjects;
  }

  public CreateStudyPopulationArgs removeSubjectsWithPriorOutcome(Boolean removeSubjectsWithPriorOutcome) {
    this.removeSubjectsWithPriorOutcome = removeSubjectsWithPriorOutcome;
    return this;
  }

  /**
   * Remove subjects that have the outcome prior to the risk window start? 
   * @return removeSubjectsWithPriorOutcome
   **/
  @JsonProperty("removeSubjectsWithPriorOutcome")
  public Boolean isisRemoveSubjectsWithPriorOutcome() {
    return removeSubjectsWithPriorOutcome;
  }

  public void setRemoveSubjectsWithPriorOutcome(Boolean removeSubjectsWithPriorOutcome) {
    this.removeSubjectsWithPriorOutcome = removeSubjectsWithPriorOutcome;
  }

  public CreateStudyPopulationArgs priorOutcomeLookback(Integer priorOutcomeLookback) {
    this.priorOutcomeLookback = priorOutcomeLookback;
    return this;
  }

  /**
   * How many days should we look back when identifying prior outcomes? 
   * @return priorOutcomeLookback
   **/
  @JsonProperty("priorOutcomeLookback")
  public Integer getPriorOutcomeLookback() {
    return priorOutcomeLookback;
  }

  public void setPriorOutcomeLookback(Integer priorOutcomeLookback) {
    this.priorOutcomeLookback = priorOutcomeLookback;
  }

  public CreateStudyPopulationArgs minDaysAtRisk(Integer minDaysAtRisk) {
    this.minDaysAtRisk = minDaysAtRisk;
    return this;
  }

  /**
   * The minimum required number of days at risk. 
   * @return minDaysAtRisk
   **/
  @JsonProperty("minDaysAtRisk")
  public Integer getMinDaysAtRisk() {
    return minDaysAtRisk;
  }

  public void setMinDaysAtRisk(Integer minDaysAtRisk) {
    this.minDaysAtRisk = minDaysAtRisk;
  }

  public CreateStudyPopulationArgs riskWindowStart(Integer riskWindowStart) {
    this.riskWindowStart = riskWindowStart;
    return this;
  }

  /**
   * The start of the risk window (in days) relative to the indexdate (+ days of exposure if theaddExposureDaysToStart parameter is specified). 
   * @return riskWindowStart
   **/
  @JsonProperty("riskWindowStart")
  public Integer getRiskWindowStart() {
    return riskWindowStart;
  }

  public void setRiskWindowStart(Integer riskWindowStart) {
    this.riskWindowStart = riskWindowStart;
  }

  public CreateStudyPopulationArgs addExposureDaysToStart(Boolean addExposureDaysToStart) {
    this.addExposureDaysToStart = addExposureDaysToStart;
    return this;
  }

  /**
   * Add the length of exposure the start of the risk window?
   * @return addExposureDaysToStart
   **/
  @JsonProperty("addExposureDaysToStart")
  public Boolean isisAddExposureDaysToStart() {
    return addExposureDaysToStart;
  }

  public void setAddExposureDaysToStart(Boolean addExposureDaysToStart) {
    this.addExposureDaysToStart = addExposureDaysToStart;
  }

  public CreateStudyPopulationArgs riskWindowEnd(Integer riskWindowEnd) {
    this.riskWindowEnd = riskWindowEnd;
    return this;
  }

  /**
   * The end of the risk window (in days) relative to the index date (+ days of exposure if the addExposureDaysToEnd parameter is specified). 
   * @return riskWindowEnd
   **/
  @JsonProperty("riskWindowEnd")
  public Integer getRiskWindowEnd() {
    return riskWindowEnd;
  }

  public void setRiskWindowEnd(Integer riskWindowEnd) {
    this.riskWindowEnd = riskWindowEnd;
  }

  public CreateStudyPopulationArgs addExposureDaysToEnd(Boolean addExposureDaysToEnd) {
    this.addExposureDaysToEnd = addExposureDaysToEnd;
    return this;
  }

  /**
   * Add the length of exposure the risk window? 
   * @return addExposureDaysToEnd
   **/
  @JsonProperty("addExposureDaysToEnd")
  public Boolean isisAddExposureDaysToEnd() {
    return addExposureDaysToEnd;
  }

  public void setAddExposureDaysToEnd(Boolean addExposureDaysToEnd) {
    this.addExposureDaysToEnd = addExposureDaysToEnd;
  }

  public CreateStudyPopulationArgs censorAtNewRiskWindow(Boolean censorAtNewRiskWindow) {
    this.censorAtNewRiskWindow = censorAtNewRiskWindow;
    return this;
  }

  /**
   * If a subject is in multiple cohorts, should time-at-risk be censored when the new time-at-risk start to prevent overlap? 
   * @return censorAtNewRiskWindow
   **/
  @JsonProperty("censorAtNewRiskWindow")
  public Boolean isisCensorAtNewRiskWindow() {
    return censorAtNewRiskWindow;
  }

  public void setCensorAtNewRiskWindow(Boolean censorAtNewRiskWindow) {
    this.censorAtNewRiskWindow = censorAtNewRiskWindow;
  }

  public CreateStudyPopulationArgs attrClass(String attrClass) {
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
    CreateStudyPopulationArgs createStudyPopulationArgs = (CreateStudyPopulationArgs) o;
    return Objects.equals(this.firstExposureOnly, createStudyPopulationArgs.firstExposureOnly) &&
        Objects.equals(this.restrictToCommonPeriod, createStudyPopulationArgs.restrictToCommonPeriod) &&
        Objects.equals(this.washoutPeriod, createStudyPopulationArgs.washoutPeriod) &&
        Objects.equals(this.removeDuplicateSubjects, createStudyPopulationArgs.removeDuplicateSubjects) &&
        Objects.equals(this.removeSubjectsWithPriorOutcome, createStudyPopulationArgs.removeSubjectsWithPriorOutcome) &&
        Objects.equals(this.priorOutcomeLookback, createStudyPopulationArgs.priorOutcomeLookback) &&
        Objects.equals(this.minDaysAtRisk, createStudyPopulationArgs.minDaysAtRisk) &&
        Objects.equals(this.riskWindowStart, createStudyPopulationArgs.riskWindowStart) &&
        Objects.equals(this.addExposureDaysToStart, createStudyPopulationArgs.addExposureDaysToStart) &&
        Objects.equals(this.riskWindowEnd, createStudyPopulationArgs.riskWindowEnd) &&
        Objects.equals(this.addExposureDaysToEnd, createStudyPopulationArgs.addExposureDaysToEnd) &&
        Objects.equals(this.censorAtNewRiskWindow, createStudyPopulationArgs.censorAtNewRiskWindow) &&
        Objects.equals(this.attrClass, createStudyPopulationArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstExposureOnly, restrictToCommonPeriod, washoutPeriod, removeDuplicateSubjects, removeSubjectsWithPriorOutcome, priorOutcomeLookback, minDaysAtRisk, riskWindowStart, addExposureDaysToStart, riskWindowEnd, addExposureDaysToEnd, censorAtNewRiskWindow, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateStudyPopulationArgs {\n");
    
    sb.append("    firstExposureOnly: ").append(toIndentedString(firstExposureOnly)).append("\n");
    sb.append("    restrictToCommonPeriod: ").append(toIndentedString(restrictToCommonPeriod)).append("\n");
    sb.append("    washoutPeriod: ").append(toIndentedString(washoutPeriod)).append("\n");
    sb.append("    removeDuplicateSubjects: ").append(toIndentedString(removeDuplicateSubjects)).append("\n");
    sb.append("    removeSubjectsWithPriorOutcome: ").append(toIndentedString(removeSubjectsWithPriorOutcome)).append("\n");
    sb.append("    priorOutcomeLookback: ").append(toIndentedString(priorOutcomeLookback)).append("\n");
    sb.append("    minDaysAtRisk: ").append(toIndentedString(minDaysAtRisk)).append("\n");
    sb.append("    riskWindowStart: ").append(toIndentedString(riskWindowStart)).append("\n");
    sb.append("    addExposureDaysToStart: ").append(toIndentedString(addExposureDaysToStart)).append("\n");
    sb.append("    riskWindowEnd: ").append(toIndentedString(riskWindowEnd)).append("\n");
    sb.append("    addExposureDaysToEnd: ").append(toIndentedString(addExposureDaysToEnd)).append("\n");
    sb.append("    censorAtNewRiskWindow: ").append(toIndentedString(censorAtNewRiskWindow)).append("\n");
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
