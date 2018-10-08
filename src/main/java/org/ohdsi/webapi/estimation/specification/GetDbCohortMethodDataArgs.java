package org.ohdsi.webapi.estimation.specification;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettings;
import java.util.Date;

/**
 * Create a parameter object for the function getDbCohortMethodData 
 */
public class GetDbCohortMethodDataArgs {
  @JsonProperty("studyStartDate")
  private Date studyStartDate = null;

  @JsonProperty("studyEndDate")
  private Date studyEndDate = null;

  @JsonProperty("excludeDrugsFromCovariates")
  private Boolean excludeDrugsFromCovariates = true;

  @JsonProperty("firstExposureOnly")
  private Boolean firstExposureOnly = false;

  /**
   * Remove subjects that are in both the target and comparator cohort? Note that this is typically done in the createStudyPopulation function, but can already be done here for efficiency reasons. 
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

  @JsonProperty("restrictToCommonPeriod")
  private Boolean restrictToCommonPeriod = false;

  @JsonProperty("washoutPeriod")
  private Integer washoutPeriod = 0;

  @JsonProperty("maxCohortSize")
  private Integer maxCohortSize = 0;

  @JsonProperty("covariateSettings")
  private CovariateSettings covariateSettings = null;

  @JsonProperty("attr_class")
  private String attrClass = "args";

  public GetDbCohortMethodDataArgs studyStartDate(Date studyStartDate) {
    this.studyStartDate = studyStartDate;
    return this;
  }

  /**
   * A calendar date specifying the minimum date that a cohort index date can appear. Date format is &#x27;yyyymmdd&#x27;. 
   * @return studyStartDate
   **/
  @JsonProperty("studyStartDate")
  public Date getStudyStartDate() {
    return studyStartDate;
  }

  public void setStudyStartDate(Date studyStartDate) {
    this.studyStartDate = studyStartDate;
  }

  public GetDbCohortMethodDataArgs studyEndDate(Date studyEndDate) {
    this.studyEndDate = studyEndDate;
    return this;
  }

  /**
   * A calendar date specifying the maximum date that a cohort index date can appear. Date format is &#x27;yyyymmdd&#x27;. Important - the study end date is also used to truncate risk windows, meaning no outcomes beyond the study end date will be considered. 
   * @return studyEndDate
   **/
  @JsonProperty("studyEndDate")
  public Date getStudyEndDate() {
    return studyEndDate;
  }

  public void setStudyEndDate(Date studyEndDate) {
    this.studyEndDate = studyEndDate;
  }

  public GetDbCohortMethodDataArgs excludeDrugsFromCovariates(Boolean excludeDrugsFromCovariates) {
    this.excludeDrugsFromCovariates = excludeDrugsFromCovariates;
    return this;
  }

  /**
   * Should the target and comparator drugs (and their descendant concepts) be excluded from the covariates? Note that this will work if the drugs are actualy drug concept IDs (and not cohort IDs). 
   * @return excludeDrugsFromCovariates
   **/
  @JsonProperty("excludeDrugsFromCovariates")
  public Boolean isisExcludeDrugsFromCovariates() {
    return excludeDrugsFromCovariates;
  }

  public void setExcludeDrugsFromCovariates(Boolean excludeDrugsFromCovariates) {
    this.excludeDrugsFromCovariates = excludeDrugsFromCovariates;
  }

