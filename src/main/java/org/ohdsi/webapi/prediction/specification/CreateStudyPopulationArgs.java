package org.ohdsi.webapi.prediction.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Create a parameter object for the function createStudyPopulation 
 */
public class CreateStudyPopulationArgs   {
  @JsonProperty("binary")
  private Boolean binary = true;

  @JsonProperty("includeAllOutcomes")
  private Boolean includeAllOutcomes = true;

  @JsonProperty("firstExposureOnly")
  private Boolean firstExposureOnly = false;

  @JsonProperty("washoutPeriod")
  private Integer washoutPeriod = 0;

  @JsonProperty("removeSubjectsWithPriorOutcome")
  private Boolean removeSubjectsWithPriorOutcome = false;

  @JsonProperty("priorOutcomeLookback")
  private Integer priorOutcomeLookback = 99999;

  @JsonProperty("requireTimeAtRisk")
  private Boolean requireTimeAtRisk = true;

  @JsonProperty("minTimeAtRisk")
  private Integer minTimeAtRisk = 365;

  @JsonProperty("riskWindowStart")
  private Integer riskWindowStart = 0;

  @JsonProperty("addExposureDaysToStart")
  private Boolean addExposureDaysToStart = false;

  @JsonProperty("riskWindowEnd")
  private Integer riskWindowEnd = 365;

  @JsonProperty("addExposureDaysToEnd")
  private Boolean addExposureDaysToEnd = true;
  
  @JsonProperty("attr_class")
  private String attrClass = "populationSettings";

  public CreateStudyPopulationArgs binary(Boolean binary) {
    this.binary = binary;
    return this;
  }

  /**
   * Forces the outcomeCount to be 0 or 1 (use for binary prediction problems) 
   * @return binary
   **/
  @JsonProperty("binary")
  public Boolean isisBinary() {
    return binary;
  }

  public void setBinary(Boolean binary) {
    this.binary = binary;
  }

  public CreateStudyPopulationArgs includeAllOutcomes(Boolean includeAllOutcomes) {
    this.includeAllOutcomes = includeAllOutcomes;
    return this;
  }

  /**
   * (binary) indicating whether to include people with outcomes who are not observed for the whole at risk period 
   * @return includeAllOutcomes
   **/
  @JsonProperty("includeAllOutcomes")
  public Boolean isisIncludeAllOutcomes() {
    return includeAllOutcomes;
  }

  public void setIncludeAllOutcomes(Boolean includeAllOutcomes) {
    this.includeAllOutcomes = includeAllOutcomes;
  }

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

  public CreateStudyPopulationArgs requireTimeAtRisk(Boolean requireTimeAtRisk) {
    this.requireTimeAtRisk = requireTimeAtRisk;
    return this;
  }

  /**
   * Should subjects without time at risk be removed? 
   * @return requireTimeAtRisk
   **/
  @JsonProperty("requireTimeAtRisk")
  public Boolean isisRequireTimeAtRisk() {
    return requireTimeAtRisk;
  }

  public void setRequireTimeAtRisk(Boolean requireTimeAtRisk) {
    this.requireTimeAtRisk = requireTimeAtRisk;
  }

  public CreateStudyPopulationArgs minTimeAtRisk(Integer minTimeAtRisk) {
    this.minTimeAtRisk = minTimeAtRisk;
    return this;
  }

  /**
   * The miminum time at risk in days 
   * @return minTimeAtRisk
   **/
  @JsonProperty("minTimeAtRisk")
  public Integer getMinTimeAtRisk() {
    return minTimeAtRisk;
  }

  public void setMinTimeAtRisk(Integer minTimeAtRisk) {
    this.minTimeAtRisk = minTimeAtRisk;
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
    return Objects.equals(this.binary, createStudyPopulationArgs.binary) &&
        Objects.equals(this.includeAllOutcomes, createStudyPopulationArgs.includeAllOutcomes) &&
        Objects.equals(this.firstExposureOnly, createStudyPopulationArgs.firstExposureOnly) &&
        Objects.equals(this.washoutPeriod, createStudyPopulationArgs.washoutPeriod) &&
        Objects.equals(this.removeSubjectsWithPriorOutcome, createStudyPopulationArgs.removeSubjectsWithPriorOutcome) &&
        Objects.equals(this.priorOutcomeLookback, createStudyPopulationArgs.priorOutcomeLookback) &&
        Objects.equals(this.requireTimeAtRisk, createStudyPopulationArgs.requireTimeAtRisk) &&
        Objects.equals(this.minTimeAtRisk, createStudyPopulationArgs.minTimeAtRisk) &&
        Objects.equals(this.riskWindowStart, createStudyPopulationArgs.riskWindowStart) &&
        Objects.equals(this.addExposureDaysToStart, createStudyPopulationArgs.addExposureDaysToStart) &&
        Objects.equals(this.riskWindowEnd, createStudyPopulationArgs.riskWindowEnd) &&
        Objects.equals(this.addExposureDaysToEnd, createStudyPopulationArgs.addExposureDaysToEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(binary, includeAllOutcomes, firstExposureOnly, washoutPeriod, removeSubjectsWithPriorOutcome, priorOutcomeLookback, requireTimeAtRisk, minTimeAtRisk, riskWindowStart, addExposureDaysToStart, riskWindowEnd, addExposureDaysToEnd);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateStudyPopulationArgs {\n");
    
    sb.append("    binary: ").append(toIndentedString(binary)).append("\n");
    sb.append("    includeAllOutcomes: ").append(toIndentedString(includeAllOutcomes)).append("\n");
    sb.append("    firstExposureOnly: ").append(toIndentedString(firstExposureOnly)).append("\n");
    sb.append("    washoutPeriod: ").append(toIndentedString(washoutPeriod)).append("\n");
    sb.append("    removeSubjectsWithPriorOutcome: ").append(toIndentedString(removeSubjectsWithPriorOutcome)).append("\n");
    sb.append("    priorOutcomeLookback: ").append(toIndentedString(priorOutcomeLookback)).append("\n");
    sb.append("    requireTimeAtRisk: ").append(toIndentedString(requireTimeAtRisk)).append("\n");
    sb.append("    minTimeAtRisk: ").append(toIndentedString(minTimeAtRisk)).append("\n");
    sb.append("    riskWindowStart: ").append(toIndentedString(riskWindowStart)).append("\n");
    sb.append("    addExposureDaysToStart: ").append(toIndentedString(addExposureDaysToStart)).append("\n");
    sb.append("    riskWindowEnd: ").append(toIndentedString(riskWindowEnd)).append("\n");
    sb.append("    addExposureDaysToEnd: ").append(toIndentedString(addExposureDaysToEnd)).append("\n");
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
