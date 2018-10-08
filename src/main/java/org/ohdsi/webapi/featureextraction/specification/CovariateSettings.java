package org.ohdsi.webapi.featureextraction.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class CovariateSettings {
  @JsonProperty("temporal")
  private Boolean temporal = false;

  @JsonProperty("DemographicsGender")
  private Boolean demographicsGender = false;

  @JsonProperty("DemographicsAge")
  private Boolean demographicsAge = false;

  @JsonProperty("DemographicsAgeGroup")
  private Boolean demographicsAgeGroup = false;

  @JsonProperty("DemographicsRace")
  private Boolean demographicsRace = false;

  @JsonProperty("DemographicsEthnicity")
  private Boolean demographicsEthnicity = false;

  @JsonProperty("DemographicsIndexYear")
  private Boolean demographicsIndexYear = false;

  @JsonProperty("DemographicsIndexMonth")
  private Boolean demographicsIndexMonth = false;

  @JsonProperty("DemographicsPriorObservationTime")
  private Boolean demographicsPriorObservationTime = false;

  @JsonProperty("DemographicsPostObservationTime")
  private Boolean demographicsPostObservationTime = false;

  @JsonProperty("DemographicsTimeInCohort")
  private Boolean demographicsTimeInCohort = false;

  @JsonProperty("DemographicsIndexYearMonth")
  private Boolean demographicsIndexYearMonth = false;

  @JsonProperty("ConditionOccurrenceAnyTimePrior")
  private Boolean conditionOccurrenceAnyTimePrior = false;

  @JsonProperty("ConditionOccurrenceLongTerm")
  private Boolean conditionOccurrenceLongTerm = false;

  @JsonProperty("ConditionOccurrenceMediumTerm")
  private Boolean conditionOccurrenceMediumTerm = false;

  @JsonProperty("ConditionOccurrenceShortTerm")
  private Boolean conditionOccurrenceShortTerm = false;

  @JsonProperty("ConditionOccurrencePrimaryInpatientAnyTimePrior")
  private Boolean conditionOccurrencePrimaryInpatientAnyTimePrior = false;

  @JsonProperty("ConditionOccurrencePrimaryInpatientLongTerm")
  private Boolean conditionOccurrencePrimaryInpatientLongTerm = false;

  @JsonProperty("ConditionOccurrencePrimaryInpatientMediumTerm")
  private Boolean conditionOccurrencePrimaryInpatientMediumTerm = false;

  @JsonProperty("ConditionOccurrencePrimaryInpatientShortTerm")
  private Boolean conditionOccurrencePrimaryInpatientShortTerm = false;

  @JsonProperty("ConditionEraAnyTimePrior")
  private Boolean conditionEraAnyTimePrior = false;

  @JsonProperty("ConditionEraLongTerm")
  private Boolean conditionEraLongTerm = false;

  @JsonProperty("ConditionEraMediumTerm")
  private Boolean conditionEraMediumTerm = false;

  @JsonProperty("ConditionEraShortTerm")
  private Boolean conditionEraShortTerm = false;

  @JsonProperty("ConditionEraOverlapping")
  private Boolean conditionEraOverlapping = false;

  @JsonProperty("ConditionEraStartLongTerm")
  private Boolean conditionEraStartLongTerm = false;

  @JsonProperty("ConditionEraStartMediumTerm")
  private Boolean conditionEraStartMediumTerm = false;

  @JsonProperty("ConditionEraStartShortTerm")
  private Boolean conditionEraStartShortTerm = false;

  @JsonProperty("ConditionGroupEraAnyTimePrior")
  private Boolean conditionGroupEraAnyTimePrior = false;

  @JsonProperty("ConditionGroupEraLongTerm")
  private Boolean conditionGroupEraLongTerm = false;

  @JsonProperty("ConditionGroupEraMediumTerm")
  private Boolean conditionGroupEraMediumTerm = false;

  @JsonProperty("ConditionGroupEraShortTerm")
  private Boolean conditionGroupEraShortTerm = false;

  @JsonProperty("ConditionGroupEraOverlapping")
  private Boolean conditionGroupEraOverlapping = false;

  @JsonProperty("ConditionGroupEraStartLongTerm")
  private Boolean conditionGroupEraStartLongTerm = false;

  @JsonProperty("ConditionGroupEraStartMediumTerm")
  private Boolean conditionGroupEraStartMediumTerm = false;

  @JsonProperty("ConditionGroupEraStartShortTerm")
  private Boolean conditionGroupEraStartShortTerm = false;

  @JsonProperty("DrugExposureAnyTimePrior")
  private Boolean drugExposureAnyTimePrior = false;

  @JsonProperty("DrugExposureLongTerm")
  private Boolean drugExposureLongTerm = false;

  @JsonProperty("DrugExposureMediumTerm")
  private Boolean drugExposureMediumTerm = false;

  @JsonProperty("DrugExposureShortTerm")
  private Boolean drugExposureShortTerm = false;

  @JsonProperty("DrugEraAnyTimePrior")
  private Boolean drugEraAnyTimePrior = false;

  @JsonProperty("DrugEraLongTerm")
  private Boolean drugEraLongTerm = false;

  @JsonProperty("DrugEraMediumTerm")
  private Boolean drugEraMediumTerm = false;

  @JsonProperty("DrugEraShortTerm")
  private Boolean drugEraShortTerm = false;

  @JsonProperty("DrugEraOverlapping")
  private Boolean drugEraOverlapping = false;

  @JsonProperty("DrugEraStartLongTerm")
  private Boolean drugEraStartLongTerm = false;

  @JsonProperty("DrugEraStartMediumTerm")
  private Boolean drugEraStartMediumTerm = false;

  @JsonProperty("DrugEraStartShortTerm")
  private Boolean drugEraStartShortTerm = false;

  @JsonProperty("DrugGroupEraAnyTimePrior")
  private Boolean drugGroupEraAnyTimePrior = false;

  @JsonProperty("DrugGroupEraLongTerm")
  private Boolean drugGroupEraLongTerm = false;

  @JsonProperty("DrugGroupEraMediumTerm")
  private Boolean drugGroupEraMediumTerm = false;

  @JsonProperty("DrugGroupEraShortTerm")
  private Boolean drugGroupEraShortTerm = false;

  @JsonProperty("DrugGroupEraOverlapping")
  private Boolean drugGroupEraOverlapping = false;

  @JsonProperty("DrugGroupEraStartLongTerm")
  private Boolean drugGroupEraStartLongTerm = false;

  @JsonProperty("DrugGroupEraStartMediumTerm")
  private Boolean drugGroupEraStartMediumTerm = false;

  @JsonProperty("DrugGroupEraStartShortTerm")
  private Boolean drugGroupEraStartShortTerm = false;

  @JsonProperty("ProcedureOccurrenceAnyTimePrior")
  private Boolean procedureOccurrenceAnyTimePrior = false;

  @JsonProperty("ProcedureOccurrenceLongTerm")
  private Boolean procedureOccurrenceLongTerm = false;

  @JsonProperty("ProcedureOccurrenceMediumTerm")
  private Boolean procedureOccurrenceMediumTerm = false;

  @JsonProperty("ProcedureOccurrenceShortTerm")
  private Boolean procedureOccurrenceShortTerm = false;

  @JsonProperty("DeviceExposureAnyTimePrior")
  private Boolean deviceExposureAnyTimePrior = false;

  @JsonProperty("DeviceExposureLongTerm")
  private Boolean deviceExposureLongTerm = false;

  @JsonProperty("DeviceExposureMediumTerm")
  private Boolean deviceExposureMediumTerm = false;

  @JsonProperty("DeviceExposureShortTerm")
  private Boolean deviceExposureShortTerm = false;

  @JsonProperty("MeasurementAnyTimePrior")
  private Boolean measurementAnyTimePrior = false;

  @JsonProperty("MeasurementLongTerm")
  private Boolean measurementLongTerm = false;

  @JsonProperty("MeasurementMediumTerm")
  private Boolean measurementMediumTerm = false;

  @JsonProperty("MeasurementShortTerm")
  private Boolean measurementShortTerm = false;

  @JsonProperty("MeasurementValueAnyTimePrior")
  private Boolean measurementValueAnyTimePrior = false;

  @JsonProperty("MeasurementValueLongTerm")
  private Boolean measurementValueLongTerm = false;

  @JsonProperty("MeasurementValueMediumTerm")
  private Boolean measurementValueMediumTerm = false;

  @JsonProperty("MeasurementValueShortTerm")
  private Boolean measurementValueShortTerm = false;

  @JsonProperty("MeasurementRangeGroupAnyTimePrior")
  private Boolean measurementRangeGroupAnyTimePrior = false;

  @JsonProperty("MeasurementRangeGroupLongTerm")
  private Boolean measurementRangeGroupLongTerm = false;

  @JsonProperty("MeasurementRangeGroupMediumTerm")
  private Boolean measurementRangeGroupMediumTerm = false;

  @JsonProperty("MeasurementRangeGroupShortTerm")
  private Boolean measurementRangeGroupShortTerm = false;

  @JsonProperty("ObservationAnyTimePrior")
  private Boolean observationAnyTimePrior = false;

  @JsonProperty("ObservationLongTerm")
  private Boolean observationLongTerm = false;

  @JsonProperty("ObservationMediumTerm")
  private Boolean observationMediumTerm = false;

  @JsonProperty("ObservationShortTerm")
  private Boolean observationShortTerm = false;

  @JsonProperty("CharlsonIndex")
  private Boolean charlsonIndex = false;

  @JsonProperty("Dcsi")
  private Boolean dcsi = false;

  @JsonProperty("Chads2")
  private Boolean chads2 = false;

  @JsonProperty("Chads2Vasc")
  private Boolean chads2Vasc = false;

  @JsonProperty("DistinctConditionCountLongTerm")
  private Boolean distinctConditionCountLongTerm = false;

  @JsonProperty("DistinctConditionCountMediumTerm")
  private Boolean distinctConditionCountMediumTerm = false;

  @JsonProperty("DistinctConditionCountShortTerm")
  private Boolean distinctConditionCountShortTerm = false;

  @JsonProperty("DistinctIngredientCountLongTerm")
  private Boolean distinctIngredientCountLongTerm = false;

  @JsonProperty("DistinctIngredientCountMediumTerm")
  private Boolean distinctIngredientCountMediumTerm = false;

  @JsonProperty("DistinctIngredientCountShortTerm")
  private Boolean distinctIngredientCountShortTerm = false;

  @JsonProperty("DistinctProcedureCountLongTerm")
  private Boolean distinctProcedureCountLongTerm = false;

  @JsonProperty("DistinctProcedureCountMediumTerm")
  private Boolean distinctProcedureCountMediumTerm = false;

  @JsonProperty("DistinctProcedureCountShortTerm")
  private Boolean distinctProcedureCountShortTerm = false;

  @JsonProperty("DistinctMeasurementCountLongTerm")
  private Boolean distinctMeasurementCountLongTerm = false;

  @JsonProperty("DistinctMeasurementCountMediumTerm")
  private Boolean distinctMeasurementCountMediumTerm = false;

  @JsonProperty("DistinctMeasurementCountShortTerm")
  private Boolean distinctMeasurementCountShortTerm = false;

  @JsonProperty("DistinctObservationCountLongTerm")
  private Boolean distinctObservationCountLongTerm = false;

  @JsonProperty("DistinctObservationCountMediumTerm")
  private Boolean distinctObservationCountMediumTerm = false;

  @JsonProperty("DistinctObservationCountShortTerm")
  private Boolean distinctObservationCountShortTerm = false;

  @JsonProperty("VisitCountLongTerm")
  private Boolean visitCountLongTerm = false;

  @JsonProperty("VisitCountMediumTerm")
  private Boolean visitCountMediumTerm = false;

  @JsonProperty("VisitCountShortTerm")
  private Boolean visitCountShortTerm = false;

  @JsonProperty("VisitConceptCountLongTerm")
  private Boolean visitConceptCountLongTerm = false;

  @JsonProperty("VisitConceptCountMediumTerm")
  private Boolean visitConceptCountMediumTerm = false;

  @JsonProperty("VisitConceptCountShortTerm")
  private Boolean visitConceptCountShortTerm = false;

  @JsonProperty("longTermStartDays")
  private Integer longTermStartDays = -365;

  @JsonProperty("mediumTermStartDays")
  private Integer mediumTermStartDays = -180;

  @JsonProperty("shortTermStartDays")
  private Integer shortTermStartDays = -30;

  @JsonProperty("endDays")
  private Integer endDays = 0;

  @JsonProperty("includedCovariateConceptIds")
  private List<Long> includedCovariateConceptIds = null;

  @JsonProperty("addDescendantsToInclude")
  private Boolean addDescendantsToInclude = false;

  @JsonProperty("excludedCovariateConceptIds")
  private List<Long> excludedCovariateConceptIds = null;

  @JsonProperty("addDescendantsToExclude")
  private Boolean addDescendantsToExclude = false;

  @JsonProperty("includedCovariateIds")
  private List<Integer> includedCovariateIds = null;

  @JsonProperty("attr_fun")
  private String attrFun = "getDbDefaultCovariateData";

  @JsonProperty("attr_class")
  private String attrClass = "covariateSettings";

  public CovariateSettings temporal(Boolean temporal) {
    this.temporal = temporal;
    return this;
  }

  /**
   * Construct temporal covariates  
   * @return temporal
   **/
  @JsonProperty("temporal")
  public Boolean isisTemporal() {
    return temporal;
  }

  public void setTemporal(Boolean temporal) {
    this.temporal = temporal;
  }

  public CovariateSettings demographicsGender(Boolean demographicsGender) {
    this.demographicsGender = demographicsGender;
    return this;
  }

  /**
   * Gender of the subject. (analysis ID 1) 
   * @return demographicsGender
   **/
  @JsonProperty("DemographicsGender")
  public Boolean isisDemographicsGender() {
    return demographicsGender;
  }

  public void setDemographicsGender(Boolean demographicsGender) {
    this.demographicsGender = demographicsGender;
  }

  public CovariateSettings demographicsAge(Boolean demographicsAge) {
    this.demographicsAge = demographicsAge;
    return this;
  }

  /**
   * Age of the subject on the index date (in years). (analysis ID 2) 
   * @return demographicsAge
   **/
  @JsonProperty("DemographicsAge")
  public Boolean isisDemographicsAge() {
    return demographicsAge;
  }

  public void setDemographicsAge(Boolean demographicsAge) {
    this.demographicsAge = demographicsAge;
  }

  public CovariateSettings demographicsAgeGroup(Boolean demographicsAgeGroup) {
    this.demographicsAgeGroup = demographicsAgeGroup;
    return this;
  }

  /**
   * Age of the subject on the index date (in 5 year age groups) (analysis ID 3) 
   * @return demographicsAgeGroup
   **/
  @JsonProperty("DemographicsAgeGroup")
  public Boolean isisDemographicsAgeGroup() {
    return demographicsAgeGroup;
  }

  public void setDemographicsAgeGroup(Boolean demographicsAgeGroup) {
    this.demographicsAgeGroup = demographicsAgeGroup;
  }

  public CovariateSettings demographicsRace(Boolean demographicsRace) {
    this.demographicsRace = demographicsRace;
    return this;
  }

  /**
   * Race of the subject. (analysis ID 4) 
   * @return demographicsRace
   **/
  @JsonProperty("DemographicsRace")
  public Boolean isisDemographicsRace() {
    return demographicsRace;
  }

  public void setDemographicsRace(Boolean demographicsRace) {
    this.demographicsRace = demographicsRace;
  }

  public CovariateSettings demographicsEthnicity(Boolean demographicsEthnicity) {
    this.demographicsEthnicity = demographicsEthnicity;
    return this;
  }

  /**
   * Ethnicity of the subject. (analysis ID 5) 
   * @return demographicsEthnicity
   **/
  @JsonProperty("DemographicsEthnicity")
  public Boolean isisDemographicsEthnicity() {
    return demographicsEthnicity;
  }

  public void setDemographicsEthnicity(Boolean demographicsEthnicity) {
    this.demographicsEthnicity = demographicsEthnicity;
  }

  public CovariateSettings demographicsIndexYear(Boolean demographicsIndexYear) {
    this.demographicsIndexYear = demographicsIndexYear;
    return this;
  }

  /**
   * Year of the index date. (analysis ID 6) 
   * @return demographicsIndexYear
   **/
  @JsonProperty("DemographicsIndexYear")
  public Boolean isisDemographicsIndexYear() {
    return demographicsIndexYear;
  }

  public void setDemographicsIndexYear(Boolean demographicsIndexYear) {
    this.demographicsIndexYear = demographicsIndexYear;
  }

  public CovariateSettings demographicsIndexMonth(Boolean demographicsIndexMonth) {
    this.demographicsIndexMonth = demographicsIndexMonth;
    return this;
  }

  /**
   * Month of the index date. (analysis ID 7) 
   * @return demographicsIndexMonth
   **/
  @JsonProperty("DemographicsIndexMonth")
  public Boolean isisDemographicsIndexMonth() {
    return demographicsIndexMonth;
  }

  public void setDemographicsIndexMonth(Boolean demographicsIndexMonth) {
    this.demographicsIndexMonth = demographicsIndexMonth;
  }

  public CovariateSettings demographicsPriorObservationTime(Boolean demographicsPriorObservationTime) {
    this.demographicsPriorObservationTime = demographicsPriorObservationTime;
    return this;
  }

  /**
   * Number of continuous days of observation time preceding the index date. (analysis ID 8) 
   * @return demographicsPriorObservationTime
   **/
  @JsonProperty("DemographicsPriorObservationTime")
  public Boolean isisDemographicsPriorObservationTime() {
    return demographicsPriorObservationTime;
  }

  public void setDemographicsPriorObservationTime(Boolean demographicsPriorObservationTime) {
    this.demographicsPriorObservationTime = demographicsPriorObservationTime;
  }

  public CovariateSettings demographicsPostObservationTime(Boolean demographicsPostObservationTime) {
    this.demographicsPostObservationTime = demographicsPostObservationTime;
    return this;
  }

  /**
   * Number of continuous days of observation time following the index date. (analysis ID 9) 
   * @return demographicsPostObservationTime
   **/
  @JsonProperty("DemographicsPostObservationTime")
  public Boolean isisDemographicsPostObservationTime() {
    return demographicsPostObservationTime;
  }

  public void setDemographicsPostObservationTime(Boolean demographicsPostObservationTime) {
    this.demographicsPostObservationTime = demographicsPostObservationTime;
  }

  public CovariateSettings demographicsTimeInCohort(Boolean demographicsTimeInCohort) {
    this.demographicsTimeInCohort = demographicsTimeInCohort;
    return this;
  }

  /**
   * Number of days of observation time during cohort period. (analysis ID 10) 
   * @return demographicsTimeInCohort
   **/
  @JsonProperty("DemographicsTimeInCohort")
  public Boolean isisDemographicsTimeInCohort() {
    return demographicsTimeInCohort;
  }

  public void setDemographicsTimeInCohort(Boolean demographicsTimeInCohort) {
    this.demographicsTimeInCohort = demographicsTimeInCohort;
  }

  public CovariateSettings demographicsIndexYearMonth(Boolean demographicsIndexYearMonth) {
    this.demographicsIndexYearMonth = demographicsIndexYearMonth;
    return this;
  }

  /**
   * Both calendar year and month of the index date in a single variable. (analysis ID 11) 
   * @return demographicsIndexYearMonth
   **/
  @JsonProperty("DemographicsIndexYearMonth")
  public Boolean isisDemographicsIndexYearMonth() {
    return demographicsIndexYearMonth;
  }

  public void setDemographicsIndexYearMonth(Boolean demographicsIndexYearMonth) {
    this.demographicsIndexYearMonth = demographicsIndexYearMonth;
  }

  public CovariateSettings conditionOccurrenceAnyTimePrior(Boolean conditionOccurrenceAnyTimePrior) {
    this.conditionOccurrenceAnyTimePrior = conditionOccurrenceAnyTimePrior;
    return this;
  }

  /**
   * One covariate per condition in the condition_occurrence table starting any time prior to index. (analysis ID 101) 
   * @return conditionOccurrenceAnyTimePrior
   **/
  @JsonProperty("ConditionOccurrenceAnyTimePrior")
  public Boolean isisConditionOccurrenceAnyTimePrior() {
    return conditionOccurrenceAnyTimePrior;
  }

  public void setConditionOccurrenceAnyTimePrior(Boolean conditionOccurrenceAnyTimePrior) {
    this.conditionOccurrenceAnyTimePrior = conditionOccurrenceAnyTimePrior;
  }

  public CovariateSettings conditionOccurrenceLongTerm(Boolean conditionOccurrenceLongTerm) {
    this.conditionOccurrenceLongTerm = conditionOccurrenceLongTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_occurrence table starting in the long term window. (analysis ID 102) 
   * @return conditionOccurrenceLongTerm
   **/
  @JsonProperty("ConditionOccurrenceLongTerm")
  public Boolean isisConditionOccurrenceLongTerm() {
    return conditionOccurrenceLongTerm;
  }

  public void setConditionOccurrenceLongTerm(Boolean conditionOccurrenceLongTerm) {
    this.conditionOccurrenceLongTerm = conditionOccurrenceLongTerm;
  }

  public CovariateSettings conditionOccurrenceMediumTerm(Boolean conditionOccurrenceMediumTerm) {
    this.conditionOccurrenceMediumTerm = conditionOccurrenceMediumTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_occurrence table starting in the medium term window. (analysis ID 103) 
   * @return conditionOccurrenceMediumTerm
   **/
  @JsonProperty("ConditionOccurrenceMediumTerm")
  public Boolean isisConditionOccurrenceMediumTerm() {
    return conditionOccurrenceMediumTerm;
  }

  public void setConditionOccurrenceMediumTerm(Boolean conditionOccurrenceMediumTerm) {
    this.conditionOccurrenceMediumTerm = conditionOccurrenceMediumTerm;
  }

  public CovariateSettings conditionOccurrenceShortTerm(Boolean conditionOccurrenceShortTerm) {
    this.conditionOccurrenceShortTerm = conditionOccurrenceShortTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_occurrence table starting in the short term window. (analysis ID 104) 
   * @return conditionOccurrenceShortTerm
   **/
  @JsonProperty("ConditionOccurrenceShortTerm")
  public Boolean isisConditionOccurrenceShortTerm() {
    return conditionOccurrenceShortTerm;
  }

  public void setConditionOccurrenceShortTerm(Boolean conditionOccurrenceShortTerm) {
    this.conditionOccurrenceShortTerm = conditionOccurrenceShortTerm;
  }

  public CovariateSettings conditionOccurrencePrimaryInpatientAnyTimePrior(Boolean conditionOccurrencePrimaryInpatientAnyTimePrior) {
    this.conditionOccurrencePrimaryInpatientAnyTimePrior = conditionOccurrencePrimaryInpatientAnyTimePrior;
    return this;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting any time prior to index. (analysis ID 105) 
   * @return conditionOccurrencePrimaryInpatientAnyTimePrior
   **/
  @JsonProperty("ConditionOccurrencePrimaryInpatientAnyTimePrior")
  public Boolean isisConditionOccurrencePrimaryInpatientAnyTimePrior() {
    return conditionOccurrencePrimaryInpatientAnyTimePrior;
  }

  public void setConditionOccurrencePrimaryInpatientAnyTimePrior(Boolean conditionOccurrencePrimaryInpatientAnyTimePrior) {
    this.conditionOccurrencePrimaryInpatientAnyTimePrior = conditionOccurrencePrimaryInpatientAnyTimePrior;
  }

  public CovariateSettings conditionOccurrencePrimaryInpatientLongTerm(Boolean conditionOccurrencePrimaryInpatientLongTerm) {
    this.conditionOccurrencePrimaryInpatientLongTerm = conditionOccurrencePrimaryInpatientLongTerm;
    return this;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting in the long term window. (analysis ID 106) 
   * @return conditionOccurrencePrimaryInpatientLongTerm
   **/
  @JsonProperty("ConditionOccurrencePrimaryInpatientLongTerm")
  public Boolean isisConditionOccurrencePrimaryInpatientLongTerm() {
    return conditionOccurrencePrimaryInpatientLongTerm;
  }

  public void setConditionOccurrencePrimaryInpatientLongTerm(Boolean conditionOccurrencePrimaryInpatientLongTerm) {
    this.conditionOccurrencePrimaryInpatientLongTerm = conditionOccurrencePrimaryInpatientLongTerm;
  }

  public CovariateSettings conditionOccurrencePrimaryInpatientMediumTerm(Boolean conditionOccurrencePrimaryInpatientMediumTerm) {
    this.conditionOccurrencePrimaryInpatientMediumTerm = conditionOccurrencePrimaryInpatientMediumTerm;
    return this;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting in the medium term window. (analysis ID 107) 
   * @return conditionOccurrencePrimaryInpatientMediumTerm
   **/
  @JsonProperty("ConditionOccurrencePrimaryInpatientMediumTerm")
  public Boolean isisConditionOccurrencePrimaryInpatientMediumTerm() {
    return conditionOccurrencePrimaryInpatientMediumTerm;
  }

  public void setConditionOccurrencePrimaryInpatientMediumTerm(Boolean conditionOccurrencePrimaryInpatientMediumTerm) {
    this.conditionOccurrencePrimaryInpatientMediumTerm = conditionOccurrencePrimaryInpatientMediumTerm;
  }

  public CovariateSettings conditionOccurrencePrimaryInpatientShortTerm(Boolean conditionOccurrencePrimaryInpatientShortTerm) {
    this.conditionOccurrencePrimaryInpatientShortTerm = conditionOccurrencePrimaryInpatientShortTerm;
    return this;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting in the short term window. (analysis ID 108) 
   * @return conditionOccurrencePrimaryInpatientShortTerm
   **/
  @JsonProperty("ConditionOccurrencePrimaryInpatientShortTerm")
  public Boolean isisConditionOccurrencePrimaryInpatientShortTerm() {
    return conditionOccurrencePrimaryInpatientShortTerm;
  }

  public void setConditionOccurrencePrimaryInpatientShortTerm(Boolean conditionOccurrencePrimaryInpatientShortTerm) {
    this.conditionOccurrencePrimaryInpatientShortTerm = conditionOccurrencePrimaryInpatientShortTerm;
  }

  public CovariateSettings conditionEraAnyTimePrior(Boolean conditionEraAnyTimePrior) {
    this.conditionEraAnyTimePrior = conditionEraAnyTimePrior;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any time prior to index. (analysis ID 201) 
   * @return conditionEraAnyTimePrior
   **/
  @JsonProperty("ConditionEraAnyTimePrior")
  public Boolean isisConditionEraAnyTimePrior() {
    return conditionEraAnyTimePrior;
  }

  public void setConditionEraAnyTimePrior(Boolean conditionEraAnyTimePrior) {
    this.conditionEraAnyTimePrior = conditionEraAnyTimePrior;
  }

  public CovariateSettings conditionEraLongTerm(Boolean conditionEraLongTerm) {
    this.conditionEraLongTerm = conditionEraLongTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any part of the long term window. (analysis ID 202) 
   * @return conditionEraLongTerm
   **/
  @JsonProperty("ConditionEraLongTerm")
  public Boolean isisConditionEraLongTerm() {
    return conditionEraLongTerm;
  }

  public void setConditionEraLongTerm(Boolean conditionEraLongTerm) {
    this.conditionEraLongTerm = conditionEraLongTerm;
  }

  public CovariateSettings conditionEraMediumTerm(Boolean conditionEraMediumTerm) {
    this.conditionEraMediumTerm = conditionEraMediumTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any part of the medium term window. (analysis ID 203) 
   * @return conditionEraMediumTerm
   **/
  @JsonProperty("ConditionEraMediumTerm")
  public Boolean isisConditionEraMediumTerm() {
    return conditionEraMediumTerm;
  }

  public void setConditionEraMediumTerm(Boolean conditionEraMediumTerm) {
    this.conditionEraMediumTerm = conditionEraMediumTerm;
  }

  public CovariateSettings conditionEraShortTerm(Boolean conditionEraShortTerm) {
    this.conditionEraShortTerm = conditionEraShortTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any part of the short term window. (analysis ID 204) 
   * @return conditionEraShortTerm
   **/
  @JsonProperty("ConditionEraShortTerm")
  public Boolean isisConditionEraShortTerm() {
    return conditionEraShortTerm;
  }

  public void setConditionEraShortTerm(Boolean conditionEraShortTerm) {
    this.conditionEraShortTerm = conditionEraShortTerm;
  }

  public CovariateSettings conditionEraOverlapping(Boolean conditionEraOverlapping) {
    this.conditionEraOverlapping = conditionEraOverlapping;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with the end of the risk window. (analysis ID 205) 
   * @return conditionEraOverlapping
   **/
  @JsonProperty("ConditionEraOverlapping")
  public Boolean isisConditionEraOverlapping() {
    return conditionEraOverlapping;
  }

  public void setConditionEraOverlapping(Boolean conditionEraOverlapping) {
    this.conditionEraOverlapping = conditionEraOverlapping;
  }

  public CovariateSettings conditionEraStartLongTerm(Boolean conditionEraStartLongTerm) {
    this.conditionEraStartLongTerm = conditionEraStartLongTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table starting in the long term window. (analysis ID 206) 
   * @return conditionEraStartLongTerm
   **/
  @JsonProperty("ConditionEraStartLongTerm")
  public Boolean isisConditionEraStartLongTerm() {
    return conditionEraStartLongTerm;
  }

  public void setConditionEraStartLongTerm(Boolean conditionEraStartLongTerm) {
    this.conditionEraStartLongTerm = conditionEraStartLongTerm;
  }

  public CovariateSettings conditionEraStartMediumTerm(Boolean conditionEraStartMediumTerm) {
    this.conditionEraStartMediumTerm = conditionEraStartMediumTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table starting in the medium term window. (analysis ID 207) 
   * @return conditionEraStartMediumTerm
   **/
  @JsonProperty("ConditionEraStartMediumTerm")
  public Boolean isisConditionEraStartMediumTerm() {
    return conditionEraStartMediumTerm;
  }

  public void setConditionEraStartMediumTerm(Boolean conditionEraStartMediumTerm) {
    this.conditionEraStartMediumTerm = conditionEraStartMediumTerm;
  }

  public CovariateSettings conditionEraStartShortTerm(Boolean conditionEraStartShortTerm) {
    this.conditionEraStartShortTerm = conditionEraStartShortTerm;
    return this;
  }

  /**
   * One covariate per condition in the condition_era table starting in the short term window. (analysis ID 208) 
   * @return conditionEraStartShortTerm
   **/
  @JsonProperty("ConditionEraStartShortTerm")
  public Boolean isisConditionEraStartShortTerm() {
    return conditionEraStartShortTerm;
  }

  public void setConditionEraStartShortTerm(Boolean conditionEraStartShortTerm) {
    this.conditionEraStartShortTerm = conditionEraStartShortTerm;
  }

  public CovariateSettings conditionGroupEraAnyTimePrior(Boolean conditionGroupEraAnyTimePrior) {
    this.conditionGroupEraAnyTimePrior = conditionGroupEraAnyTimePrior;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any time prior to index. (analysis ID 209) 
   * @return conditionGroupEraAnyTimePrior
   **/
  @JsonProperty("ConditionGroupEraAnyTimePrior")
  public Boolean isisConditionGroupEraAnyTimePrior() {
    return conditionGroupEraAnyTimePrior;
  }

  public void setConditionGroupEraAnyTimePrior(Boolean conditionGroupEraAnyTimePrior) {
    this.conditionGroupEraAnyTimePrior = conditionGroupEraAnyTimePrior;
  }

  public CovariateSettings conditionGroupEraLongTerm(Boolean conditionGroupEraLongTerm) {
    this.conditionGroupEraLongTerm = conditionGroupEraLongTerm;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the long term window. (analysis ID 210) 
   * @return conditionGroupEraLongTerm
   **/
  @JsonProperty("ConditionGroupEraLongTerm")
  public Boolean isisConditionGroupEraLongTerm() {
    return conditionGroupEraLongTerm;
  }

  public void setConditionGroupEraLongTerm(Boolean conditionGroupEraLongTerm) {
    this.conditionGroupEraLongTerm = conditionGroupEraLongTerm;
  }

  public CovariateSettings conditionGroupEraMediumTerm(Boolean conditionGroupEraMediumTerm) {
    this.conditionGroupEraMediumTerm = conditionGroupEraMediumTerm;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the medium term window. (analysis ID 211) 
   * @return conditionGroupEraMediumTerm
   **/
  @JsonProperty("ConditionGroupEraMediumTerm")
  public Boolean isisConditionGroupEraMediumTerm() {
    return conditionGroupEraMediumTerm;
  }

  public void setConditionGroupEraMediumTerm(Boolean conditionGroupEraMediumTerm) {
    this.conditionGroupEraMediumTerm = conditionGroupEraMediumTerm;
  }

  public CovariateSettings conditionGroupEraShortTerm(Boolean conditionGroupEraShortTerm) {
    this.conditionGroupEraShortTerm = conditionGroupEraShortTerm;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the short term window. (analysis ID 212) 
   * @return conditionGroupEraShortTerm
   **/
  @JsonProperty("ConditionGroupEraShortTerm")
  public Boolean isisConditionGroupEraShortTerm() {
    return conditionGroupEraShortTerm;
  }

  public void setConditionGroupEraShortTerm(Boolean conditionGroupEraShortTerm) {
    this.conditionGroupEraShortTerm = conditionGroupEraShortTerm;
  }

  public CovariateSettings conditionGroupEraOverlapping(Boolean conditionGroupEraOverlapping) {
    this.conditionGroupEraOverlapping = conditionGroupEraOverlapping;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with the end of the risk window. (analysis ID 213) 
   * @return conditionGroupEraOverlapping
   **/
  @JsonProperty("ConditionGroupEraOverlapping")
  public Boolean isisConditionGroupEraOverlapping() {
    return conditionGroupEraOverlapping;
  }

  public void setConditionGroupEraOverlapping(Boolean conditionGroupEraOverlapping) {
    this.conditionGroupEraOverlapping = conditionGroupEraOverlapping;
  }

  public CovariateSettings conditionGroupEraStartLongTerm(Boolean conditionGroupEraStartLongTerm) {
    this.conditionGroupEraStartLongTerm = conditionGroupEraStartLongTerm;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table starting in the long term window. (analysis ID 214) 
   * @return conditionGroupEraStartLongTerm
   **/
  @JsonProperty("ConditionGroupEraStartLongTerm")
  public Boolean isisConditionGroupEraStartLongTerm() {
    return conditionGroupEraStartLongTerm;
  }

  public void setConditionGroupEraStartLongTerm(Boolean conditionGroupEraStartLongTerm) {
    this.conditionGroupEraStartLongTerm = conditionGroupEraStartLongTerm;
  }

  public CovariateSettings conditionGroupEraStartMediumTerm(Boolean conditionGroupEraStartMediumTerm) {
    this.conditionGroupEraStartMediumTerm = conditionGroupEraStartMediumTerm;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table starting in the medium term window. (analysis ID 215) 
   * @return conditionGroupEraStartMediumTerm
   **/
  @JsonProperty("ConditionGroupEraStartMediumTerm")
  public Boolean isisConditionGroupEraStartMediumTerm() {
    return conditionGroupEraStartMediumTerm;
  }

  public void setConditionGroupEraStartMediumTerm(Boolean conditionGroupEraStartMediumTerm) {
    this.conditionGroupEraStartMediumTerm = conditionGroupEraStartMediumTerm;
  }

  public CovariateSettings conditionGroupEraStartShortTerm(Boolean conditionGroupEraStartShortTerm) {
    this.conditionGroupEraStartShortTerm = conditionGroupEraStartShortTerm;
    return this;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table starting in the short term window. (analysis ID 216) 
   * @return conditionGroupEraStartShortTerm
   **/
  @JsonProperty("ConditionGroupEraStartShortTerm")
  public Boolean isisConditionGroupEraStartShortTerm() {
    return conditionGroupEraStartShortTerm;
  }

  public void setConditionGroupEraStartShortTerm(Boolean conditionGroupEraStartShortTerm) {
    this.conditionGroupEraStartShortTerm = conditionGroupEraStartShortTerm;
  }

  public CovariateSettings drugExposureAnyTimePrior(Boolean drugExposureAnyTimePrior) {
    this.drugExposureAnyTimePrior = drugExposureAnyTimePrior;
    return this;
  }

  /**
   * One covariate per drug in the drug_exposure table starting any time prior to index. (analysis ID 301) 
   * @return drugExposureAnyTimePrior
   **/
  @JsonProperty("DrugExposureAnyTimePrior")
  public Boolean isisDrugExposureAnyTimePrior() {
    return drugExposureAnyTimePrior;
  }

  public void setDrugExposureAnyTimePrior(Boolean drugExposureAnyTimePrior) {
    this.drugExposureAnyTimePrior = drugExposureAnyTimePrior;
  }

  public CovariateSettings drugExposureLongTerm(Boolean drugExposureLongTerm) {
    this.drugExposureLongTerm = drugExposureLongTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_exposure table starting in the long term window. (analysis ID 302) 
   * @return drugExposureLongTerm
   **/
  @JsonProperty("DrugExposureLongTerm")
  public Boolean isisDrugExposureLongTerm() {
    return drugExposureLongTerm;
  }

  public void setDrugExposureLongTerm(Boolean drugExposureLongTerm) {
    this.drugExposureLongTerm = drugExposureLongTerm;
  }

  public CovariateSettings drugExposureMediumTerm(Boolean drugExposureMediumTerm) {
    this.drugExposureMediumTerm = drugExposureMediumTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_exposure table starting in the medium term window. (analysis ID 303) 
   * @return drugExposureMediumTerm
   **/
  @JsonProperty("DrugExposureMediumTerm")
  public Boolean isisDrugExposureMediumTerm() {
    return drugExposureMediumTerm;
  }

  public void setDrugExposureMediumTerm(Boolean drugExposureMediumTerm) {
    this.drugExposureMediumTerm = drugExposureMediumTerm;
  }

  public CovariateSettings drugExposureShortTerm(Boolean drugExposureShortTerm) {
    this.drugExposureShortTerm = drugExposureShortTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_exposure table starting in the short term window. (analysis ID 304) 
   * @return drugExposureShortTerm
   **/
  @JsonProperty("DrugExposureShortTerm")
  public Boolean isisDrugExposureShortTerm() {
    return drugExposureShortTerm;
  }

  public void setDrugExposureShortTerm(Boolean drugExposureShortTerm) {
    this.drugExposureShortTerm = drugExposureShortTerm;
  }

  public CovariateSettings drugEraAnyTimePrior(Boolean drugEraAnyTimePrior) {
    this.drugEraAnyTimePrior = drugEraAnyTimePrior;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any time prior to index. (analysis ID 401) 
   * @return drugEraAnyTimePrior
   **/
  @JsonProperty("DrugEraAnyTimePrior")
  public Boolean isisDrugEraAnyTimePrior() {
    return drugEraAnyTimePrior;
  }

  public void setDrugEraAnyTimePrior(Boolean drugEraAnyTimePrior) {
    this.drugEraAnyTimePrior = drugEraAnyTimePrior;
  }

  public CovariateSettings drugEraLongTerm(Boolean drugEraLongTerm) {
    this.drugEraLongTerm = drugEraLongTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any part of the long term window. (analysis ID 402) 
   * @return drugEraLongTerm
   **/
  @JsonProperty("DrugEraLongTerm")
  public Boolean isisDrugEraLongTerm() {
    return drugEraLongTerm;
  }

  public void setDrugEraLongTerm(Boolean drugEraLongTerm) {
    this.drugEraLongTerm = drugEraLongTerm;
  }

  public CovariateSettings drugEraMediumTerm(Boolean drugEraMediumTerm) {
    this.drugEraMediumTerm = drugEraMediumTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any part of the medium term window. (analysis ID 403) 
   * @return drugEraMediumTerm
   **/
  @JsonProperty("DrugEraMediumTerm")
  public Boolean isisDrugEraMediumTerm() {
    return drugEraMediumTerm;
  }

  public void setDrugEraMediumTerm(Boolean drugEraMediumTerm) {
    this.drugEraMediumTerm = drugEraMediumTerm;
  }

  public CovariateSettings drugEraShortTerm(Boolean drugEraShortTerm) {
    this.drugEraShortTerm = drugEraShortTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any part of the short window. (analysis ID 404) 
   * @return drugEraShortTerm
   **/
  @JsonProperty("DrugEraShortTerm")
  public Boolean isisDrugEraShortTerm() {
    return drugEraShortTerm;
  }

  public void setDrugEraShortTerm(Boolean drugEraShortTerm) {
    this.drugEraShortTerm = drugEraShortTerm;
  }

  public CovariateSettings drugEraOverlapping(Boolean drugEraOverlapping) {
    this.drugEraOverlapping = drugEraOverlapping;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with the end of the risk window. (analysis ID 405) 
   * @return drugEraOverlapping
   **/
  @JsonProperty("DrugEraOverlapping")
  public Boolean isisDrugEraOverlapping() {
    return drugEraOverlapping;
  }

  public void setDrugEraOverlapping(Boolean drugEraOverlapping) {
    this.drugEraOverlapping = drugEraOverlapping;
  }

  public CovariateSettings drugEraStartLongTerm(Boolean drugEraStartLongTerm) {
    this.drugEraStartLongTerm = drugEraStartLongTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table starting in the long term window. (analysis ID 406) 
   * @return drugEraStartLongTerm
   **/
  @JsonProperty("DrugEraStartLongTerm")
  public Boolean isisDrugEraStartLongTerm() {
    return drugEraStartLongTerm;
  }

  public void setDrugEraStartLongTerm(Boolean drugEraStartLongTerm) {
    this.drugEraStartLongTerm = drugEraStartLongTerm;
  }

  public CovariateSettings drugEraStartMediumTerm(Boolean drugEraStartMediumTerm) {
    this.drugEraStartMediumTerm = drugEraStartMediumTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table starting in the medium term window. (analysis ID 407) 
   * @return drugEraStartMediumTerm
   **/
  @JsonProperty("DrugEraStartMediumTerm")
  public Boolean isisDrugEraStartMediumTerm() {
    return drugEraStartMediumTerm;
  }

  public void setDrugEraStartMediumTerm(Boolean drugEraStartMediumTerm) {
    this.drugEraStartMediumTerm = drugEraStartMediumTerm;
  }

  public CovariateSettings drugEraStartShortTerm(Boolean drugEraStartShortTerm) {
    this.drugEraStartShortTerm = drugEraStartShortTerm;
    return this;
  }

  /**
   * One covariate per drug in the drug_era table starting in the long short window. (analysis ID 408) 
   * @return drugEraStartShortTerm
   **/
  @JsonProperty("DrugEraStartShortTerm")
  public Boolean isisDrugEraStartShortTerm() {
    return drugEraStartShortTerm;
  }

  public void setDrugEraStartShortTerm(Boolean drugEraStartShortTerm) {
    this.drugEraStartShortTerm = drugEraStartShortTerm;
  }

  public CovariateSettings drugGroupEraAnyTimePrior(Boolean drugGroupEraAnyTimePrior) {
    this.drugGroupEraAnyTimePrior = drugGroupEraAnyTimePrior;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any time prior to index. (analysis ID 409) 
   * @return drugGroupEraAnyTimePrior
   **/
  @JsonProperty("DrugGroupEraAnyTimePrior")
  public Boolean isisDrugGroupEraAnyTimePrior() {
    return drugGroupEraAnyTimePrior;
  }

  public void setDrugGroupEraAnyTimePrior(Boolean drugGroupEraAnyTimePrior) {
    this.drugGroupEraAnyTimePrior = drugGroupEraAnyTimePrior;
  }

  public CovariateSettings drugGroupEraLongTerm(Boolean drugGroupEraLongTerm) {
    this.drugGroupEraLongTerm = drugGroupEraLongTerm;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the long term window. (analysis ID 410) 
   * @return drugGroupEraLongTerm
   **/
  @JsonProperty("DrugGroupEraLongTerm")
  public Boolean isisDrugGroupEraLongTerm() {
    return drugGroupEraLongTerm;
  }

  public void setDrugGroupEraLongTerm(Boolean drugGroupEraLongTerm) {
    this.drugGroupEraLongTerm = drugGroupEraLongTerm;
  }

  public CovariateSettings drugGroupEraMediumTerm(Boolean drugGroupEraMediumTerm) {
    this.drugGroupEraMediumTerm = drugGroupEraMediumTerm;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the medium term window. (analysis ID 411) 
   * @return drugGroupEraMediumTerm
   **/
  @JsonProperty("DrugGroupEraMediumTerm")
  public Boolean isisDrugGroupEraMediumTerm() {
    return drugGroupEraMediumTerm;
  }

  public void setDrugGroupEraMediumTerm(Boolean drugGroupEraMediumTerm) {
    this.drugGroupEraMediumTerm = drugGroupEraMediumTerm;
  }

  public CovariateSettings drugGroupEraShortTerm(Boolean drugGroupEraShortTerm) {
    this.drugGroupEraShortTerm = drugGroupEraShortTerm;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the short term window. (analysis ID 412) 
   * @return drugGroupEraShortTerm
   **/
  @JsonProperty("DrugGroupEraShortTerm")
  public Boolean isisDrugGroupEraShortTerm() {
    return drugGroupEraShortTerm;
  }

  public void setDrugGroupEraShortTerm(Boolean drugGroupEraShortTerm) {
    this.drugGroupEraShortTerm = drugGroupEraShortTerm;
  }

  public CovariateSettings drugGroupEraOverlapping(Boolean drugGroupEraOverlapping) {
    this.drugGroupEraOverlapping = drugGroupEraOverlapping;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with the end of the risk window. (analysis ID 413) 
   * @return drugGroupEraOverlapping
   **/
  @JsonProperty("DrugGroupEraOverlapping")
  public Boolean isisDrugGroupEraOverlapping() {
    return drugGroupEraOverlapping;
  }

  public void setDrugGroupEraOverlapping(Boolean drugGroupEraOverlapping) {
    this.drugGroupEraOverlapping = drugGroupEraOverlapping;
  }

  public CovariateSettings drugGroupEraStartLongTerm(Boolean drugGroupEraStartLongTerm) {
    this.drugGroupEraStartLongTerm = drugGroupEraStartLongTerm;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table starting in the long term window. (analysis ID 414) 
   * @return drugGroupEraStartLongTerm
   **/
  @JsonProperty("DrugGroupEraStartLongTerm")
  public Boolean isisDrugGroupEraStartLongTerm() {
    return drugGroupEraStartLongTerm;
  }

  public void setDrugGroupEraStartLongTerm(Boolean drugGroupEraStartLongTerm) {
    this.drugGroupEraStartLongTerm = drugGroupEraStartLongTerm;
  }

  public CovariateSettings drugGroupEraStartMediumTerm(Boolean drugGroupEraStartMediumTerm) {
    this.drugGroupEraStartMediumTerm = drugGroupEraStartMediumTerm;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table starting in the medium term window. (analysis ID 415) 
   * @return drugGroupEraStartMediumTerm
   **/
  @JsonProperty("DrugGroupEraStartMediumTerm")
  public Boolean isisDrugGroupEraStartMediumTerm() {
    return drugGroupEraStartMediumTerm;
  }

  public void setDrugGroupEraStartMediumTerm(Boolean drugGroupEraStartMediumTerm) {
    this.drugGroupEraStartMediumTerm = drugGroupEraStartMediumTerm;
  }

  public CovariateSettings drugGroupEraStartShortTerm(Boolean drugGroupEraStartShortTerm) {
    this.drugGroupEraStartShortTerm = drugGroupEraStartShortTerm;
    return this;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table starting in the short term window. (analysis ID 416) 
   * @return drugGroupEraStartShortTerm
   **/
  @JsonProperty("DrugGroupEraStartShortTerm")
  public Boolean isisDrugGroupEraStartShortTerm() {
    return drugGroupEraStartShortTerm;
  }

  public void setDrugGroupEraStartShortTerm(Boolean drugGroupEraStartShortTerm) {
    this.drugGroupEraStartShortTerm = drugGroupEraStartShortTerm;
  }

  public CovariateSettings procedureOccurrenceAnyTimePrior(Boolean procedureOccurrenceAnyTimePrior) {
    this.procedureOccurrenceAnyTimePrior = procedureOccurrenceAnyTimePrior;
    return this;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table any time prior to index. (analysis ID 501) 
   * @return procedureOccurrenceAnyTimePrior
   **/
  @JsonProperty("ProcedureOccurrenceAnyTimePrior")
  public Boolean isisProcedureOccurrenceAnyTimePrior() {
    return procedureOccurrenceAnyTimePrior;
  }

  public void setProcedureOccurrenceAnyTimePrior(Boolean procedureOccurrenceAnyTimePrior) {
    this.procedureOccurrenceAnyTimePrior = procedureOccurrenceAnyTimePrior;
  }

  public CovariateSettings procedureOccurrenceLongTerm(Boolean procedureOccurrenceLongTerm) {
    this.procedureOccurrenceLongTerm = procedureOccurrenceLongTerm;
    return this;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table in the long term window. (analysis ID 502) 
   * @return procedureOccurrenceLongTerm
   **/
  @JsonProperty("ProcedureOccurrenceLongTerm")
  public Boolean isisProcedureOccurrenceLongTerm() {
    return procedureOccurrenceLongTerm;
  }

  public void setProcedureOccurrenceLongTerm(Boolean procedureOccurrenceLongTerm) {
    this.procedureOccurrenceLongTerm = procedureOccurrenceLongTerm;
  }

  public CovariateSettings procedureOccurrenceMediumTerm(Boolean procedureOccurrenceMediumTerm) {
    this.procedureOccurrenceMediumTerm = procedureOccurrenceMediumTerm;
    return this;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table in the medium term window. (analysis ID 503) 
   * @return procedureOccurrenceMediumTerm
   **/
  @JsonProperty("ProcedureOccurrenceMediumTerm")
  public Boolean isisProcedureOccurrenceMediumTerm() {
    return procedureOccurrenceMediumTerm;
  }

  public void setProcedureOccurrenceMediumTerm(Boolean procedureOccurrenceMediumTerm) {
    this.procedureOccurrenceMediumTerm = procedureOccurrenceMediumTerm;
  }

  public CovariateSettings procedureOccurrenceShortTerm(Boolean procedureOccurrenceShortTerm) {
    this.procedureOccurrenceShortTerm = procedureOccurrenceShortTerm;
    return this;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table in the short term window. (analysis ID 504) 
   * @return procedureOccurrenceShortTerm
   **/
  @JsonProperty("ProcedureOccurrenceShortTerm")
  public Boolean isisProcedureOccurrenceShortTerm() {
    return procedureOccurrenceShortTerm;
  }

  public void setProcedureOccurrenceShortTerm(Boolean procedureOccurrenceShortTerm) {
    this.procedureOccurrenceShortTerm = procedureOccurrenceShortTerm;
  }

  public CovariateSettings deviceExposureAnyTimePrior(Boolean deviceExposureAnyTimePrior) {
    this.deviceExposureAnyTimePrior = deviceExposureAnyTimePrior;
    return this;
  }

  /**
   * One covariate per device in the device exposure table starting any time prior to index. (analysis ID 601) 
   * @return deviceExposureAnyTimePrior
   **/
  @JsonProperty("DeviceExposureAnyTimePrior")
  public Boolean isisDeviceExposureAnyTimePrior() {
    return deviceExposureAnyTimePrior;
  }

  public void setDeviceExposureAnyTimePrior(Boolean deviceExposureAnyTimePrior) {
    this.deviceExposureAnyTimePrior = deviceExposureAnyTimePrior;
  }

  public CovariateSettings deviceExposureLongTerm(Boolean deviceExposureLongTerm) {
    this.deviceExposureLongTerm = deviceExposureLongTerm;
    return this;
  }

  /**
   * One covariate per device in the device exposure table starting in the long term window. (analysis ID 602) 
   * @return deviceExposureLongTerm
   **/
  @JsonProperty("DeviceExposureLongTerm")
  public Boolean isisDeviceExposureLongTerm() {
    return deviceExposureLongTerm;
  }

  public void setDeviceExposureLongTerm(Boolean deviceExposureLongTerm) {
    this.deviceExposureLongTerm = deviceExposureLongTerm;
  }

  public CovariateSettings deviceExposureMediumTerm(Boolean deviceExposureMediumTerm) {
    this.deviceExposureMediumTerm = deviceExposureMediumTerm;
    return this;
  }

  /**
   * One covariate per device in the device exposure table starting in the medium term window. (analysis ID 603) 
   * @return deviceExposureMediumTerm
   **/
  @JsonProperty("DeviceExposureMediumTerm")
  public Boolean isisDeviceExposureMediumTerm() {
    return deviceExposureMediumTerm;
  }

  public void setDeviceExposureMediumTerm(Boolean deviceExposureMediumTerm) {
    this.deviceExposureMediumTerm = deviceExposureMediumTerm;
  }

  public CovariateSettings deviceExposureShortTerm(Boolean deviceExposureShortTerm) {
    this.deviceExposureShortTerm = deviceExposureShortTerm;
    return this;
  }

  /**
   * One covariate per device in the device exposure table starting in the short term window. (analysis ID 604) 
   * @return deviceExposureShortTerm
   **/
  @JsonProperty("DeviceExposureShortTerm")
  public Boolean isisDeviceExposureShortTerm() {
    return deviceExposureShortTerm;
  }

  public void setDeviceExposureShortTerm(Boolean deviceExposureShortTerm) {
    this.deviceExposureShortTerm = deviceExposureShortTerm;
  }

  public CovariateSettings measurementAnyTimePrior(Boolean measurementAnyTimePrior) {
    this.measurementAnyTimePrior = measurementAnyTimePrior;
    return this;
  }

  /**
   * One covariate per measurement in the measurement table any time prior to index. (analysis ID 701) 
   * @return measurementAnyTimePrior
   **/
  @JsonProperty("MeasurementAnyTimePrior")
  public Boolean isisMeasurementAnyTimePrior() {
    return measurementAnyTimePrior;
  }

  public void setMeasurementAnyTimePrior(Boolean measurementAnyTimePrior) {
    this.measurementAnyTimePrior = measurementAnyTimePrior;
  }

  public CovariateSettings measurementLongTerm(Boolean measurementLongTerm) {
    this.measurementLongTerm = measurementLongTerm;
    return this;
  }

  /**
   * One covariate per measurement in the measurement table in the long term window. (analysis ID 702) 
   * @return measurementLongTerm
   **/
  @JsonProperty("MeasurementLongTerm")
  public Boolean isisMeasurementLongTerm() {
    return measurementLongTerm;
  }

  public void setMeasurementLongTerm(Boolean measurementLongTerm) {
    this.measurementLongTerm = measurementLongTerm;
  }

  public CovariateSettings measurementMediumTerm(Boolean measurementMediumTerm) {
    this.measurementMediumTerm = measurementMediumTerm;
    return this;
  }

  /**
   * One covariate per measurement in the measurement table in the medium term window. (analysis ID 703) 
   * @return measurementMediumTerm
   **/
  @JsonProperty("MeasurementMediumTerm")
  public Boolean isisMeasurementMediumTerm() {
    return measurementMediumTerm;
  }

  public void setMeasurementMediumTerm(Boolean measurementMediumTerm) {
    this.measurementMediumTerm = measurementMediumTerm;
  }

  public CovariateSettings measurementShortTerm(Boolean measurementShortTerm) {
    this.measurementShortTerm = measurementShortTerm;
    return this;
  }

  /**
   * One covariate per measurement in the measurement table in the short term window. (analysis ID 704) 
   * @return measurementShortTerm
   **/
  @JsonProperty("MeasurementShortTerm")
  public Boolean isisMeasurementShortTerm() {
    return measurementShortTerm;
  }

  public void setMeasurementShortTerm(Boolean measurementShortTerm) {
    this.measurementShortTerm = measurementShortTerm;
  }

  public CovariateSettings measurementValueAnyTimePrior(Boolean measurementValueAnyTimePrior) {
    this.measurementValueAnyTimePrior = measurementValueAnyTimePrior;
    return this;
  }

  /**
   * One covariate containing the value per measurement-unit combination any time prior to index. (analysis ID 705) 
   * @return measurementValueAnyTimePrior
   **/
  @JsonProperty("MeasurementValueAnyTimePrior")
  public Boolean isisMeasurementValueAnyTimePrior() {
    return measurementValueAnyTimePrior;
  }

  public void setMeasurementValueAnyTimePrior(Boolean measurementValueAnyTimePrior) {
    this.measurementValueAnyTimePrior = measurementValueAnyTimePrior;
  }

  public CovariateSettings measurementValueLongTerm(Boolean measurementValueLongTerm) {
    this.measurementValueLongTerm = measurementValueLongTerm;
    return this;
  }

  /**
   * One covariate containing the value per measurement-unit combination in the long term window. (analysis ID 706) 
   * @return measurementValueLongTerm
   **/
  @JsonProperty("MeasurementValueLongTerm")
  public Boolean isisMeasurementValueLongTerm() {
    return measurementValueLongTerm;
  }

  public void setMeasurementValueLongTerm(Boolean measurementValueLongTerm) {
    this.measurementValueLongTerm = measurementValueLongTerm;
  }

  public CovariateSettings measurementValueMediumTerm(Boolean measurementValueMediumTerm) {
    this.measurementValueMediumTerm = measurementValueMediumTerm;
    return this;
  }

  /**
   * One covariate containing the value per measurement-unit combination in the medium term window. (analysis ID 707) 
   * @return measurementValueMediumTerm
   **/
  @JsonProperty("MeasurementValueMediumTerm")
  public Boolean isisMeasurementValueMediumTerm() {
    return measurementValueMediumTerm;
  }

  public void setMeasurementValueMediumTerm(Boolean measurementValueMediumTerm) {
    this.measurementValueMediumTerm = measurementValueMediumTerm;
  }

  public CovariateSettings measurementValueShortTerm(Boolean measurementValueShortTerm) {
    this.measurementValueShortTerm = measurementValueShortTerm;
    return this;
  }

  /**
   * One covariate containing the value per measurement-unit combination in the short term window. (analysis ID 708) 
   * @return measurementValueShortTerm
   **/
  @JsonProperty("MeasurementValueShortTerm")
  public Boolean isisMeasurementValueShortTerm() {
    return measurementValueShortTerm;
  }

  public void setMeasurementValueShortTerm(Boolean measurementValueShortTerm) {
    this.measurementValueShortTerm = measurementValueShortTerm;
  }

  public CovariateSettings measurementRangeGroupAnyTimePrior(Boolean measurementRangeGroupAnyTimePrior) {
    this.measurementRangeGroupAnyTimePrior = measurementRangeGroupAnyTimePrior;
    return this;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range any time prior to index. (analysis ID 709) 
   * @return measurementRangeGroupAnyTimePrior
   **/
  @JsonProperty("MeasurementRangeGroupAnyTimePrior")
  public Boolean isisMeasurementRangeGroupAnyTimePrior() {
    return measurementRangeGroupAnyTimePrior;
  }

  public void setMeasurementRangeGroupAnyTimePrior(Boolean measurementRangeGroupAnyTimePrior) {
    this.measurementRangeGroupAnyTimePrior = measurementRangeGroupAnyTimePrior;
  }

  public CovariateSettings measurementRangeGroupLongTerm(Boolean measurementRangeGroupLongTerm) {
    this.measurementRangeGroupLongTerm = measurementRangeGroupLongTerm;
    return this;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range in the long term window. (analysis ID 710) 
   * @return measurementRangeGroupLongTerm
   **/
  @JsonProperty("MeasurementRangeGroupLongTerm")
  public Boolean isisMeasurementRangeGroupLongTerm() {
    return measurementRangeGroupLongTerm;
  }

  public void setMeasurementRangeGroupLongTerm(Boolean measurementRangeGroupLongTerm) {
    this.measurementRangeGroupLongTerm = measurementRangeGroupLongTerm;
  }

  public CovariateSettings measurementRangeGroupMediumTerm(Boolean measurementRangeGroupMediumTerm) {
    this.measurementRangeGroupMediumTerm = measurementRangeGroupMediumTerm;
    return this;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range in the medium term window. (analysis ID 711) 
   * @return measurementRangeGroupMediumTerm
   **/
  @JsonProperty("MeasurementRangeGroupMediumTerm")
  public Boolean isisMeasurementRangeGroupMediumTerm() {
    return measurementRangeGroupMediumTerm;
  }

  public void setMeasurementRangeGroupMediumTerm(Boolean measurementRangeGroupMediumTerm) {
    this.measurementRangeGroupMediumTerm = measurementRangeGroupMediumTerm;
  }

  public CovariateSettings measurementRangeGroupShortTerm(Boolean measurementRangeGroupShortTerm) {
    this.measurementRangeGroupShortTerm = measurementRangeGroupShortTerm;
    return this;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range in the short term window. (analysis ID 712) 
   * @return measurementRangeGroupShortTerm
   **/
  @JsonProperty("MeasurementRangeGroupShortTerm")
  public Boolean isisMeasurementRangeGroupShortTerm() {
    return measurementRangeGroupShortTerm;
  }

  public void setMeasurementRangeGroupShortTerm(Boolean measurementRangeGroupShortTerm) {
    this.measurementRangeGroupShortTerm = measurementRangeGroupShortTerm;
  }

  public CovariateSettings observationAnyTimePrior(Boolean observationAnyTimePrior) {
    this.observationAnyTimePrior = observationAnyTimePrior;
    return this;
  }

  /**
   * One covariate per observation in the observation table any time prior to index. (analysis ID 801) 
   * @return observationAnyTimePrior
   **/
  @JsonProperty("ObservationAnyTimePrior")
  public Boolean isisObservationAnyTimePrior() {
    return observationAnyTimePrior;
  }

  public void setObservationAnyTimePrior(Boolean observationAnyTimePrior) {
    this.observationAnyTimePrior = observationAnyTimePrior;
  }

  public CovariateSettings observationLongTerm(Boolean observationLongTerm) {
    this.observationLongTerm = observationLongTerm;
    return this;
  }

  /**
   * One covariate per observation in the observation table in the long term window. (analysis ID 802) 
   * @return observationLongTerm
   **/
  @JsonProperty("ObservationLongTerm")
  public Boolean isisObservationLongTerm() {
    return observationLongTerm;
  }

  public void setObservationLongTerm(Boolean observationLongTerm) {
    this.observationLongTerm = observationLongTerm;
  }

  public CovariateSettings observationMediumTerm(Boolean observationMediumTerm) {
    this.observationMediumTerm = observationMediumTerm;
    return this;
  }

  /**
   * One covariate per observation in the observation table in the medium term window. (analysis ID 803) 
   * @return observationMediumTerm
   **/
  @JsonProperty("ObservationMediumTerm")
  public Boolean isisObservationMediumTerm() {
    return observationMediumTerm;
  }

  public void setObservationMediumTerm(Boolean observationMediumTerm) {
    this.observationMediumTerm = observationMediumTerm;
  }

  public CovariateSettings observationShortTerm(Boolean observationShortTerm) {
    this.observationShortTerm = observationShortTerm;
    return this;
  }

  /**
   * One covariate per observation in the observation table in the short term window. (analysis ID 804) 
   * @return observationShortTerm
   **/
  @JsonProperty("ObservationShortTerm")
  public Boolean isisObservationShortTerm() {
    return observationShortTerm;
  }

  public void setObservationShortTerm(Boolean observationShortTerm) {
    this.observationShortTerm = observationShortTerm;
  }

  public CovariateSettings charlsonIndex(Boolean charlsonIndex) {
    this.charlsonIndex = charlsonIndex;
    return this;
  }

  /**
   * The Charlson comorbidity index (Romano adaptation) using all conditions prior to the window end. (analysis ID 901) 
   * @return charlsonIndex
   **/
  @JsonProperty("CharlsonIndex")
  public Boolean isisCharlsonIndex() {
    return charlsonIndex;
  }

  public void setCharlsonIndex(Boolean charlsonIndex) {
    this.charlsonIndex = charlsonIndex;
  }

  public CovariateSettings dcsi(Boolean dcsi) {
    this.dcsi = dcsi;
    return this;
  }

  /**
   * The Diabetes Comorbidity Severity Index (DCSI) using all conditions prior to the window end. (analysis ID 902) 
   * @return dcsi
   **/
  @JsonProperty("Dcsi")
  public Boolean isisDcsi() {
    return dcsi;
  }

  public void setDcsi(Boolean dcsi) {
    this.dcsi = dcsi;
  }

  public CovariateSettings chads2(Boolean chads2) {
    this.chads2 = chads2;
    return this;
  }

  /**
   * The CHADS2 score using all conditions prior to the window end. (analysis ID 903) 
   * @return chads2
   **/
  @JsonProperty("Chads2")
  public Boolean isisChads2() {
    return chads2;
  }

  public void setChads2(Boolean chads2) {
    this.chads2 = chads2;
  }

  public CovariateSettings chads2Vasc(Boolean chads2Vasc) {
    this.chads2Vasc = chads2Vasc;
    return this;
  }

  /**
   * The CHADS2VASc score using all conditions prior to the window end. (analysis ID 904) 
   * @return chads2Vasc
   **/
  @JsonProperty("Chads2Vasc")
  public Boolean isisChads2Vasc() {
    return chads2Vasc;
  }

  public void setChads2Vasc(Boolean chads2Vasc) {
    this.chads2Vasc = chads2Vasc;
  }

  public CovariateSettings distinctConditionCountLongTerm(Boolean distinctConditionCountLongTerm) {
    this.distinctConditionCountLongTerm = distinctConditionCountLongTerm;
    return this;
  }

  /**
   * The number of distinct condition concepts observed in the long term window. (analysis ID 905) 
   * @return distinctConditionCountLongTerm
   **/
  @JsonProperty("DistinctConditionCountLongTerm")
  public Boolean isisDistinctConditionCountLongTerm() {
    return distinctConditionCountLongTerm;
  }

  public void setDistinctConditionCountLongTerm(Boolean distinctConditionCountLongTerm) {
    this.distinctConditionCountLongTerm = distinctConditionCountLongTerm;
  }

  public CovariateSettings distinctConditionCountMediumTerm(Boolean distinctConditionCountMediumTerm) {
    this.distinctConditionCountMediumTerm = distinctConditionCountMediumTerm;
    return this;
  }

  /**
   * The number of distinct condition concepts observed in the medium term window. (analysis ID 906) 
   * @return distinctConditionCountMediumTerm
   **/
  @JsonProperty("DistinctConditionCountMediumTerm")
  public Boolean isisDistinctConditionCountMediumTerm() {
    return distinctConditionCountMediumTerm;
  }

  public void setDistinctConditionCountMediumTerm(Boolean distinctConditionCountMediumTerm) {
    this.distinctConditionCountMediumTerm = distinctConditionCountMediumTerm;
  }

  public CovariateSettings distinctConditionCountShortTerm(Boolean distinctConditionCountShortTerm) {
    this.distinctConditionCountShortTerm = distinctConditionCountShortTerm;
    return this;
  }

  /**
   * The number of distinct condition concepts observed in the short term window. (analysis ID 907) 
   * @return distinctConditionCountShortTerm
   **/
  @JsonProperty("DistinctConditionCountShortTerm")
  public Boolean isisDistinctConditionCountShortTerm() {
    return distinctConditionCountShortTerm;
  }

  public void setDistinctConditionCountShortTerm(Boolean distinctConditionCountShortTerm) {
    this.distinctConditionCountShortTerm = distinctConditionCountShortTerm;
  }

  public CovariateSettings distinctIngredientCountLongTerm(Boolean distinctIngredientCountLongTerm) {
    this.distinctIngredientCountLongTerm = distinctIngredientCountLongTerm;
    return this;
  }

  /**
   * The number of distinct ingredients observed in the long term window. (analysis ID 908) 
   * @return distinctIngredientCountLongTerm
   **/
  @JsonProperty("DistinctIngredientCountLongTerm")
  public Boolean isisDistinctIngredientCountLongTerm() {
    return distinctIngredientCountLongTerm;
  }

  public void setDistinctIngredientCountLongTerm(Boolean distinctIngredientCountLongTerm) {
    this.distinctIngredientCountLongTerm = distinctIngredientCountLongTerm;
  }

  public CovariateSettings distinctIngredientCountMediumTerm(Boolean distinctIngredientCountMediumTerm) {
    this.distinctIngredientCountMediumTerm = distinctIngredientCountMediumTerm;
    return this;
  }

  /**
   * The number of distinct ingredients observed in the medium term window. (analysis ID 909) 
   * @return distinctIngredientCountMediumTerm
   **/
  @JsonProperty("DistinctIngredientCountMediumTerm")
  public Boolean isisDistinctIngredientCountMediumTerm() {
    return distinctIngredientCountMediumTerm;
  }

  public void setDistinctIngredientCountMediumTerm(Boolean distinctIngredientCountMediumTerm) {
    this.distinctIngredientCountMediumTerm = distinctIngredientCountMediumTerm;
  }

  public CovariateSettings distinctIngredientCountShortTerm(Boolean distinctIngredientCountShortTerm) {
    this.distinctIngredientCountShortTerm = distinctIngredientCountShortTerm;
    return this;
  }

  /**
   * The number of distinct ingredients observed in the short term window. (analysis ID 910) 
   * @return distinctIngredientCountShortTerm
   **/
  @JsonProperty("DistinctIngredientCountShortTerm")
  public Boolean isisDistinctIngredientCountShortTerm() {
    return distinctIngredientCountShortTerm;
  }

  public void setDistinctIngredientCountShortTerm(Boolean distinctIngredientCountShortTerm) {
    this.distinctIngredientCountShortTerm = distinctIngredientCountShortTerm;
  }

  public CovariateSettings distinctProcedureCountLongTerm(Boolean distinctProcedureCountLongTerm) {
    this.distinctProcedureCountLongTerm = distinctProcedureCountLongTerm;
    return this;
  }

  /**
   * The number of distinct procedures observed in the long term window. (analysis ID 911) 
   * @return distinctProcedureCountLongTerm
   **/
  @JsonProperty("DistinctProcedureCountLongTerm")
  public Boolean isisDistinctProcedureCountLongTerm() {
    return distinctProcedureCountLongTerm;
  }

  public void setDistinctProcedureCountLongTerm(Boolean distinctProcedureCountLongTerm) {
    this.distinctProcedureCountLongTerm = distinctProcedureCountLongTerm;
  }

  public CovariateSettings distinctProcedureCountMediumTerm(Boolean distinctProcedureCountMediumTerm) {
    this.distinctProcedureCountMediumTerm = distinctProcedureCountMediumTerm;
    return this;
  }

  /**
   * The number of distinct procedures observed in the medium term window. (analysis ID 912) 
   * @return distinctProcedureCountMediumTerm
   **/
  @JsonProperty("DistinctProcedureCountMediumTerm")
  public Boolean isisDistinctProcedureCountMediumTerm() {
    return distinctProcedureCountMediumTerm;
  }

  public void setDistinctProcedureCountMediumTerm(Boolean distinctProcedureCountMediumTerm) {
    this.distinctProcedureCountMediumTerm = distinctProcedureCountMediumTerm;
  }

  public CovariateSettings distinctProcedureCountShortTerm(Boolean distinctProcedureCountShortTerm) {
    this.distinctProcedureCountShortTerm = distinctProcedureCountShortTerm;
    return this;
  }

  /**
   * The number of distinct procedures observed in the short term window. (analysis ID 913) 
   * @return distinctProcedureCountShortTerm
   **/
  @JsonProperty("DistinctProcedureCountShortTerm")
  public Boolean isisDistinctProcedureCountShortTerm() {
    return distinctProcedureCountShortTerm;
  }

  public void setDistinctProcedureCountShortTerm(Boolean distinctProcedureCountShortTerm) {
    this.distinctProcedureCountShortTerm = distinctProcedureCountShortTerm;
  }

  public CovariateSettings distinctMeasurementCountLongTerm(Boolean distinctMeasurementCountLongTerm) {
    this.distinctMeasurementCountLongTerm = distinctMeasurementCountLongTerm;
    return this;
  }

  /**
   * The number of distinct measurements observed in the long term window. (analysis ID 914) 
   * @return distinctMeasurementCountLongTerm
   **/
  @JsonProperty("DistinctMeasurementCountLongTerm")
  public Boolean isisDistinctMeasurementCountLongTerm() {
    return distinctMeasurementCountLongTerm;
  }

  public void setDistinctMeasurementCountLongTerm(Boolean distinctMeasurementCountLongTerm) {
    this.distinctMeasurementCountLongTerm = distinctMeasurementCountLongTerm;
  }

  public CovariateSettings distinctMeasurementCountMediumTerm(Boolean distinctMeasurementCountMediumTerm) {
    this.distinctMeasurementCountMediumTerm = distinctMeasurementCountMediumTerm;
    return this;
  }

  /**
   * The number of distinct measurements observed in the medium term window. (analysis ID 915) 
   * @return distinctMeasurementCountMediumTerm
   **/
  @JsonProperty("DistinctMeasurementCountMediumTerm")
  public Boolean isisDistinctMeasurementCountMediumTerm() {
    return distinctMeasurementCountMediumTerm;
  }

  public void setDistinctMeasurementCountMediumTerm(Boolean distinctMeasurementCountMediumTerm) {
    this.distinctMeasurementCountMediumTerm = distinctMeasurementCountMediumTerm;
  }

  public CovariateSettings distinctMeasurementCountShortTerm(Boolean distinctMeasurementCountShortTerm) {
    this.distinctMeasurementCountShortTerm = distinctMeasurementCountShortTerm;
    return this;
  }

  /**
   * The number of distinct measurements observed in the short term window. (analysis ID 916) 
   * @return distinctMeasurementCountShortTerm
   **/
  @JsonProperty("DistinctMeasurementCountShortTerm")
  public Boolean isisDistinctMeasurementCountShortTerm() {
    return distinctMeasurementCountShortTerm;
  }

  public void setDistinctMeasurementCountShortTerm(Boolean distinctMeasurementCountShortTerm) {
    this.distinctMeasurementCountShortTerm = distinctMeasurementCountShortTerm;
  }

  public CovariateSettings distinctObservationCountLongTerm(Boolean distinctObservationCountLongTerm) {
    this.distinctObservationCountLongTerm = distinctObservationCountLongTerm;
    return this;
  }

  /**
   * The number of distinct observations observed in the long term window. (analysis ID 917) 
   * @return distinctObservationCountLongTerm
   **/
  @JsonProperty("DistinctObservationCountLongTerm")
  public Boolean isisDistinctObservationCountLongTerm() {
    return distinctObservationCountLongTerm;
  }

  public void setDistinctObservationCountLongTerm(Boolean distinctObservationCountLongTerm) {
    this.distinctObservationCountLongTerm = distinctObservationCountLongTerm;
  }

  public CovariateSettings distinctObservationCountMediumTerm(Boolean distinctObservationCountMediumTerm) {
    this.distinctObservationCountMediumTerm = distinctObservationCountMediumTerm;
    return this;
  }

  /**
   * The number of distinct observations observed in the medium term window. (analysis ID 918) 
   * @return distinctObservationCountMediumTerm
   **/
  @JsonProperty("DistinctObservationCountMediumTerm")
  public Boolean isisDistinctObservationCountMediumTerm() {
    return distinctObservationCountMediumTerm;
  }

  public void setDistinctObservationCountMediumTerm(Boolean distinctObservationCountMediumTerm) {
    this.distinctObservationCountMediumTerm = distinctObservationCountMediumTerm;
  }

  public CovariateSettings distinctObservationCountShortTerm(Boolean distinctObservationCountShortTerm) {
    this.distinctObservationCountShortTerm = distinctObservationCountShortTerm;
    return this;
  }

  /**
   * The number of distinct observations observed in the short term window. (analysis ID 919) 
   * @return distinctObservationCountShortTerm
   **/
  @JsonProperty("DistinctObservationCountShortTerm")
  public Boolean isisDistinctObservationCountShortTerm() {
    return distinctObservationCountShortTerm;
  }

  public void setDistinctObservationCountShortTerm(Boolean distinctObservationCountShortTerm) {
    this.distinctObservationCountShortTerm = distinctObservationCountShortTerm;
  }

  public CovariateSettings visitCountLongTerm(Boolean visitCountLongTerm) {
    this.visitCountLongTerm = visitCountLongTerm;
    return this;
  }

  /**
   * The number of visits observed in the long term window. (analysis ID 920) 
   * @return visitCountLongTerm
   **/
  @JsonProperty("VisitCountLongTerm")
  public Boolean isisVisitCountLongTerm() {
    return visitCountLongTerm;
  }

  public void setVisitCountLongTerm(Boolean visitCountLongTerm) {
    this.visitCountLongTerm = visitCountLongTerm;
  }

  public CovariateSettings visitCountMediumTerm(Boolean visitCountMediumTerm) {
    this.visitCountMediumTerm = visitCountMediumTerm;
    return this;
  }

  /**
   * The number of visits observed in the medium term window. (analysis ID 921) 
   * @return visitCountMediumTerm
   **/
  @JsonProperty("VisitCountMediumTerm")
  public Boolean isisVisitCountMediumTerm() {
    return visitCountMediumTerm;
  }

  public void setVisitCountMediumTerm(Boolean visitCountMediumTerm) {
    this.visitCountMediumTerm = visitCountMediumTerm;
  }

  public CovariateSettings visitCountShortTerm(Boolean visitCountShortTerm) {
    this.visitCountShortTerm = visitCountShortTerm;
    return this;
  }

  /**
   * The number of visits observed in the short term window. (analysis ID 922) 
   * @return visitCountShortTerm
   **/
  @JsonProperty("VisitCountShortTerm")
  public Boolean isisVisitCountShortTerm() {
    return visitCountShortTerm;
  }

  public void setVisitCountShortTerm(Boolean visitCountShortTerm) {
    this.visitCountShortTerm = visitCountShortTerm;
  }

  public CovariateSettings visitConceptCountLongTerm(Boolean visitConceptCountLongTerm) {
    this.visitConceptCountLongTerm = visitConceptCountLongTerm;
    return this;
  }

  /**
   * The number of visits observed in the long term window, stratified by visit concept ID. (analysis ID 923) 
   * @return visitConceptCountLongTerm
   **/
  @JsonProperty("VisitConceptCountLongTerm")
  public Boolean isisVisitConceptCountLongTerm() {
    return visitConceptCountLongTerm;
  }

  public void setVisitConceptCountLongTerm(Boolean visitConceptCountLongTerm) {
    this.visitConceptCountLongTerm = visitConceptCountLongTerm;
  }

  public CovariateSettings visitConceptCountMediumTerm(Boolean visitConceptCountMediumTerm) {
    this.visitConceptCountMediumTerm = visitConceptCountMediumTerm;
    return this;
  }

  /**
   * The number of visits observed in the medium term window, stratified by visit concept ID. (analysis ID 924) 
   * @return visitConceptCountMediumTerm
   **/
  @JsonProperty("VisitConceptCountMediumTerm")
  public Boolean isisVisitConceptCountMediumTerm() {
    return visitConceptCountMediumTerm;
  }

  public void setVisitConceptCountMediumTerm(Boolean visitConceptCountMediumTerm) {
    this.visitConceptCountMediumTerm = visitConceptCountMediumTerm;
  }

  public CovariateSettings visitConceptCountShortTerm(Boolean visitConceptCountShortTerm) {
    this.visitConceptCountShortTerm = visitConceptCountShortTerm;
    return this;
  }

  /**
   * The number of visits observed in the short term window, stratified by visit concept ID. (analysis ID 925) 
   * @return visitConceptCountShortTerm
   **/
  @JsonProperty("VisitConceptCountShortTerm")
  public Boolean isisVisitConceptCountShortTerm() {
    return visitConceptCountShortTerm;
  }

  public void setVisitConceptCountShortTerm(Boolean visitConceptCountShortTerm) {
    this.visitConceptCountShortTerm = visitConceptCountShortTerm;
  }

  public CovariateSettings longTermStartDays(Integer longTermStartDays) {
    this.longTermStartDays = longTermStartDays;
    return this;
  }

  /**
   * What is the start day (relative to the index date) of the long-term window? 
   * @return longTermStartDays
   **/
  @JsonProperty("longTermStartDays")
  public Integer getLongTermStartDays() {
    return longTermStartDays;
  }

  public void setLongTermStartDays(Integer longTermStartDays) {
    this.longTermStartDays = longTermStartDays;
  }

  public CovariateSettings mediumTermStartDays(Integer mediumTermStartDays) {
    this.mediumTermStartDays = mediumTermStartDays;
    return this;
  }

  /**
   * What is the start day (relative to the index date) of the medium-term window? 
   * @return mediumTermStartDays
   **/
  @JsonProperty("mediumTermStartDays")
  public Integer getMediumTermStartDays() {
    return mediumTermStartDays;
  }

  public void setMediumTermStartDays(Integer mediumTermStartDays) {
    this.mediumTermStartDays = mediumTermStartDays;
  }

  public CovariateSettings shortTermStartDays(Integer shortTermStartDays) {
    this.shortTermStartDays = shortTermStartDays;
    return this;
  }

  /**
   * What is the start day (relative to the index date) of the short-term window? 
   * @return shortTermStartDays
   **/
  @JsonProperty("shortTermStartDays")
  public Integer getShortTermStartDays() {
    return shortTermStartDays;
  }

  public void setShortTermStartDays(Integer shortTermStartDays) {
    this.shortTermStartDays = shortTermStartDays;
  }

  public CovariateSettings endDays(Integer endDays) {
    this.endDays = endDays;
    return this;
  }

  /**
   * What is the end day (relative to the index date) of the window? 
   * @return endDays
   **/
  @JsonProperty("endDays")
  public Integer getEndDays() {
    return endDays;
  }

  public void setEndDays(Integer endDays) {
    this.endDays = endDays;
  }

  public CovariateSettings includedCovariateConceptIds(List<Long> includedCovariateConceptIds) {
    this.includedCovariateConceptIds = includedCovariateConceptIds;
    return this;
  }

  public CovariateSettings addIncludedCovariateConceptIdsItem(Long includedCovariateConceptIdsItem) {
    if (this.includedCovariateConceptIds == null) {
      this.includedCovariateConceptIds = new ArrayList<Long>();
    }
    this.includedCovariateConceptIds.add(includedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that should be d to construct covariates. 
   * @return includedCovariateConceptIds
   **/
  @JsonProperty("includedCovariateConceptIds")
  public List<Long> getIncludedCovariateConceptIds() {
    return includedCovariateConceptIds;
  }

  public void setIncludedCovariateConceptIds(List<Long> includedCovariateConceptIds) {
    this.includedCovariateConceptIds = includedCovariateConceptIds;
  }

  public CovariateSettings addDescendantsToInclude(Boolean addDescendantsToInclude) {
    this.addDescendantsToInclude = addDescendantsToInclude;
    return this;
  }

  /**
   * Should descendant concept IDs be added to the list of concepts to include? 
   * @return addDescendantsToInclude
   **/
  @JsonProperty("addDescendantsToInclude")
  public Boolean isisAddDescendantsToInclude() {
    return addDescendantsToInclude;
  }

  public void setAddDescendantsToInclude(Boolean addDescendantsToInclude) {
    this.addDescendantsToInclude = addDescendantsToInclude;
  }

  public CovariateSettings excludedCovariateConceptIds(List<Long> excludedCovariateConceptIds) {
    this.excludedCovariateConceptIds = excludedCovariateConceptIds;
    return this;
  }

  public CovariateSettings addExcludedCovariateConceptIdsItem(Long excludedCovariateConceptIdsItem) {
    if (this.excludedCovariateConceptIds == null) {
      this.excludedCovariateConceptIds = new ArrayList<Long>();
    }
    this.excludedCovariateConceptIds.add(excludedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that should NOT be d to construct covariates. 
   * @return excludedCovariateConceptIds
   **/
  @JsonProperty("excludedCovariateConceptIds")
  public List<Long> getExcludedCovariateConceptIds() {
    return excludedCovariateConceptIds;
  }

  public void setExcludedCovariateConceptIds(List<Long> excludedCovariateConceptIds) {
    this.excludedCovariateConceptIds = excludedCovariateConceptIds;
  }

  public CovariateSettings addDescendantsToExclude(Boolean addDescendantsToExclude) {
    this.addDescendantsToExclude = addDescendantsToExclude;
    return this;
  }

  /**
   * Should descendant concept IDs be added to the list of concepts to exclude? 
   * @return addDescendantsToExclude
   **/
  @JsonProperty("addDescendantsToExclude")
  public Boolean isisAddDescendantsToExclude() {
    return addDescendantsToExclude;
  }

  public void setAddDescendantsToExclude(Boolean addDescendantsToExclude) {
    this.addDescendantsToExclude = addDescendantsToExclude;
  }

  public CovariateSettings includedCovariateIds(List<Integer> includedCovariateIds) {
    this.includedCovariateIds = includedCovariateIds;
    return this;
  }

  public CovariateSettings addIncludedCovariateIdsItem(Integer includedCovariateIdsItem) {
    if (this.includedCovariateIds == null) {
      this.includedCovariateIds = new ArrayList<Integer>();
    }
    this.includedCovariateIds.add(includedCovariateIdsItem);
    return this;
  }

  /**
   * A list of covariate IDs that should be restricted to. 
   * @return includedCovariateIds
   **/
  @JsonProperty("includedCovariateIds")
  public List<Integer> getIncludedCovariateIds() {
    return includedCovariateIds;
  }

  public void setIncludedCovariateIds(List<Integer> includedCovariateIds) {
    this.includedCovariateIds = includedCovariateIds;
  }

  public CovariateSettings attrFun(String attrFun) {
    this.attrFun = attrFun;
    return this;
  }

  /**
   * Get attrFun
   * @return attrFun
   **/
  @JsonProperty("attr_fun")
  public String getAttrFun() {
    return attrFun;
  }

  public void setAttrFun(String attrFun) {
    this.attrFun = attrFun;
  }

  public CovariateSettings attrClass(String attrClass) {
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
    CovariateSettings covariateSettings = (CovariateSettings) o;
    return Objects.equals(this.temporal, covariateSettings.temporal) &&
        Objects.equals(this.demographicsGender, covariateSettings.demographicsGender) &&
        Objects.equals(this.demographicsAge, covariateSettings.demographicsAge) &&
        Objects.equals(this.demographicsAgeGroup, covariateSettings.demographicsAgeGroup) &&
        Objects.equals(this.demographicsRace, covariateSettings.demographicsRace) &&
        Objects.equals(this.demographicsEthnicity, covariateSettings.demographicsEthnicity) &&
        Objects.equals(this.demographicsIndexYear, covariateSettings.demographicsIndexYear) &&
        Objects.equals(this.demographicsIndexMonth, covariateSettings.demographicsIndexMonth) &&
        Objects.equals(this.demographicsPriorObservationTime, covariateSettings.demographicsPriorObservationTime) &&
        Objects.equals(this.demographicsPostObservationTime, covariateSettings.demographicsPostObservationTime) &&
        Objects.equals(this.demographicsTimeInCohort, covariateSettings.demographicsTimeInCohort) &&
        Objects.equals(this.demographicsIndexYearMonth, covariateSettings.demographicsIndexYearMonth) &&
        Objects.equals(this.conditionOccurrenceAnyTimePrior, covariateSettings.conditionOccurrenceAnyTimePrior) &&
        Objects.equals(this.conditionOccurrenceLongTerm, covariateSettings.conditionOccurrenceLongTerm) &&
        Objects.equals(this.conditionOccurrenceMediumTerm, covariateSettings.conditionOccurrenceMediumTerm) &&
        Objects.equals(this.conditionOccurrenceShortTerm, covariateSettings.conditionOccurrenceShortTerm) &&
        Objects.equals(this.conditionOccurrencePrimaryInpatientAnyTimePrior, covariateSettings.conditionOccurrencePrimaryInpatientAnyTimePrior) &&
        Objects.equals(this.conditionOccurrencePrimaryInpatientLongTerm, covariateSettings.conditionOccurrencePrimaryInpatientLongTerm) &&
        Objects.equals(this.conditionOccurrencePrimaryInpatientMediumTerm, covariateSettings.conditionOccurrencePrimaryInpatientMediumTerm) &&
        Objects.equals(this.conditionOccurrencePrimaryInpatientShortTerm, covariateSettings.conditionOccurrencePrimaryInpatientShortTerm) &&
        Objects.equals(this.conditionEraAnyTimePrior, covariateSettings.conditionEraAnyTimePrior) &&
        Objects.equals(this.conditionEraLongTerm, covariateSettings.conditionEraLongTerm) &&
        Objects.equals(this.conditionEraMediumTerm, covariateSettings.conditionEraMediumTerm) &&
        Objects.equals(this.conditionEraShortTerm, covariateSettings.conditionEraShortTerm) &&
        Objects.equals(this.conditionEraOverlapping, covariateSettings.conditionEraOverlapping) &&
        Objects.equals(this.conditionEraStartLongTerm, covariateSettings.conditionEraStartLongTerm) &&
        Objects.equals(this.conditionEraStartMediumTerm, covariateSettings.conditionEraStartMediumTerm) &&
        Objects.equals(this.conditionEraStartShortTerm, covariateSettings.conditionEraStartShortTerm) &&
        Objects.equals(this.conditionGroupEraAnyTimePrior, covariateSettings.conditionGroupEraAnyTimePrior) &&
        Objects.equals(this.conditionGroupEraLongTerm, covariateSettings.conditionGroupEraLongTerm) &&
        Objects.equals(this.conditionGroupEraMediumTerm, covariateSettings.conditionGroupEraMediumTerm) &&
        Objects.equals(this.conditionGroupEraShortTerm, covariateSettings.conditionGroupEraShortTerm) &&
        Objects.equals(this.conditionGroupEraOverlapping, covariateSettings.conditionGroupEraOverlapping) &&
        Objects.equals(this.conditionGroupEraStartLongTerm, covariateSettings.conditionGroupEraStartLongTerm) &&
        Objects.equals(this.conditionGroupEraStartMediumTerm, covariateSettings.conditionGroupEraStartMediumTerm) &&
        Objects.equals(this.conditionGroupEraStartShortTerm, covariateSettings.conditionGroupEraStartShortTerm) &&
        Objects.equals(this.drugExposureAnyTimePrior, covariateSettings.drugExposureAnyTimePrior) &&
        Objects.equals(this.drugExposureLongTerm, covariateSettings.drugExposureLongTerm) &&
        Objects.equals(this.drugExposureMediumTerm, covariateSettings.drugExposureMediumTerm) &&
        Objects.equals(this.drugExposureShortTerm, covariateSettings.drugExposureShortTerm) &&
        Objects.equals(this.drugEraAnyTimePrior, covariateSettings.drugEraAnyTimePrior) &&
        Objects.equals(this.drugEraLongTerm, covariateSettings.drugEraLongTerm) &&
        Objects.equals(this.drugEraMediumTerm, covariateSettings.drugEraMediumTerm) &&
        Objects.equals(this.drugEraShortTerm, covariateSettings.drugEraShortTerm) &&
        Objects.equals(this.drugEraOverlapping, covariateSettings.drugEraOverlapping) &&
        Objects.equals(this.drugEraStartLongTerm, covariateSettings.drugEraStartLongTerm) &&
        Objects.equals(this.drugEraStartMediumTerm, covariateSettings.drugEraStartMediumTerm) &&
        Objects.equals(this.drugEraStartShortTerm, covariateSettings.drugEraStartShortTerm) &&
        Objects.equals(this.drugGroupEraAnyTimePrior, covariateSettings.drugGroupEraAnyTimePrior) &&
        Objects.equals(this.drugGroupEraLongTerm, covariateSettings.drugGroupEraLongTerm) &&
        Objects.equals(this.drugGroupEraMediumTerm, covariateSettings.drugGroupEraMediumTerm) &&
        Objects.equals(this.drugGroupEraShortTerm, covariateSettings.drugGroupEraShortTerm) &&
        Objects.equals(this.drugGroupEraOverlapping, covariateSettings.drugGroupEraOverlapping) &&
        Objects.equals(this.drugGroupEraStartLongTerm, covariateSettings.drugGroupEraStartLongTerm) &&
        Objects.equals(this.drugGroupEraStartMediumTerm, covariateSettings.drugGroupEraStartMediumTerm) &&
        Objects.equals(this.drugGroupEraStartShortTerm, covariateSettings.drugGroupEraStartShortTerm) &&
        Objects.equals(this.procedureOccurrenceAnyTimePrior, covariateSettings.procedureOccurrenceAnyTimePrior) &&
        Objects.equals(this.procedureOccurrenceLongTerm, covariateSettings.procedureOccurrenceLongTerm) &&
        Objects.equals(this.procedureOccurrenceMediumTerm, covariateSettings.procedureOccurrenceMediumTerm) &&
        Objects.equals(this.procedureOccurrenceShortTerm, covariateSettings.procedureOccurrenceShortTerm) &&
        Objects.equals(this.deviceExposureAnyTimePrior, covariateSettings.deviceExposureAnyTimePrior) &&
        Objects.equals(this.deviceExposureLongTerm, covariateSettings.deviceExposureLongTerm) &&
        Objects.equals(this.deviceExposureMediumTerm, covariateSettings.deviceExposureMediumTerm) &&
        Objects.equals(this.deviceExposureShortTerm, covariateSettings.deviceExposureShortTerm) &&
        Objects.equals(this.measurementAnyTimePrior, covariateSettings.measurementAnyTimePrior) &&
        Objects.equals(this.measurementLongTerm, covariateSettings.measurementLongTerm) &&
        Objects.equals(this.measurementMediumTerm, covariateSettings.measurementMediumTerm) &&
        Objects.equals(this.measurementShortTerm, covariateSettings.measurementShortTerm) &&
        Objects.equals(this.measurementValueAnyTimePrior, covariateSettings.measurementValueAnyTimePrior) &&
        Objects.equals(this.measurementValueLongTerm, covariateSettings.measurementValueLongTerm) &&
        Objects.equals(this.measurementValueMediumTerm, covariateSettings.measurementValueMediumTerm) &&
        Objects.equals(this.measurementValueShortTerm, covariateSettings.measurementValueShortTerm) &&
        Objects.equals(this.measurementRangeGroupAnyTimePrior, covariateSettings.measurementRangeGroupAnyTimePrior) &&
        Objects.equals(this.measurementRangeGroupLongTerm, covariateSettings.measurementRangeGroupLongTerm) &&
        Objects.equals(this.measurementRangeGroupMediumTerm, covariateSettings.measurementRangeGroupMediumTerm) &&
        Objects.equals(this.measurementRangeGroupShortTerm, covariateSettings.measurementRangeGroupShortTerm) &&
        Objects.equals(this.observationAnyTimePrior, covariateSettings.observationAnyTimePrior) &&
        Objects.equals(this.observationLongTerm, covariateSettings.observationLongTerm) &&
        Objects.equals(this.observationMediumTerm, covariateSettings.observationMediumTerm) &&
        Objects.equals(this.observationShortTerm, covariateSettings.observationShortTerm) &&
        Objects.equals(this.charlsonIndex, covariateSettings.charlsonIndex) &&
        Objects.equals(this.dcsi, covariateSettings.dcsi) &&
        Objects.equals(this.chads2, covariateSettings.chads2) &&
        Objects.equals(this.chads2Vasc, covariateSettings.chads2Vasc) &&
        Objects.equals(this.distinctConditionCountLongTerm, covariateSettings.distinctConditionCountLongTerm) &&
        Objects.equals(this.distinctConditionCountMediumTerm, covariateSettings.distinctConditionCountMediumTerm) &&
        Objects.equals(this.distinctConditionCountShortTerm, covariateSettings.distinctConditionCountShortTerm) &&
        Objects.equals(this.distinctIngredientCountLongTerm, covariateSettings.distinctIngredientCountLongTerm) &&
        Objects.equals(this.distinctIngredientCountMediumTerm, covariateSettings.distinctIngredientCountMediumTerm) &&
        Objects.equals(this.distinctIngredientCountShortTerm, covariateSettings.distinctIngredientCountShortTerm) &&
        Objects.equals(this.distinctProcedureCountLongTerm, covariateSettings.distinctProcedureCountLongTerm) &&
        Objects.equals(this.distinctProcedureCountMediumTerm, covariateSettings.distinctProcedureCountMediumTerm) &&
        Objects.equals(this.distinctProcedureCountShortTerm, covariateSettings.distinctProcedureCountShortTerm) &&
        Objects.equals(this.distinctMeasurementCountLongTerm, covariateSettings.distinctMeasurementCountLongTerm) &&
        Objects.equals(this.distinctMeasurementCountMediumTerm, covariateSettings.distinctMeasurementCountMediumTerm) &&
        Objects.equals(this.distinctMeasurementCountShortTerm, covariateSettings.distinctMeasurementCountShortTerm) &&
        Objects.equals(this.distinctObservationCountLongTerm, covariateSettings.distinctObservationCountLongTerm) &&
        Objects.equals(this.distinctObservationCountMediumTerm, covariateSettings.distinctObservationCountMediumTerm) &&
        Objects.equals(this.distinctObservationCountShortTerm, covariateSettings.distinctObservationCountShortTerm) &&
        Objects.equals(this.visitCountLongTerm, covariateSettings.visitCountLongTerm) &&
        Objects.equals(this.visitCountMediumTerm, covariateSettings.visitCountMediumTerm) &&
        Objects.equals(this.visitCountShortTerm, covariateSettings.visitCountShortTerm) &&
        Objects.equals(this.visitConceptCountLongTerm, covariateSettings.visitConceptCountLongTerm) &&
        Objects.equals(this.visitConceptCountMediumTerm, covariateSettings.visitConceptCountMediumTerm) &&
        Objects.equals(this.visitConceptCountShortTerm, covariateSettings.visitConceptCountShortTerm) &&
        Objects.equals(this.longTermStartDays, covariateSettings.longTermStartDays) &&
        Objects.equals(this.mediumTermStartDays, covariateSettings.mediumTermStartDays) &&
        Objects.equals(this.shortTermStartDays, covariateSettings.shortTermStartDays) &&
        Objects.equals(this.endDays, covariateSettings.endDays) &&
        Objects.equals(this.includedCovariateConceptIds, covariateSettings.includedCovariateConceptIds) &&
        Objects.equals(this.addDescendantsToInclude, covariateSettings.addDescendantsToInclude) &&
        Objects.equals(this.excludedCovariateConceptIds, covariateSettings.excludedCovariateConceptIds) &&
        Objects.equals(this.addDescendantsToExclude, covariateSettings.addDescendantsToExclude) &&
        Objects.equals(this.includedCovariateIds, covariateSettings.includedCovariateIds) &&
        Objects.equals(this.attrFun, covariateSettings.attrFun) &&
        Objects.equals(this.attrClass, covariateSettings.attrClass);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temporal, demographicsGender, demographicsAge, demographicsAgeGroup, demographicsRace, demographicsEthnicity, demographicsIndexYear, demographicsIndexMonth, demographicsPriorObservationTime, demographicsPostObservationTime, demographicsTimeInCohort, demographicsIndexYearMonth, conditionOccurrenceAnyTimePrior, conditionOccurrenceLongTerm, conditionOccurrenceMediumTerm, conditionOccurrenceShortTerm, conditionOccurrencePrimaryInpatientAnyTimePrior, conditionOccurrencePrimaryInpatientLongTerm, conditionOccurrencePrimaryInpatientMediumTerm, conditionOccurrencePrimaryInpatientShortTerm, conditionEraAnyTimePrior, conditionEraLongTerm, conditionEraMediumTerm, conditionEraShortTerm, conditionEraOverlapping, conditionEraStartLongTerm, conditionEraStartMediumTerm, conditionEraStartShortTerm, conditionGroupEraAnyTimePrior, conditionGroupEraLongTerm, conditionGroupEraMediumTerm, conditionGroupEraShortTerm, conditionGroupEraOverlapping, conditionGroupEraStartLongTerm, conditionGroupEraStartMediumTerm, conditionGroupEraStartShortTerm, drugExposureAnyTimePrior, drugExposureLongTerm, drugExposureMediumTerm, drugExposureShortTerm, drugEraAnyTimePrior, drugEraLongTerm, drugEraMediumTerm, drugEraShortTerm, drugEraOverlapping, drugEraStartLongTerm, drugEraStartMediumTerm, drugEraStartShortTerm, drugGroupEraAnyTimePrior, drugGroupEraLongTerm, drugGroupEraMediumTerm, drugGroupEraShortTerm, drugGroupEraOverlapping, drugGroupEraStartLongTerm, drugGroupEraStartMediumTerm, drugGroupEraStartShortTerm, procedureOccurrenceAnyTimePrior, procedureOccurrenceLongTerm, procedureOccurrenceMediumTerm, procedureOccurrenceShortTerm, deviceExposureAnyTimePrior, deviceExposureLongTerm, deviceExposureMediumTerm, deviceExposureShortTerm, measurementAnyTimePrior, measurementLongTerm, measurementMediumTerm, measurementShortTerm, measurementValueAnyTimePrior, measurementValueLongTerm, measurementValueMediumTerm, measurementValueShortTerm, measurementRangeGroupAnyTimePrior, measurementRangeGroupLongTerm, measurementRangeGroupMediumTerm, measurementRangeGroupShortTerm, observationAnyTimePrior, observationLongTerm, observationMediumTerm, observationShortTerm, charlsonIndex, dcsi, chads2, chads2Vasc, distinctConditionCountLongTerm, distinctConditionCountMediumTerm, distinctConditionCountShortTerm, distinctIngredientCountLongTerm, distinctIngredientCountMediumTerm, distinctIngredientCountShortTerm, distinctProcedureCountLongTerm, distinctProcedureCountMediumTerm, distinctProcedureCountShortTerm, distinctMeasurementCountLongTerm, distinctMeasurementCountMediumTerm, distinctMeasurementCountShortTerm, distinctObservationCountLongTerm, distinctObservationCountMediumTerm, distinctObservationCountShortTerm, visitCountLongTerm, visitCountMediumTerm, visitCountShortTerm, visitConceptCountLongTerm, visitConceptCountMediumTerm, visitConceptCountShortTerm, longTermStartDays, mediumTermStartDays, shortTermStartDays, endDays, includedCovariateConceptIds, addDescendantsToInclude, excludedCovariateConceptIds, addDescendantsToExclude, includedCovariateIds, attrFun, attrClass);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CovariateSettings {\n");
    
    sb.append("    temporal: ").append(toIndentedString(temporal)).append("\n");
    sb.append("    demographicsGender: ").append(toIndentedString(demographicsGender)).append("\n");
    sb.append("    demographicsAge: ").append(toIndentedString(demographicsAge)).append("\n");
    sb.append("    demographicsAgeGroup: ").append(toIndentedString(demographicsAgeGroup)).append("\n");
    sb.append("    demographicsRace: ").append(toIndentedString(demographicsRace)).append("\n");
    sb.append("    demographicsEthnicity: ").append(toIndentedString(demographicsEthnicity)).append("\n");
    sb.append("    demographicsIndexYear: ").append(toIndentedString(demographicsIndexYear)).append("\n");
    sb.append("    demographicsIndexMonth: ").append(toIndentedString(demographicsIndexMonth)).append("\n");
    sb.append("    demographicsPriorObservationTime: ").append(toIndentedString(demographicsPriorObservationTime)).append("\n");
    sb.append("    demographicsPostObservationTime: ").append(toIndentedString(demographicsPostObservationTime)).append("\n");
    sb.append("    demographicsTimeInCohort: ").append(toIndentedString(demographicsTimeInCohort)).append("\n");
    sb.append("    demographicsIndexYearMonth: ").append(toIndentedString(demographicsIndexYearMonth)).append("\n");
    sb.append("    conditionOccurrenceAnyTimePrior: ").append(toIndentedString(conditionOccurrenceAnyTimePrior)).append("\n");
    sb.append("    conditionOccurrenceLongTerm: ").append(toIndentedString(conditionOccurrenceLongTerm)).append("\n");
    sb.append("    conditionOccurrenceMediumTerm: ").append(toIndentedString(conditionOccurrenceMediumTerm)).append("\n");
    sb.append("    conditionOccurrenceShortTerm: ").append(toIndentedString(conditionOccurrenceShortTerm)).append("\n");
    sb.append("    conditionOccurrencePrimaryInpatientAnyTimePrior: ").append(toIndentedString(conditionOccurrencePrimaryInpatientAnyTimePrior)).append("\n");
    sb.append("    conditionOccurrencePrimaryInpatientLongTerm: ").append(toIndentedString(conditionOccurrencePrimaryInpatientLongTerm)).append("\n");
    sb.append("    conditionOccurrencePrimaryInpatientMediumTerm: ").append(toIndentedString(conditionOccurrencePrimaryInpatientMediumTerm)).append("\n");
    sb.append("    conditionOccurrencePrimaryInpatientShortTerm: ").append(toIndentedString(conditionOccurrencePrimaryInpatientShortTerm)).append("\n");
    sb.append("    conditionEraAnyTimePrior: ").append(toIndentedString(conditionEraAnyTimePrior)).append("\n");
    sb.append("    conditionEraLongTerm: ").append(toIndentedString(conditionEraLongTerm)).append("\n");
    sb.append("    conditionEraMediumTerm: ").append(toIndentedString(conditionEraMediumTerm)).append("\n");
    sb.append("    conditionEraShortTerm: ").append(toIndentedString(conditionEraShortTerm)).append("\n");
    sb.append("    conditionEraOverlapping: ").append(toIndentedString(conditionEraOverlapping)).append("\n");
    sb.append("    conditionEraStartLongTerm: ").append(toIndentedString(conditionEraStartLongTerm)).append("\n");
    sb.append("    conditionEraStartMediumTerm: ").append(toIndentedString(conditionEraStartMediumTerm)).append("\n");
    sb.append("    conditionEraStartShortTerm: ").append(toIndentedString(conditionEraStartShortTerm)).append("\n");
    sb.append("    conditionGroupEraAnyTimePrior: ").append(toIndentedString(conditionGroupEraAnyTimePrior)).append("\n");
    sb.append("    conditionGroupEraLongTerm: ").append(toIndentedString(conditionGroupEraLongTerm)).append("\n");
    sb.append("    conditionGroupEraMediumTerm: ").append(toIndentedString(conditionGroupEraMediumTerm)).append("\n");
    sb.append("    conditionGroupEraShortTerm: ").append(toIndentedString(conditionGroupEraShortTerm)).append("\n");
    sb.append("    conditionGroupEraOverlapping: ").append(toIndentedString(conditionGroupEraOverlapping)).append("\n");
    sb.append("    conditionGroupEraStartLongTerm: ").append(toIndentedString(conditionGroupEraStartLongTerm)).append("\n");
    sb.append("    conditionGroupEraStartMediumTerm: ").append(toIndentedString(conditionGroupEraStartMediumTerm)).append("\n");
    sb.append("    conditionGroupEraStartShortTerm: ").append(toIndentedString(conditionGroupEraStartShortTerm)).append("\n");
    sb.append("    drugExposureAnyTimePrior: ").append(toIndentedString(drugExposureAnyTimePrior)).append("\n");
    sb.append("    drugExposureLongTerm: ").append(toIndentedString(drugExposureLongTerm)).append("\n");
    sb.append("    drugExposureMediumTerm: ").append(toIndentedString(drugExposureMediumTerm)).append("\n");
    sb.append("    drugExposureShortTerm: ").append(toIndentedString(drugExposureShortTerm)).append("\n");
    sb.append("    drugEraAnyTimePrior: ").append(toIndentedString(drugEraAnyTimePrior)).append("\n");
    sb.append("    drugEraLongTerm: ").append(toIndentedString(drugEraLongTerm)).append("\n");
    sb.append("    drugEraMediumTerm: ").append(toIndentedString(drugEraMediumTerm)).append("\n");
    sb.append("    drugEraShortTerm: ").append(toIndentedString(drugEraShortTerm)).append("\n");
    sb.append("    drugEraOverlapping: ").append(toIndentedString(drugEraOverlapping)).append("\n");
    sb.append("    drugEraStartLongTerm: ").append(toIndentedString(drugEraStartLongTerm)).append("\n");
    sb.append("    drugEraStartMediumTerm: ").append(toIndentedString(drugEraStartMediumTerm)).append("\n");
    sb.append("    drugEraStartShortTerm: ").append(toIndentedString(drugEraStartShortTerm)).append("\n");
    sb.append("    drugGroupEraAnyTimePrior: ").append(toIndentedString(drugGroupEraAnyTimePrior)).append("\n");
    sb.append("    drugGroupEraLongTerm: ").append(toIndentedString(drugGroupEraLongTerm)).append("\n");
    sb.append("    drugGroupEraMediumTerm: ").append(toIndentedString(drugGroupEraMediumTerm)).append("\n");
    sb.append("    drugGroupEraShortTerm: ").append(toIndentedString(drugGroupEraShortTerm)).append("\n");
    sb.append("    drugGroupEraOverlapping: ").append(toIndentedString(drugGroupEraOverlapping)).append("\n");
    sb.append("    drugGroupEraStartLongTerm: ").append(toIndentedString(drugGroupEraStartLongTerm)).append("\n");
    sb.append("    drugGroupEraStartMediumTerm: ").append(toIndentedString(drugGroupEraStartMediumTerm)).append("\n");
    sb.append("    drugGroupEraStartShortTerm: ").append(toIndentedString(drugGroupEraStartShortTerm)).append("\n");
    sb.append("    procedureOccurrenceAnyTimePrior: ").append(toIndentedString(procedureOccurrenceAnyTimePrior)).append("\n");
    sb.append("    procedureOccurrenceLongTerm: ").append(toIndentedString(procedureOccurrenceLongTerm)).append("\n");
    sb.append("    procedureOccurrenceMediumTerm: ").append(toIndentedString(procedureOccurrenceMediumTerm)).append("\n");
    sb.append("    procedureOccurrenceShortTerm: ").append(toIndentedString(procedureOccurrenceShortTerm)).append("\n");
    sb.append("    deviceExposureAnyTimePrior: ").append(toIndentedString(deviceExposureAnyTimePrior)).append("\n");
    sb.append("    deviceExposureLongTerm: ").append(toIndentedString(deviceExposureLongTerm)).append("\n");
    sb.append("    deviceExposureMediumTerm: ").append(toIndentedString(deviceExposureMediumTerm)).append("\n");
    sb.append("    deviceExposureShortTerm: ").append(toIndentedString(deviceExposureShortTerm)).append("\n");
    sb.append("    measurementAnyTimePrior: ").append(toIndentedString(measurementAnyTimePrior)).append("\n");
    sb.append("    measurementLongTerm: ").append(toIndentedString(measurementLongTerm)).append("\n");
    sb.append("    measurementMediumTerm: ").append(toIndentedString(measurementMediumTerm)).append("\n");
    sb.append("    measurementShortTerm: ").append(toIndentedString(measurementShortTerm)).append("\n");
    sb.append("    measurementValueAnyTimePrior: ").append(toIndentedString(measurementValueAnyTimePrior)).append("\n");
    sb.append("    measurementValueLongTerm: ").append(toIndentedString(measurementValueLongTerm)).append("\n");
    sb.append("    measurementValueMediumTerm: ").append(toIndentedString(measurementValueMediumTerm)).append("\n");
    sb.append("    measurementValueShortTerm: ").append(toIndentedString(measurementValueShortTerm)).append("\n");
    sb.append("    measurementRangeGroupAnyTimePrior: ").append(toIndentedString(measurementRangeGroupAnyTimePrior)).append("\n");
    sb.append("    measurementRangeGroupLongTerm: ").append(toIndentedString(measurementRangeGroupLongTerm)).append("\n");
    sb.append("    measurementRangeGroupMediumTerm: ").append(toIndentedString(measurementRangeGroupMediumTerm)).append("\n");
    sb.append("    measurementRangeGroupShortTerm: ").append(toIndentedString(measurementRangeGroupShortTerm)).append("\n");
    sb.append("    observationAnyTimePrior: ").append(toIndentedString(observationAnyTimePrior)).append("\n");
    sb.append("    observationLongTerm: ").append(toIndentedString(observationLongTerm)).append("\n");
    sb.append("    observationMediumTerm: ").append(toIndentedString(observationMediumTerm)).append("\n");
    sb.append("    observationShortTerm: ").append(toIndentedString(observationShortTerm)).append("\n");
    sb.append("    charlsonIndex: ").append(toIndentedString(charlsonIndex)).append("\n");
    sb.append("    dcsi: ").append(toIndentedString(dcsi)).append("\n");
    sb.append("    chads2: ").append(toIndentedString(chads2)).append("\n");
    sb.append("    chads2Vasc: ").append(toIndentedString(chads2Vasc)).append("\n");
    sb.append("    distinctConditionCountLongTerm: ").append(toIndentedString(distinctConditionCountLongTerm)).append("\n");
    sb.append("    distinctConditionCountMediumTerm: ").append(toIndentedString(distinctConditionCountMediumTerm)).append("\n");
    sb.append("    distinctConditionCountShortTerm: ").append(toIndentedString(distinctConditionCountShortTerm)).append("\n");
    sb.append("    distinctIngredientCountLongTerm: ").append(toIndentedString(distinctIngredientCountLongTerm)).append("\n");
    sb.append("    distinctIngredientCountMediumTerm: ").append(toIndentedString(distinctIngredientCountMediumTerm)).append("\n");
    sb.append("    distinctIngredientCountShortTerm: ").append(toIndentedString(distinctIngredientCountShortTerm)).append("\n");
    sb.append("    distinctProcedureCountLongTerm: ").append(toIndentedString(distinctProcedureCountLongTerm)).append("\n");
    sb.append("    distinctProcedureCountMediumTerm: ").append(toIndentedString(distinctProcedureCountMediumTerm)).append("\n");
    sb.append("    distinctProcedureCountShortTerm: ").append(toIndentedString(distinctProcedureCountShortTerm)).append("\n");
    sb.append("    distinctMeasurementCountLongTerm: ").append(toIndentedString(distinctMeasurementCountLongTerm)).append("\n");
    sb.append("    distinctMeasurementCountMediumTerm: ").append(toIndentedString(distinctMeasurementCountMediumTerm)).append("\n");
    sb.append("    distinctMeasurementCountShortTerm: ").append(toIndentedString(distinctMeasurementCountShortTerm)).append("\n");
    sb.append("    distinctObservationCountLongTerm: ").append(toIndentedString(distinctObservationCountLongTerm)).append("\n");
    sb.append("    distinctObservationCountMediumTerm: ").append(toIndentedString(distinctObservationCountMediumTerm)).append("\n");
    sb.append("    distinctObservationCountShortTerm: ").append(toIndentedString(distinctObservationCountShortTerm)).append("\n");
    sb.append("    visitCountLongTerm: ").append(toIndentedString(visitCountLongTerm)).append("\n");
    sb.append("    visitCountMediumTerm: ").append(toIndentedString(visitCountMediumTerm)).append("\n");
    sb.append("    visitCountShortTerm: ").append(toIndentedString(visitCountShortTerm)).append("\n");
    sb.append("    visitConceptCountLongTerm: ").append(toIndentedString(visitConceptCountLongTerm)).append("\n");
    sb.append("    visitConceptCountMediumTerm: ").append(toIndentedString(visitConceptCountMediumTerm)).append("\n");
    sb.append("    visitConceptCountShortTerm: ").append(toIndentedString(visitConceptCountShortTerm)).append("\n");
    sb.append("    longTermStartDays: ").append(toIndentedString(longTermStartDays)).append("\n");
    sb.append("    mediumTermStartDays: ").append(toIndentedString(mediumTermStartDays)).append("\n");
    sb.append("    shortTermStartDays: ").append(toIndentedString(shortTermStartDays)).append("\n");
    sb.append("    endDays: ").append(toIndentedString(endDays)).append("\n");
    sb.append("    includedCovariateConceptIds: ").append(toIndentedString(includedCovariateConceptIds)).append("\n");
    sb.append("    addDescendantsToInclude: ").append(toIndentedString(addDescendantsToInclude)).append("\n");
    sb.append("    excludedCovariateConceptIds: ").append(toIndentedString(excludedCovariateConceptIds)).append("\n");
    sb.append("    addDescendantsToExclude: ").append(toIndentedString(addDescendantsToExclude)).append("\n");
    sb.append("    includedCovariateIds: ").append(toIndentedString(includedCovariateIds)).append("\n");
    sb.append("    attrFun: ").append(toIndentedString(attrFun)).append("\n");
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