  public GetDbCohortMethodDataArgs firstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
    return this;
  }

  /**
   * Should only the first exposure per subject be included? Note that this is typically done in the createStudyPopulation function, but can already be done here for efficiency reasons. 
   * @return firstExposureOnly
   **/
  @JsonProperty("firstExposureOnly")
  public Boolean isisFirstExposureOnly() {
    return firstExposureOnly;
  }

  public void setFirstExposureOnly(Boolean firstExposureOnly) {
    this.firstExposureOnly = firstExposureOnly;
  }

  public GetDbCohortMethodDataArgs removeDuplicateSubjects(RemoveDuplicateSubjectsEnum removeDuplicateSubjects) {
    this.removeDuplicateSubjects = removeDuplicateSubjects;
    return this;
  }

  /**
   * Remove subjects that are in both the target and comparator cohort? Note that this is typically done in the createStudyPopulation function, but can already be done here for efficiency reasons. 
   * @return removeDuplicateSubjects
   **/
  @JsonProperty("removeDuplicateSubjects")
  public RemoveDuplicateSubjectsEnum getRemoveDuplicateSubjects() {
    return removeDuplicateSubjects;
  }

  public void setRemoveDuplicateSubjects(RemoveDuplicateSubjectsEnum removeDuplicateSubjects) {
    this.removeDuplicateSubjects = removeDuplicateSubjects;
  }

  public GetDbCohortMethodDataArgs restrictToCommonPeriod(Boolean restrictToCommonPeriod) {
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

  public GetDbCohortMethodDataArgs washoutPeriod(Integer washoutPeriod) {
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

  public GetDbCohortMethodDataArgs maxCohortSize(Integer maxCohortSize) {
    this.maxCohortSize = maxCohortSize;
    return this;
  }

  /**
   * If either the target or the comparator cohort is larger than this number it will be sampled to this size. maxCohortSize &#x3D; 0 indicates no maximum size. 
   * @return maxCohortSize
   **/
  @JsonProperty("maxCohortSize")
  public Integer getMaxCohortSize() {
    return maxCohortSize;
  }

  public void setMaxCohortSize(Integer maxCohortSize) {
    this.maxCohortSize = maxCohortSize;
  }

  public GetDbCohortMethodDataArgs covariateSettings(CovariateSettings covariateSettings) {
    this.covariateSettings = covariateSettings;
    return this;
  }

  /**
   * Get covariateSettings
   * @return covariateSettings
   **/
  @JsonProperty("covariateSettings")
  public CovariateSettings getCovariateSettings() {
    return covariateSettings;
  }

  public void setCovariateSettings(CovariateSettings covariateSettings) {
    this.covariateSettings = covariateSettings;
  }

  public GetDbCohortMethodDataArgs attrClass(String attrClass) {
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
    GetDbCohortMethodDataArgs getDbCohortMethodDataArgs = (GetDbCohortMethodDataArgs) o;
    return Objects.equals(this.studyStartDate, getDbCohortMethodDataArgs.studyStartDate) &&
        Objects.equals(this.studyEndDate, getDbCohortMethodDataArgs.studyEndDate) &&
        Objects.equals(this.excludeDrugsFromCovariates, getDbCohortMethodDataArgs.excludeDrugsFromCovariates) &&
        Objects.equals(this.firstExposureOnly, getDbCohortMethodDataArgs.firstExposureOnly) &&
        Objects.equals(this.removeDuplicateSubjects, getDbCohortMethodDataArgs.removeDuplicateSubjects) &&
        Objects.equals(this.restrictToCommonPeriod, getDbCohortMethodDataArgs.restrictToCommonPeriod) &&
        Objects.equals(this.washoutPeriod, getDbCohortMethodDataArgs.washoutPeriod) &&
        Objects.equals(this.maxCohortSize, getDbCohortMethodDataArgs.maxCohortSize) &&
        Objects.equals(this.covariateSettings, getDbCohortMethodDataArgs.covariateSettings) &&
        Objects.equals(this.attrClass, getDbCohortMethodDataArgs.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(studyStartDate, studyEndDate, excludeDrugsFromCovariates, firstExposureOnly, removeDuplicateSubjects, restrictToCommonPeriod, washoutPeriod, maxCohortSize, covariateSettings, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetDbCohortMethodDataArgs {\n");
    
    sb.append("    studyStartDate: ").append(toIndentedString(studyStartDate)).append("\n");
    sb.append("    studyEndDate: ").append(toIndentedString(studyEndDate)).append("\n");
    sb.append("    excludeDrugsFromCovariates: ").append(toIndentedString(excludeDrugsFromCovariates)).append("\n");
    sb.append("    firstExposureOnly: ").append(toIndentedString(firstExposureOnly)).append("\n");
    sb.append("    removeDuplicateSubjects: ").append(toIndentedString(removeDuplicateSubjects)).append("\n");
    sb.append("    restrictToCommonPeriod: ").append(toIndentedString(restrictToCommonPeriod)).append("\n");
    sb.append("    washoutPeriod: ").append(toIndentedString(washoutPeriod)).append("\n");
    sb.append("    maxCohortSize: ").append(toIndentedString(maxCohortSize)).append("\n");
    sb.append("    covariateSettings: ").append(toIndentedString(covariateSettings)).append("\n");
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
