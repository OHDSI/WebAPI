package org.ohdsi.webapi.featureextraction.specification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.analysis.featureextraction.design.*;
import org.ohdsi.webapi.RLangClassImpl;

/**
 *
 * @author asena5
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class CovariateSettingsImpl extends RLangClassImpl implements CovariateSettings {
  private Boolean temporal = false;
  private Boolean demographicsGender = false;
  private Boolean demographicsAge = false;
  private Boolean demographicsAgeGroup = false;
  private Boolean demographicsRace = false;
  private Boolean demographicsEthnicity = false;
  private Boolean demographicsIndexYear = false;
  private Boolean demographicsIndexMonth = false;
  private Boolean demographicsPriorObservationTime = false;
  private Boolean demographicsPostObservationTime = false;
  private Boolean demographicsTimeInCohort = false;
  private Boolean demographicsIndexYearMonth = false;
  private Boolean conditionOccurrenceAnyTimePrior = false;
  private Boolean conditionOccurrenceLongTerm = false;
  private Boolean conditionOccurrenceMediumTerm = false;
  private Boolean conditionOccurrenceShortTerm = false;
  private Boolean conditionOccurrencePrimaryInpatientAnyTimePrior = false;
  private Boolean conditionOccurrencePrimaryInpatientLongTerm = false;
  private Boolean conditionOccurrencePrimaryInpatientMediumTerm = false;
  private Boolean conditionOccurrencePrimaryInpatientShortTerm = false;
  private Boolean conditionEraAnyTimePrior = false;
  private Boolean conditionEraLongTerm = false;
  private Boolean conditionEraMediumTerm = false;
  private Boolean conditionEraShortTerm = false;
  private Boolean conditionEraOverlapping = false;
  private Boolean conditionEraStartLongTerm = false;
  private Boolean conditionEraStartMediumTerm = false;
  private Boolean conditionEraStartShortTerm = false;
  private Boolean conditionGroupEraAnyTimePrior = false;
  private Boolean conditionGroupEraLongTerm = false;
  private Boolean conditionGroupEraMediumTerm = false;
  private Boolean conditionGroupEraShortTerm = false;
  private Boolean conditionGroupEraOverlapping = false;
  private Boolean conditionGroupEraStartLongTerm = false;
  private Boolean conditionGroupEraStartMediumTerm = false;
  private Boolean conditionGroupEraStartShortTerm = false;
  private Boolean drugExposureAnyTimePrior = false;
  private Boolean drugExposureLongTerm = false;
  private Boolean drugExposureMediumTerm = false;
  private Boolean drugExposureShortTerm = false;
  private Boolean drugEraAnyTimePrior = false;
  private Boolean drugEraLongTerm = false;
  private Boolean drugEraMediumTerm = false;
  private Boolean drugEraShortTerm = false;
  private Boolean drugEraOverlapping = false;
  private Boolean drugEraStartLongTerm = false;
  private Boolean drugEraStartMediumTerm = false;
  private Boolean drugEraStartShortTerm = false;
  private Boolean drugGroupEraAnyTimePrior = false;
  private Boolean drugGroupEraLongTerm = false;
  private Boolean drugGroupEraMediumTerm = false;
  private Boolean drugGroupEraShortTerm = false;
  private Boolean drugGroupEraOverlapping = false;
  private Boolean drugGroupEraStartLongTerm = false;
  private Boolean drugGroupEraStartMediumTerm = false;
  private Boolean drugGroupEraStartShortTerm = false;
  private Boolean procedureOccurrenceAnyTimePrior = false;
  private Boolean procedureOccurrenceLongTerm = false;
  private Boolean procedureOccurrenceMediumTerm = false;
  private Boolean procedureOccurrenceShortTerm = false;
  private Boolean deviceExposureAnyTimePrior = false;
  private Boolean deviceExposureLongTerm = false;
  private Boolean deviceExposureMediumTerm = false;
  private Boolean deviceExposureShortTerm = false;
  private Boolean measurementAnyTimePrior = false;
  private Boolean measurementLongTerm = false;
  private Boolean measurementMediumTerm = false;
  private Boolean measurementShortTerm = false;
  private Boolean measurementValueAnyTimePrior = false;
  private Boolean measurementValueLongTerm = false;
  private Boolean measurementValueMediumTerm = false;
  private Boolean measurementValueShortTerm = false;
  private Boolean measurementRangeGroupAnyTimePrior = false;
  private Boolean measurementRangeGroupLongTerm = false;
  private Boolean measurementRangeGroupMediumTerm = false;
  private Boolean measurementRangeGroupShortTerm = false;
  private Boolean observationAnyTimePrior = false;
  private Boolean observationLongTerm = false;
  private Boolean observationMediumTerm = false;
  private Boolean observationShortTerm = false;
  private Boolean charlsonIndex = false;
  private Boolean dcsi = false;
  private Boolean chads2 = false;
  private Boolean chads2Vasc = false;
  private Boolean distinctConditionCountLongTerm = false;
  private Boolean distinctConditionCountMediumTerm = false;
  private Boolean distinctConditionCountShortTerm = false;
  private Boolean distinctIngredientCountLongTerm = false;
  private Boolean distinctIngredientCountMediumTerm = false;
  private Boolean distinctIngredientCountShortTerm = false;
  private Boolean distinctProcedureCountLongTerm = false;
  private Boolean distinctProcedureCountMediumTerm = false;
  private Boolean distinctProcedureCountShortTerm = false;
  private Boolean distinctMeasurementCountLongTerm = false;
  private Boolean distinctMeasurementCountMediumTerm = false;
  private Boolean distinctMeasurementCountShortTerm = false;
  private Boolean distinctObservationCountLongTerm = false;
  private Boolean distinctObservationCountMediumTerm = false;
  private Boolean distinctObservationCountShortTerm = false;
  private Boolean visitCountLongTerm = false;
  private Boolean visitCountMediumTerm = false;
  private Boolean visitCountShortTerm = false;
  private Boolean visitConceptCountLongTerm = false;
  private Boolean visitConceptCountMediumTerm = false;
  private Boolean visitConceptCountShortTerm = false;
  private Integer longTermStartDays = -365;
  private Integer mediumTermStartDays = -180;
  private Integer shortTermStartDays = -30;
  private Integer endDays = 0;
  private List<Long> includedCovariateConceptIds = null;
  private Boolean addDescendantsToInclude = false;
  private List<Long> excludedCovariateConceptIds = null;
  private Boolean addDescendantsToExclude = false;
  private List<Long> includedCovariateIds = null;
  private String attrFun = "getDbDefaultCovariateData";
  private String csAttrClass = "covariateSettings";

  /**
   * Construct temporal covariates  
   * @return temporal
   **/
  @Override
  public Boolean getTemporal() {
    return temporal;
  }

    /**
     *
     * @param temporal
     */
    public void setTemporal(Boolean temporal) {
    this.temporal = temporal;
  }

    
  /**
   * Gender of the subject. (analysis ID 1) 
   * @return demographicsGender
   **/
  @Override
  public Boolean getDemographicsGender() {
    return demographicsGender;
  }

    /**
     *
     * @param demographicsGender
     */
    public void setDemographicsGender(Boolean demographicsGender) {
    this.demographicsGender = demographicsGender;
  }

    
  /**
   * Age of the subject on the index date (in years). (analysis ID 2) 
   * @return demographicsAge
   **/
  @Override
  public Boolean getDemographicsAge() {
    return demographicsAge;
  }

    /**
     *
     * @param demographicsAge
     */
    public void setDemographicsAge(Boolean demographicsAge) {
    this.demographicsAge = demographicsAge;
  }

    
  /**
   * Age of the subject on the index date (in 5 year age groups) (analysis ID 3) 
   * @return demographicsAgeGroup
   **/
  @Override
  public Boolean getDemographicsAgeGroup() {
    return demographicsAgeGroup;
  }

    /**
     *
     * @param demographicsAgeGroup
     */
    public void setDemographicsAgeGroup(Boolean demographicsAgeGroup) {
    this.demographicsAgeGroup = demographicsAgeGroup;
  }

    
  /**
   * Race of the subject. (analysis ID 4) 
   * @return demographicsRace
   **/
  @Override
  public Boolean getDemographicsRace() {
    return demographicsRace;
  }

    /**
     *
     * @param demographicsRace
     */
    public void setDemographicsRace(Boolean demographicsRace) {
    this.demographicsRace = demographicsRace;
  }

  /**
   * Ethnicity of the subject. (analysis ID 5) 
   * @return demographicsEthnicity
   **/
  @Override
  public Boolean getDemographicsEthnicity() {
    return demographicsEthnicity;
  }

    /**
     *
     * @param demographicsEthnicity
     */
    public void setDemographicsEthnicity(Boolean demographicsEthnicity) {
    this.demographicsEthnicity = demographicsEthnicity;
  }

  /**
   * Year of the index date. (analysis ID 6) 
   * @return demographicsIndexYear
   **/
  @Override
  public Boolean getDemographicsIndexYear() {
    return demographicsIndexYear;
  }

    /**
     *
     * @param demographicsIndexYear
     */
    public void setDemographicsIndexYear(Boolean demographicsIndexYear) {
    this.demographicsIndexYear = demographicsIndexYear;
  }

  /**
   * Month of the index date. (analysis ID 7) 
   * @return demographicsIndexMonth
   **/
  @Override
  public Boolean getDemographicsIndexMonth() {
    return demographicsIndexMonth;
  }

    /**
     *
     * @param demographicsIndexMonth
     */
    public void setDemographicsIndexMonth(Boolean demographicsIndexMonth) {
    this.demographicsIndexMonth = demographicsIndexMonth;
  }

  /**
   * Number of continuous days of observation time preceding the index date. (analysis ID 8) 
   * @return demographicsPriorObservationTime
   **/
  @Override
  public Boolean getDemographicsPriorObservationTime() {
    return demographicsPriorObservationTime;
  }

    /**
     *
     * @param demographicsPriorObservationTime
     */
    public void setDemographicsPriorObservationTime(Boolean demographicsPriorObservationTime) {
    this.demographicsPriorObservationTime = demographicsPriorObservationTime;
  }

  /**
   * Number of continuous days of observation time following the index date. (analysis ID 9) 
   * @return demographicsPostObservationTime
   **/
  @Override
  public Boolean getDemographicsPostObservationTime() {
    return demographicsPostObservationTime;
  }

    /**
     *
     * @param demographicsPostObservationTime
     */
    public void setDemographicsPostObservationTime(Boolean demographicsPostObservationTime) {
    this.demographicsPostObservationTime = demographicsPostObservationTime;
  }

  /**
   * Number of days of observation time during cohort period. (analysis ID 10) 
   * @return demographicsTimeInCohort
   **/
  @Override
  public Boolean getDemographicsTimeInCohort() {
    return demographicsTimeInCohort;
  }

    /**
     *
     * @param demographicsTimeInCohort
     */
    public void setDemographicsTimeInCohort(Boolean demographicsTimeInCohort) {
    this.demographicsTimeInCohort = demographicsTimeInCohort;
  }

    
  /**
   * Both calendar year and month of the index date in a single variable. (analysis ID 11) 
   * @return demographicsIndexYearMonth
   **/
  @Override
  public Boolean getDemographicsIndexYearMonth() {
    return demographicsIndexYearMonth;
  }

    /**
     *
     * @param demographicsIndexYearMonth
     */
    public void setDemographicsIndexYearMonth(Boolean demographicsIndexYearMonth) {
    this.demographicsIndexYearMonth = demographicsIndexYearMonth;
  }

  /**
   * One covariate per condition in the condition_occurrence table starting any time prior to index. (analysis ID 101) 
   * @return conditionOccurrenceAnyTimePrior
   **/
  @Override
  public Boolean getConditionOccurrenceAnyTimePrior() {
    return conditionOccurrenceAnyTimePrior;
  }

    /**
     *
     * @param conditionOccurrenceAnyTimePrior
     */
    public void setConditionOccurrenceAnyTimePrior(Boolean conditionOccurrenceAnyTimePrior) {
    this.conditionOccurrenceAnyTimePrior = conditionOccurrenceAnyTimePrior;
  }

    
  /**
   * One covariate per condition in the condition_occurrence table starting in the long term window. (analysis ID 102) 
   * @return conditionOccurrenceLongTerm
   **/
  @Override
  public Boolean getConditionOccurrenceLongTerm() {
    return conditionOccurrenceLongTerm;
  }

    /**
     *
     * @param conditionOccurrenceLongTerm
     */
    public void setConditionOccurrenceLongTerm(Boolean conditionOccurrenceLongTerm) {
    this.conditionOccurrenceLongTerm = conditionOccurrenceLongTerm;
  }

    
  /**
   * One covariate per condition in the condition_occurrence table starting in the medium term window. (analysis ID 103) 
   * @return conditionOccurrenceMediumTerm
   **/
  @Override
  public Boolean getConditionOccurrenceMediumTerm() {
    return conditionOccurrenceMediumTerm;
  }

    /**
     *
     * @param conditionOccurrenceMediumTerm
     */
    public void setConditionOccurrenceMediumTerm(Boolean conditionOccurrenceMediumTerm) {
    this.conditionOccurrenceMediumTerm = conditionOccurrenceMediumTerm;
  }

  /**
   * One covariate per condition in the condition_occurrence table starting in the short term window. (analysis ID 104) 
   * @return conditionOccurrenceShortTerm
   **/
  @Override
  public Boolean getConditionOccurrenceShortTerm() {
    return conditionOccurrenceShortTerm;
  }

    /**
     *
     * @param conditionOccurrenceShortTerm
     */
    public void setConditionOccurrenceShortTerm(Boolean conditionOccurrenceShortTerm) {
    this.conditionOccurrenceShortTerm = conditionOccurrenceShortTerm;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting any time prior to index. (analysis ID 105) 
   * @return conditionOccurrencePrimaryInpatientAnyTimePrior
   **/
  @Override
  public Boolean getConditionOccurrencePrimaryInpatientAnyTimePrior() {
    return conditionOccurrencePrimaryInpatientAnyTimePrior;
  }

    /**
     *
     * @param conditionOccurrencePrimaryInpatientAnyTimePrior
     */
    public void setConditionOccurrencePrimaryInpatientAnyTimePrior(Boolean conditionOccurrencePrimaryInpatientAnyTimePrior) {
    this.conditionOccurrencePrimaryInpatientAnyTimePrior = conditionOccurrencePrimaryInpatientAnyTimePrior;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting in the long term window. (analysis ID 106) 
   * @return conditionOccurrencePrimaryInpatientLongTerm
   **/
  @Override
  public Boolean getConditionOccurrencePrimaryInpatientLongTerm() {
    return conditionOccurrencePrimaryInpatientLongTerm;
  }

    /**
     *
     * @param conditionOccurrencePrimaryInpatientLongTerm
     */
    public void setConditionOccurrencePrimaryInpatientLongTerm(Boolean conditionOccurrencePrimaryInpatientLongTerm) {
    this.conditionOccurrencePrimaryInpatientLongTerm = conditionOccurrencePrimaryInpatientLongTerm;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting in the medium term window. (analysis ID 107) 
   * @return conditionOccurrencePrimaryInpatientMediumTerm
   **/
  @Override
  public Boolean getConditionOccurrencePrimaryInpatientMediumTerm() {
    return conditionOccurrencePrimaryInpatientMediumTerm;
  }

    /**
     *
     * @param conditionOccurrencePrimaryInpatientMediumTerm
     */
    public void setConditionOccurrencePrimaryInpatientMediumTerm(Boolean conditionOccurrencePrimaryInpatientMediumTerm) {
    this.conditionOccurrencePrimaryInpatientMediumTerm = conditionOccurrencePrimaryInpatientMediumTerm;
  }

  /**
   * One covariate per condition observed in an inpatient setting in the condition_occurrence table starting in the short term window. (analysis ID 108) 
   * @return conditionOccurrencePrimaryInpatientShortTerm
   **/
  @Override
  public Boolean getConditionOccurrencePrimaryInpatientShortTerm() {
    return conditionOccurrencePrimaryInpatientShortTerm;
  }

    /**
     *
     * @param conditionOccurrencePrimaryInpatientShortTerm
     */
    public void setConditionOccurrencePrimaryInpatientShortTerm(Boolean conditionOccurrencePrimaryInpatientShortTerm) {
    this.conditionOccurrencePrimaryInpatientShortTerm = conditionOccurrencePrimaryInpatientShortTerm;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any time prior to index. (analysis ID 201) 
   * @return conditionEraAnyTimePrior
   **/
  @Override
  public Boolean getConditionEraAnyTimePrior() {
    return conditionEraAnyTimePrior;
  }

    /**
     *
     * @param conditionEraAnyTimePrior
     */
    public void setConditionEraAnyTimePrior(Boolean conditionEraAnyTimePrior) {
    this.conditionEraAnyTimePrior = conditionEraAnyTimePrior;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any part of the long term window. (analysis ID 202) 
   * @return conditionEraLongTerm
   **/
  @Override
  public Boolean getConditionEraLongTerm() {
    return conditionEraLongTerm;
  }

    /**
     *
     * @param conditionEraLongTerm
     */
    public void setConditionEraLongTerm(Boolean conditionEraLongTerm) {
    this.conditionEraLongTerm = conditionEraLongTerm;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any part of the medium term window. (analysis ID 203) 
   * @return conditionEraMediumTerm
   **/
  @Override
  public Boolean getConditionEraMediumTerm() {
    return conditionEraMediumTerm;
  }

    /**
     *
     * @param conditionEraMediumTerm
     */
    public void setConditionEraMediumTerm(Boolean conditionEraMediumTerm) {
    this.conditionEraMediumTerm = conditionEraMediumTerm;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with any part of the short term window. (analysis ID 204) 
   * @return conditionEraShortTerm
   **/
  @Override
  public Boolean getConditionEraShortTerm() {
    return conditionEraShortTerm;
  }

    /**
     *
     * @param conditionEraShortTerm
     */
    public void setConditionEraShortTerm(Boolean conditionEraShortTerm) {
    this.conditionEraShortTerm = conditionEraShortTerm;
  }

  /**
   * One covariate per condition in the condition_era table overlapping with the end of the risk window. (analysis ID 205) 
   * @return conditionEraOverlapping
   **/
  public Boolean getConditionEraOverlapping() {
    return conditionEraOverlapping;
  }

    /**
     *
     * @param conditionEraOverlapping
     */
    public void setConditionEraOverlapping(Boolean conditionEraOverlapping) {
    this.conditionEraOverlapping = conditionEraOverlapping;
  }

  /**
   * One covariate per condition in the condition_era table starting in the long term window. (analysis ID 206) 
   * @return conditionEraStartLongTerm
   **/
  @Override
  public Boolean getConditionEraStartLongTerm() {
    return conditionEraStartLongTerm;
  }

    /**
     *
     * @param conditionEraStartLongTerm
     */
    public void setConditionEraStartLongTerm(Boolean conditionEraStartLongTerm) {
    this.conditionEraStartLongTerm = conditionEraStartLongTerm;
  }

  /**
   * One covariate per condition in the condition_era table starting in the medium term window. (analysis ID 207) 
   * @return conditionEraStartMediumTerm
   **/
  @Override
  public Boolean getConditionEraStartMediumTerm() {
    return conditionEraStartMediumTerm;
  }

    /**
     *
     * @param conditionEraStartMediumTerm
     */
    public void setConditionEraStartMediumTerm(Boolean conditionEraStartMediumTerm) {
    this.conditionEraStartMediumTerm = conditionEraStartMediumTerm;
  }

  /**
   * One covariate per condition in the condition_era table starting in the short term window. (analysis ID 208) 
   * @return conditionEraStartShortTerm
   **/
  @Override
  public Boolean getConditionEraStartShortTerm() {
    return conditionEraStartShortTerm;
  }

    /**
     *
     * @param conditionEraStartShortTerm
     */
    public void setConditionEraStartShortTerm(Boolean conditionEraStartShortTerm) {
    this.conditionEraStartShortTerm = conditionEraStartShortTerm;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any time prior to index. (analysis ID 209) 
   * @return conditionGroupEraAnyTimePrior
   **/
  @Override
  public Boolean getConditionGroupEraAnyTimePrior() {
    return conditionGroupEraAnyTimePrior;
  }

    /**
     *
     * @param conditionGroupEraAnyTimePrior
     */
    public void setConditionGroupEraAnyTimePrior(Boolean conditionGroupEraAnyTimePrior) {
    this.conditionGroupEraAnyTimePrior = conditionGroupEraAnyTimePrior;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the long term window. (analysis ID 210) 
   * @return conditionGroupEraLongTerm
   **/
  @Override
  public Boolean getConditionGroupEraLongTerm() {
    return conditionGroupEraLongTerm;
  }

    /**
     *
     * @param conditionGroupEraLongTerm
     */
    public void setConditionGroupEraLongTerm(Boolean conditionGroupEraLongTerm) {
    this.conditionGroupEraLongTerm = conditionGroupEraLongTerm;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the medium term window. (analysis ID 211) 
   * @return conditionGroupEraMediumTerm
   **/
  @Override
  public Boolean getConditionGroupEraMediumTerm() {
    return conditionGroupEraMediumTerm;
  }

    /**
     *
     * @param conditionGroupEraMediumTerm
     */
    public void setConditionGroupEraMediumTerm(Boolean conditionGroupEraMediumTerm) {
    this.conditionGroupEraMediumTerm = conditionGroupEraMediumTerm;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with any part of the short term window. (analysis ID 212) 
   * @return conditionGroupEraShortTerm
   **/
  @Override
  public Boolean getConditionGroupEraShortTerm() {
    return conditionGroupEraShortTerm;
  }

    /**
     *
     * @param conditionGroupEraShortTerm
     */
    public void setConditionGroupEraShortTerm(Boolean conditionGroupEraShortTerm) {
    this.conditionGroupEraShortTerm = conditionGroupEraShortTerm;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table overlapping with the end of the risk window. (analysis ID 213) 
   * @return conditionGroupEraOverlapping
   **/
  @Override
  public Boolean getConditionGroupEraOverlapping() {
    return conditionGroupEraOverlapping;
  }

    /**
     *
     * @param conditionGroupEraOverlapping
     */
    public void setConditionGroupEraOverlapping(Boolean conditionGroupEraOverlapping) {
    this.conditionGroupEraOverlapping = conditionGroupEraOverlapping;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table starting in the long term window. (analysis ID 214) 
   * @return conditionGroupEraStartLongTerm
   **/
  @Override
  public Boolean getConditionGroupEraStartLongTerm() {
    return conditionGroupEraStartLongTerm;
  }

    /**
     *
     * @param conditionGroupEraStartLongTerm
     */
    public void setConditionGroupEraStartLongTerm(Boolean conditionGroupEraStartLongTerm) {
    this.conditionGroupEraStartLongTerm = conditionGroupEraStartLongTerm;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table starting in the medium term window. (analysis ID 215) 
   * @return conditionGroupEraStartMediumTerm
   **/
  @Override
  public Boolean getConditionGroupEraStartMediumTerm() {
    return conditionGroupEraStartMediumTerm;
  }

    /**
     *
     * @param conditionGroupEraStartMediumTerm
     */
    public void setConditionGroupEraStartMediumTerm(Boolean conditionGroupEraStartMediumTerm) {
    this.conditionGroupEraStartMediumTerm = conditionGroupEraStartMediumTerm;
  }

  /**
   * One covariate per condition era rolled up to groups in the condition_era table starting in the short term window. (analysis ID 216) 
   * @return conditionGroupEraStartShortTerm
   **/
  @Override
  public Boolean getConditionGroupEraStartShortTerm() {
    return conditionGroupEraStartShortTerm;
  }

    /**
     *
     * @param conditionGroupEraStartShortTerm
     */
    public void setConditionGroupEraStartShortTerm(Boolean conditionGroupEraStartShortTerm) {
    this.conditionGroupEraStartShortTerm = conditionGroupEraStartShortTerm;
  }

  /**
   * One covariate per drug in the drug_exposure table starting any time prior to index. (analysis ID 301) 
   * @return drugExposureAnyTimePrior
   **/
  @Override
  public Boolean getDrugExposureAnyTimePrior() {
    return drugExposureAnyTimePrior;
  }

    /**
     *
     * @param drugExposureAnyTimePrior
     */
    public void setDrugExposureAnyTimePrior(Boolean drugExposureAnyTimePrior) {
    this.drugExposureAnyTimePrior = drugExposureAnyTimePrior;
  }

  /**
   * One covariate per drug in the drug_exposure table starting in the long term window. (analysis ID 302) 
   * @return drugExposureLongTerm
   **/
  @Override
  public Boolean getDrugExposureLongTerm() {
    return drugExposureLongTerm;
  }

    /**
     *
     * @param drugExposureLongTerm
     */
    public void setDrugExposureLongTerm(Boolean drugExposureLongTerm) {
    this.drugExposureLongTerm = drugExposureLongTerm;
  }

  /**
   * One covariate per drug in the drug_exposure table starting in the medium term window. (analysis ID 303) 
   * @return drugExposureMediumTerm
   **/
  @Override
  public Boolean getDrugExposureMediumTerm() {
    return drugExposureMediumTerm;
  }

    /**
     *
     * @param drugExposureMediumTerm
     */
    public void setDrugExposureMediumTerm(Boolean drugExposureMediumTerm) {
    this.drugExposureMediumTerm = drugExposureMediumTerm;
  }

  /**
   * One covariate per drug in the drug_exposure table starting in the short term window. (analysis ID 304) 
   * @return drugExposureShortTerm
   **/
  @Override
  public Boolean getDrugExposureShortTerm() {
    return drugExposureShortTerm;
  }

    /**
     *
     * @param drugExposureShortTerm
     */
    public void setDrugExposureShortTerm(Boolean drugExposureShortTerm) {
    this.drugExposureShortTerm = drugExposureShortTerm;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any time prior to index. (analysis ID 401) 
   * @return drugEraAnyTimePrior
   **/
  @Override
  public Boolean getDrugEraAnyTimePrior() {
    return drugEraAnyTimePrior;
  }

    /**
     *
     * @param drugEraAnyTimePrior
     */
    public void setDrugEraAnyTimePrior(Boolean drugEraAnyTimePrior) {
    this.drugEraAnyTimePrior = drugEraAnyTimePrior;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any part of the long term window. (analysis ID 402) 
   * @return drugEraLongTerm
   **/
  @Override
  public Boolean getDrugEraLongTerm() {
    return drugEraLongTerm;
  }

    /**
     *
     * @param drugEraLongTerm
     */
    public void setDrugEraLongTerm(Boolean drugEraLongTerm) {
    this.drugEraLongTerm = drugEraLongTerm;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any part of the medium term window. (analysis ID 403) 
   * @return drugEraMediumTerm
   **/
  @Override
  public Boolean getDrugEraMediumTerm() {
    return drugEraMediumTerm;
  }

    /**
     *
     * @param drugEraMediumTerm
     */
    public void setDrugEraMediumTerm(Boolean drugEraMediumTerm) {
    this.drugEraMediumTerm = drugEraMediumTerm;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with any part of the short window. (analysis ID 404) 
   * @return drugEraShortTerm
   **/
  @Override
  public Boolean getDrugEraShortTerm() {
    return drugEraShortTerm;
  }

    /**
     *
     * @param drugEraShortTerm
     */
    public void setDrugEraShortTerm(Boolean drugEraShortTerm) {
    this.drugEraShortTerm = drugEraShortTerm;
  }

  /**
   * One covariate per drug in the drug_era table overlapping with the end of the risk window. (analysis ID 405) 
   * @return drugEraOverlapping
   **/
  @Override
  public Boolean getDrugEraOverlapping() {
    return drugEraOverlapping;
  }

    /**
     *
     * @param drugEraOverlapping
     */
    public void setDrugEraOverlapping(Boolean drugEraOverlapping) {
    this.drugEraOverlapping = drugEraOverlapping;
  }

  /**
   * One covariate per drug in the drug_era table starting in the long term window. (analysis ID 406) 
   * @return drugEraStartLongTerm
   **/
  @Override
  public Boolean getDrugEraStartLongTerm() {
    return drugEraStartLongTerm;
  }

    /**
     *
     * @param drugEraStartLongTerm
     */
    public void setDrugEraStartLongTerm(Boolean drugEraStartLongTerm) {
    this.drugEraStartLongTerm = drugEraStartLongTerm;
  }

  /**
   * One covariate per drug in the drug_era table starting in the medium term window. (analysis ID 407) 
   * @return drugEraStartMediumTerm
   **/
  @Override
  public Boolean getDrugEraStartMediumTerm() {
    return drugEraStartMediumTerm;
  }

    /**
     *
     * @param drugEraStartMediumTerm
     */
    public void setDrugEraStartMediumTerm(Boolean drugEraStartMediumTerm) {
    this.drugEraStartMediumTerm = drugEraStartMediumTerm;
  }

  /**
   * One covariate per drug in the drug_era table starting in the long short window. (analysis ID 408) 
   * @return drugEraStartShortTerm
   **/
  @Override
  public Boolean getDrugEraStartShortTerm() {
    return drugEraStartShortTerm;
  }

    /**
     *
     * @param drugEraStartShortTerm
     */
    public void setDrugEraStartShortTerm(Boolean drugEraStartShortTerm) {
    this.drugEraStartShortTerm = drugEraStartShortTerm;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any time prior to index. (analysis ID 409) 
   * @return drugGroupEraAnyTimePrior
   **/
  @Override
  public Boolean getDrugGroupEraAnyTimePrior() {
    return drugGroupEraAnyTimePrior;
  }

    /**
     *
     * @param drugGroupEraAnyTimePrior
     */
    public void setDrugGroupEraAnyTimePrior(Boolean drugGroupEraAnyTimePrior) {
    this.drugGroupEraAnyTimePrior = drugGroupEraAnyTimePrior;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the long term window. (analysis ID 410) 
   * @return drugGroupEraLongTerm
   **/
  @Override
  public Boolean getDrugGroupEraLongTerm() {
    return drugGroupEraLongTerm;
  }

    /**
     *
     * @param drugGroupEraLongTerm
     */
    public void setDrugGroupEraLongTerm(Boolean drugGroupEraLongTerm) {
    this.drugGroupEraLongTerm = drugGroupEraLongTerm;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the medium term window. (analysis ID 411) 
   * @return drugGroupEraMediumTerm
   **/
  @Override
  public Boolean getDrugGroupEraMediumTerm() {
    return drugGroupEraMediumTerm;
  }

    /**
     *
     * @param drugGroupEraMediumTerm
     */
    public void setDrugGroupEraMediumTerm(Boolean drugGroupEraMediumTerm) {
    this.drugGroupEraMediumTerm = drugGroupEraMediumTerm;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with any part of the short term window. (analysis ID 412) 
   * @return drugGroupEraShortTerm
   **/
  @Override
  public Boolean getDrugGroupEraShortTerm() {
    return drugGroupEraShortTerm;
  }

    /**
     *
     * @param drugGroupEraShortTerm
     */
    public void setDrugGroupEraShortTerm(Boolean drugGroupEraShortTerm) {
    this.drugGroupEraShortTerm = drugGroupEraShortTerm;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table overlapping with the end of the risk window. (analysis ID 413) 
   * @return drugGroupEraOverlapping
   **/
  @Override
  public Boolean getDrugGroupEraOverlapping() {
    return drugGroupEraOverlapping;
  }

    /**
     *
     * @param drugGroupEraOverlapping
     */
    public void setDrugGroupEraOverlapping(Boolean drugGroupEraOverlapping) {
    this.drugGroupEraOverlapping = drugGroupEraOverlapping;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table starting in the long term window. (analysis ID 414) 
   * @return drugGroupEraStartLongTerm
   **/
  @Override
  public Boolean getDrugGroupEraStartLongTerm() {
    return drugGroupEraStartLongTerm;
  }

    /**
     *
     * @param drugGroupEraStartLongTerm
     */
    public void setDrugGroupEraStartLongTerm(Boolean drugGroupEraStartLongTerm) {
    this.drugGroupEraStartLongTerm = drugGroupEraStartLongTerm;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table starting in the medium term window. (analysis ID 415) 
   * @return drugGroupEraStartMediumTerm
   **/
  @Override
  public Boolean getDrugGroupEraStartMediumTerm() {
    return drugGroupEraStartMediumTerm;
  }

    /**
     *
     * @param drugGroupEraStartMediumTerm
     */
    public void setDrugGroupEraStartMediumTerm(Boolean drugGroupEraStartMediumTerm) {
    this.drugGroupEraStartMediumTerm = drugGroupEraStartMediumTerm;
  }

  /**
   * One covariate per drug rolled up to ATC groups in the drug_era table starting in the short term window. (analysis ID 416) 
   * @return drugGroupEraStartShortTerm
   **/
  @Override
  public Boolean getDrugGroupEraStartShortTerm() {
    return drugGroupEraStartShortTerm;
  }

    /**
     *
     * @param drugGroupEraStartShortTerm
     */
    public void setDrugGroupEraStartShortTerm(Boolean drugGroupEraStartShortTerm) {
    this.drugGroupEraStartShortTerm = drugGroupEraStartShortTerm;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table any time prior to index. (analysis ID 501) 
   * @return procedureOccurrenceAnyTimePrior
   **/
  @Override
  public Boolean getProcedureOccurrenceAnyTimePrior() {
    return procedureOccurrenceAnyTimePrior;
  }

    /**
     *
     * @param procedureOccurrenceAnyTimePrior
     */
    public void setProcedureOccurrenceAnyTimePrior(Boolean procedureOccurrenceAnyTimePrior) {
    this.procedureOccurrenceAnyTimePrior = procedureOccurrenceAnyTimePrior;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table in the long term window. (analysis ID 502) 
   * @return procedureOccurrenceLongTerm
   **/
  @Override
  public Boolean getProcedureOccurrenceLongTerm() {
    return procedureOccurrenceLongTerm;
  }

    /**
     *
     * @param procedureOccurrenceLongTerm
     */
    public void setProcedureOccurrenceLongTerm(Boolean procedureOccurrenceLongTerm) {
    this.procedureOccurrenceLongTerm = procedureOccurrenceLongTerm;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table in the medium term window. (analysis ID 503) 
   * @return procedureOccurrenceMediumTerm
   **/
  @Override
  public Boolean getProcedureOccurrenceMediumTerm() {
    return procedureOccurrenceMediumTerm;
  }

    /**
     *
     * @param procedureOccurrenceMediumTerm
     */
    public void setProcedureOccurrenceMediumTerm(Boolean procedureOccurrenceMediumTerm) {
    this.procedureOccurrenceMediumTerm = procedureOccurrenceMediumTerm;
  }

  /**
   * One covariate per procedure in the procedure_occurrence table in the short term window. (analysis ID 504) 
   * @return procedureOccurrenceShortTerm
   **/
  @Override
  public Boolean getProcedureOccurrenceShortTerm() {
    return procedureOccurrenceShortTerm;
  }

    /**
     *
     * @param procedureOccurrenceShortTerm
     */
    public void setProcedureOccurrenceShortTerm(Boolean procedureOccurrenceShortTerm) {
    this.procedureOccurrenceShortTerm = procedureOccurrenceShortTerm;
  }

  /**
   * One covariate per device in the device exposure table starting any time prior to index. (analysis ID 601) 
   * @return deviceExposureAnyTimePrior
   **/
  @Override
  public Boolean getDeviceExposureAnyTimePrior() {
    return deviceExposureAnyTimePrior;
  }

    /**
     *
     * @param deviceExposureAnyTimePrior
     */
    public void setDeviceExposureAnyTimePrior(Boolean deviceExposureAnyTimePrior) {
    this.deviceExposureAnyTimePrior = deviceExposureAnyTimePrior;
  }

  /**
   * One covariate per device in the device exposure table starting in the long term window. (analysis ID 602) 
   * @return deviceExposureLongTerm
   **/
  @Override
  public Boolean getDeviceExposureLongTerm() {
    return deviceExposureLongTerm;
  }

    /**
     *
     * @param deviceExposureLongTerm
     */
    public void setDeviceExposureLongTerm(Boolean deviceExposureLongTerm) {
    this.deviceExposureLongTerm = deviceExposureLongTerm;
  }

  /**
   * One covariate per device in the device exposure table starting in the medium term window. (analysis ID 603) 
   * @return deviceExposureMediumTerm
   **/
  @Override
  public Boolean getDeviceExposureMediumTerm() {
    return deviceExposureMediumTerm;
  }

    /**
     *
     * @param deviceExposureMediumTerm
     */
    public void setDeviceExposureMediumTerm(Boolean deviceExposureMediumTerm) {
    this.deviceExposureMediumTerm = deviceExposureMediumTerm;
  }

  /**
   * One covariate per device in the device exposure table starting in the short term window. (analysis ID 604) 
   * @return deviceExposureShortTerm
   **/
  @Override
  public Boolean getDeviceExposureShortTerm() {
    return deviceExposureShortTerm;
  }

    /**
     *
     * @param deviceExposureShortTerm
     */
    public void setDeviceExposureShortTerm(Boolean deviceExposureShortTerm) {
    this.deviceExposureShortTerm = deviceExposureShortTerm;
  }

  /**
   * One covariate per measurement in the measurement table any time prior to index. (analysis ID 701) 
   * @return measurementAnyTimePrior
   **/
  @Override
  public Boolean getMeasurementAnyTimePrior() {
    return measurementAnyTimePrior;
  }

    /**
     *
     * @param measurementAnyTimePrior
     */
    public void setMeasurementAnyTimePrior(Boolean measurementAnyTimePrior) {
    this.measurementAnyTimePrior = measurementAnyTimePrior;
  }

  /**
   * One covariate per measurement in the measurement table in the long term window. (analysis ID 702) 
   * @return measurementLongTerm
   **/
  @Override
  public Boolean getMeasurementLongTerm() {
    return measurementLongTerm;
  }

    /**
     *
     * @param measurementLongTerm
     */
    public void setMeasurementLongTerm(Boolean measurementLongTerm) {
    this.measurementLongTerm = measurementLongTerm;
  }

  /**
   * One covariate per measurement in the measurement table in the medium term window. (analysis ID 703) 
   * @return measurementMediumTerm
   **/
  @Override
  public Boolean getMeasurementMediumTerm() {
    return measurementMediumTerm;
  }

    /**
     *
     * @param measurementMediumTerm
     */
    public void setMeasurementMediumTerm(Boolean measurementMediumTerm) {
    this.measurementMediumTerm = measurementMediumTerm;
  }

  /**
   * One covariate per measurement in the measurement table in the short term window. (analysis ID 704) 
   * @return measurementShortTerm
   **/
  @Override
  public Boolean getMeasurementShortTerm() {
    return measurementShortTerm;
  }

    /**
     *
     * @param measurementShortTerm
     */
    public void setMeasurementShortTerm(Boolean measurementShortTerm) {
    this.measurementShortTerm = measurementShortTerm;
  }

  /**
   * One covariate containing the value per measurement-unit combination any time prior to index. (analysis ID 705) 
   * @return measurementValueAnyTimePrior
   **/
  @Override
  public Boolean getMeasurementValueAnyTimePrior() {
    return measurementValueAnyTimePrior;
  }

    /**
     *
     * @param measurementValueAnyTimePrior
     */
    public void setMeasurementValueAnyTimePrior(Boolean measurementValueAnyTimePrior) {
    this.measurementValueAnyTimePrior = measurementValueAnyTimePrior;
  }

  /**
   * One covariate containing the value per measurement-unit combination in the long term window. (analysis ID 706) 
   * @return measurementValueLongTerm
   **/
  @Override
  public Boolean getMeasurementValueLongTerm() {
    return measurementValueLongTerm;
  }

    /**
     *
     * @param measurementValueLongTerm
     */
    public void setMeasurementValueLongTerm(Boolean measurementValueLongTerm) {
    this.measurementValueLongTerm = measurementValueLongTerm;
  }

  /**
   * One covariate containing the value per measurement-unit combination in the medium term window. (analysis ID 707) 
   * @return measurementValueMediumTerm
   **/
  @Override
  public Boolean getMeasurementValueMediumTerm() {
    return measurementValueMediumTerm;
  }

    /**
     *
     * @param measurementValueMediumTerm
     */
    public void setMeasurementValueMediumTerm(Boolean measurementValueMediumTerm) {
    this.measurementValueMediumTerm = measurementValueMediumTerm;
  }

  /**
   * One covariate containing the value per measurement-unit combination in the short term window. (analysis ID 708) 
   * @return measurementValueShortTerm
   **/
  @Override
  public Boolean getMeasurementValueShortTerm() {
    return measurementValueShortTerm;
  }

    /**
     *
     * @param measurementValueShortTerm
     */
    public void setMeasurementValueShortTerm(Boolean measurementValueShortTerm) {
    this.measurementValueShortTerm = measurementValueShortTerm;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range any time prior to index. (analysis ID 709) 
   * @return measurementRangeGroupAnyTimePrior
   **/
  @Override
  public Boolean getMeasurementRangeGroupAnyTimePrior() {
    return measurementRangeGroupAnyTimePrior;
  }

    /**
     *
     * @param measurementRangeGroupAnyTimePrior
     */
    public void setMeasurementRangeGroupAnyTimePrior(Boolean measurementRangeGroupAnyTimePrior) {
    this.measurementRangeGroupAnyTimePrior = measurementRangeGroupAnyTimePrior;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range in the long term window. (analysis ID 710) 
   * @return measurementRangeGroupLongTerm
   **/
  @Override
  public Boolean getMeasurementRangeGroupLongTerm() {
    return measurementRangeGroupLongTerm;
  }

    /**
     *
     * @param measurementRangeGroupLongTerm
     */
    public void setMeasurementRangeGroupLongTerm(Boolean measurementRangeGroupLongTerm) {
    this.measurementRangeGroupLongTerm = measurementRangeGroupLongTerm;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range in the medium term window. (analysis ID 711) 
   * @return measurementRangeGroupMediumTerm
   **/
  @Override
  public Boolean getMeasurementRangeGroupMediumTerm() {
    return measurementRangeGroupMediumTerm;
  }

    /**
     *
     * @param measurementRangeGroupMediumTerm
     */
    public void setMeasurementRangeGroupMediumTerm(Boolean measurementRangeGroupMediumTerm) {
    this.measurementRangeGroupMediumTerm = measurementRangeGroupMediumTerm;
  }

  /**
   * Covariates indicating whether measurements are below, within, or above normal range in the short term window. (analysis ID 712) 
   * @return measurementRangeGroupShortTerm
   **/
  @Override
  public Boolean getMeasurementRangeGroupShortTerm() {
    return measurementRangeGroupShortTerm;
  }

    /**
     *
     * @param measurementRangeGroupShortTerm
     */
    public void setMeasurementRangeGroupShortTerm(Boolean measurementRangeGroupShortTerm) {
    this.measurementRangeGroupShortTerm = measurementRangeGroupShortTerm;
  }

  /**
   * One covariate per observation in the observation table any time prior to index. (analysis ID 801) 
   * @return observationAnyTimePrior
   **/
  @Override
  public Boolean getObservationAnyTimePrior() {
    return observationAnyTimePrior;
  }

    /**
     *
     * @param observationAnyTimePrior
     */
    public void setObservationAnyTimePrior(Boolean observationAnyTimePrior) {
    this.observationAnyTimePrior = observationAnyTimePrior;
  }

  /**
   * One covariate per observation in the observation table in the long term window. (analysis ID 802) 
   * @return observationLongTerm
   **/
  @Override
  public Boolean getObservationLongTerm() {
    return observationLongTerm;
  }

    /**
     *
     * @param observationLongTerm
     */
    public void setObservationLongTerm(Boolean observationLongTerm) {
    this.observationLongTerm = observationLongTerm;
  }

  /**
   * One covariate per observation in the observation table in the medium term window. (analysis ID 803) 
   * @return observationMediumTerm
   **/
  @Override
  public Boolean getObservationMediumTerm() {
    return observationMediumTerm;
  }

    /**
     *
     * @param observationMediumTerm
     */
    public void setObservationMediumTerm(Boolean observationMediumTerm) {
    this.observationMediumTerm = observationMediumTerm;
  }

  /**
   * One covariate per observation in the observation table in the short term window. (analysis ID 804) 
   * @return observationShortTerm
   **/
  @Override
  public Boolean getObservationShortTerm() {
    return observationShortTerm;
  }

    /**
     *
     * @param observationShortTerm
     */
    public void setObservationShortTerm(Boolean observationShortTerm) {
    this.observationShortTerm = observationShortTerm;
  }

  /**
   * The Charlson comorbidity index (Romano adaptation) using all conditions prior to the window end. (analysis ID 901) 
   * @return charlsonIndex
   **/
  @Override
  public Boolean getCharlsonIndex() {
    return charlsonIndex;
  }

    /**
     *
     * @param charlsonIndex
     */
    public void setCharlsonIndex(Boolean charlsonIndex) {
    this.charlsonIndex = charlsonIndex;
  }

  /**
   * The Diabetes Comorbidity Severity Index (DCSI) using all conditions prior to the window end. (analysis ID 902) 
   * @return dcsi
   **/
  @Override
  public Boolean getDcsi() {
    return dcsi;
  }

    /**
     *
     * @param dcsi
     */
    public void setDcsi(Boolean dcsi) {
    this.dcsi = dcsi;
  }

  /**
   * The CHADS2 score using all conditions prior to the window end. (analysis ID 903) 
   * @return chads2
   **/
  @Override
  public Boolean getChads2() {
    return chads2;
  }

    /**
     *
     * @param chads2
     */
    public void setChads2(Boolean chads2) {
    this.chads2 = chads2;
  }

  /**
   * The CHADS2VASc score using all conditions prior to the window end. (analysis ID 904) 
   * @return chads2Vasc
   **/
  @Override
  public Boolean getChads2Vasc() {
    return chads2Vasc;
  }

    /**
     *
     * @param chads2Vasc
     */
    public void setChads2Vasc(Boolean chads2Vasc) {
    this.chads2Vasc = chads2Vasc;
  }

  /**
   * The number of distinct condition concepts observed in the long term window. (analysis ID 905) 
   * @return distinctConditionCountLongTerm
   **/
  @Override
  public Boolean getDistinctConditionCountLongTerm() {
    return distinctConditionCountLongTerm;
  }

    /**
     *
     * @param distinctConditionCountLongTerm
     */
    public void setDistinctConditionCountLongTerm(Boolean distinctConditionCountLongTerm) {
    this.distinctConditionCountLongTerm = distinctConditionCountLongTerm;
  }

  /**
   * The number of distinct condition concepts observed in the medium term window. (analysis ID 906) 
   * @return distinctConditionCountMediumTerm
   **/
  @Override
  public Boolean getDistinctConditionCountMediumTerm() {
    return distinctConditionCountMediumTerm;
  }

    /**
     *
     * @param distinctConditionCountMediumTerm
     */
    public void setDistinctConditionCountMediumTerm(Boolean distinctConditionCountMediumTerm) {
    this.distinctConditionCountMediumTerm = distinctConditionCountMediumTerm;
  }

  /**
   * The number of distinct condition concepts observed in the short term window. (analysis ID 907) 
   * @return distinctConditionCountShortTerm
   **/
  @Override
  public Boolean getDistinctConditionCountShortTerm() {
    return distinctConditionCountShortTerm;
  }

    /**
     *
     * @param distinctConditionCountShortTerm
     */
    public void setDistinctConditionCountShortTerm(Boolean distinctConditionCountShortTerm) {
    this.distinctConditionCountShortTerm = distinctConditionCountShortTerm;
  }

  /**
   * The number of distinct ingredients observed in the long term window. (analysis ID 908) 
   * @return distinctIngredientCountLongTerm
   **/
  @Override
  public Boolean getDistinctIngredientCountLongTerm() {
    return distinctIngredientCountLongTerm;
  }

    /**
     *
     * @param distinctIngredientCountLongTerm
     */
    public void setDistinctIngredientCountLongTerm(Boolean distinctIngredientCountLongTerm) {
    this.distinctIngredientCountLongTerm = distinctIngredientCountLongTerm;
  }

  /**
   * The number of distinct ingredients observed in the medium term window. (analysis ID 909) 
   * @return distinctIngredientCountMediumTerm
   **/
  @Override
  public Boolean getDistinctIngredientCountMediumTerm() {
    return distinctIngredientCountMediumTerm;
  }

    /**
     *
     * @param distinctIngredientCountMediumTerm
     */
    public void setDistinctIngredientCountMediumTerm(Boolean distinctIngredientCountMediumTerm) {
    this.distinctIngredientCountMediumTerm = distinctIngredientCountMediumTerm;
  }

  /**
   * The number of distinct ingredients observed in the short term window. (analysis ID 910) 
   * @return distinctIngredientCountShortTerm
   **/
  @Override
  public Boolean getDistinctIngredientCountShortTerm() {
    return distinctIngredientCountShortTerm;
  }

    /**
     *
     * @param distinctIngredientCountShortTerm
     */
    public void setDistinctIngredientCountShortTerm(Boolean distinctIngredientCountShortTerm) {
    this.distinctIngredientCountShortTerm = distinctIngredientCountShortTerm;
  }

    
  /**
   * The number of distinct procedures observed in the long term window. (analysis ID 911) 
   * @return distinctProcedureCountLongTerm
   **/
  @Override
  public Boolean getDistinctProcedureCountLongTerm() {
    return distinctProcedureCountLongTerm;
  }

    /**
     *
     * @param distinctProcedureCountLongTerm
     */
    public void setDistinctProcedureCountLongTerm(Boolean distinctProcedureCountLongTerm) {
    this.distinctProcedureCountLongTerm = distinctProcedureCountLongTerm;
  }

  /**
   * The number of distinct procedures observed in the medium term window. (analysis ID 912) 
   * @return distinctProcedureCountMediumTerm
   **/
  @Override
  public Boolean getDistinctProcedureCountMediumTerm() {
    return distinctProcedureCountMediumTerm;
  }

    /**
     *
     * @param distinctProcedureCountMediumTerm
     */
    public void setDistinctProcedureCountMediumTerm(Boolean distinctProcedureCountMediumTerm) {
    this.distinctProcedureCountMediumTerm = distinctProcedureCountMediumTerm;
  }

  /**
   * The number of distinct procedures observed in the short term window. (analysis ID 913) 
   * @return distinctProcedureCountShortTerm
   **/
  @Override
  public Boolean getDistinctProcedureCountShortTerm() {
    return distinctProcedureCountShortTerm;
  }

    /**
     *
     * @param distinctProcedureCountShortTerm
     */
    public void setDistinctProcedureCountShortTerm(Boolean distinctProcedureCountShortTerm) {
    this.distinctProcedureCountShortTerm = distinctProcedureCountShortTerm;
  }

  /**
   * The number of distinct measurements observed in the long term window. (analysis ID 914) 
   * @return distinctMeasurementCountLongTerm
   **/
  @Override
  public Boolean getDistinctMeasurementCountLongTerm() {
    return distinctMeasurementCountLongTerm;
  }

    /**
     *
     * @param distinctMeasurementCountLongTerm
     */
    public void setDistinctMeasurementCountLongTerm(Boolean distinctMeasurementCountLongTerm) {
    this.distinctMeasurementCountLongTerm = distinctMeasurementCountLongTerm;
  }

  /**
   * The number of distinct measurements observed in the medium term window. (analysis ID 915) 
   * @return distinctMeasurementCountMediumTerm
   **/
  @Override
  public Boolean getDistinctMeasurementCountMediumTerm() {
    return distinctMeasurementCountMediumTerm;
  }

    /**
     *
     * @param distinctMeasurementCountMediumTerm
     */
    public void setDistinctMeasurementCountMediumTerm(Boolean distinctMeasurementCountMediumTerm) {
    this.distinctMeasurementCountMediumTerm = distinctMeasurementCountMediumTerm;
  }

  /**
   * The number of distinct measurements observed in the short term window. (analysis ID 916) 
   * @return distinctMeasurementCountShortTerm
   **/
  @Override
  public Boolean getDistinctMeasurementCountShortTerm() {
    return distinctMeasurementCountShortTerm;
  }

    /**
     *
     * @param distinctMeasurementCountShortTerm
     */
    public void setDistinctMeasurementCountShortTerm(Boolean distinctMeasurementCountShortTerm) {
    this.distinctMeasurementCountShortTerm = distinctMeasurementCountShortTerm;
  }

  /**
   * The number of distinct observations observed in the long term window. (analysis ID 917) 
   * @return distinctObservationCountLongTerm
   **/
  @Override
  public Boolean getDistinctObservationCountLongTerm() {
    return distinctObservationCountLongTerm;
  }

    /**
     *
     * @param distinctObservationCountLongTerm
     */
    public void setDistinctObservationCountLongTerm(Boolean distinctObservationCountLongTerm) {
    this.distinctObservationCountLongTerm = distinctObservationCountLongTerm;
  }

  /**
   * The number of distinct observations observed in the medium term window. (analysis ID 918) 
   * @return distinctObservationCountMediumTerm
   **/
  @Override
  public Boolean getDistinctObservationCountMediumTerm() {
    return distinctObservationCountMediumTerm;
  }

    /**
     *
     * @param distinctObservationCountMediumTerm
     */
    public void setDistinctObservationCountMediumTerm(Boolean distinctObservationCountMediumTerm) {
    this.distinctObservationCountMediumTerm = distinctObservationCountMediumTerm;
  }

  /**
   * The number of distinct observations observed in the short term window. (analysis ID 919) 
   * @return distinctObservationCountShortTerm
   **/
  @Override
  public Boolean getDistinctObservationCountShortTerm() {
    return distinctObservationCountShortTerm;
  }

    /**
     *
     * @param distinctObservationCountShortTerm
     */
    public void setDistinctObservationCountShortTerm(Boolean distinctObservationCountShortTerm) {
    this.distinctObservationCountShortTerm = distinctObservationCountShortTerm;
  }

  /**
   * The number of visits observed in the long term window. (analysis ID 920) 
   * @return visitCountLongTerm
   **/
  @Override
  public Boolean getVisitCountLongTerm() {
    return visitCountLongTerm;
  }

    /**
     *
     * @param visitCountLongTerm
     */
    public void setVisitCountLongTerm(Boolean visitCountLongTerm) {
    this.visitCountLongTerm = visitCountLongTerm;
  }

  /**
   * The number of visits observed in the medium term window. (analysis ID 921) 
   * @return visitCountMediumTerm
   **/
  @Override
  public Boolean getVisitCountMediumTerm() {
    return visitCountMediumTerm;
  }

    /**
     *
     * @param visitCountMediumTerm
     */
    public void setVisitCountMediumTerm(Boolean visitCountMediumTerm) {
    this.visitCountMediumTerm = visitCountMediumTerm;
  }

  /**
   * The number of visits observed in the short term window. (analysis ID 922) 
   * @return visitCountShortTerm
   **/
  @Override
  public Boolean getVisitCountShortTerm() {
    return visitCountShortTerm;
  }

    /**
     *
     * @param visitCountShortTerm
     */
    public void setVisitCountShortTerm(Boolean visitCountShortTerm) {
    this.visitCountShortTerm = visitCountShortTerm;
  }

  /**
   * The number of visits observed in the long term window, stratified by visit concept ID. (analysis ID 923) 
   * @return visitConceptCountLongTerm
   **/
  @Override
  public Boolean getVisitConceptCountLongTerm() {
    return visitConceptCountLongTerm;
  }

    /**
     *
     * @param visitConceptCountLongTerm
     */
    public void setVisitConceptCountLongTerm(Boolean visitConceptCountLongTerm) {
    this.visitConceptCountLongTerm = visitConceptCountLongTerm;
  }

  /**
   * The number of visits observed in the medium term window, stratified by visit concept ID. (analysis ID 924) 
   * @return visitConceptCountMediumTerm
   **/
  @Override
  public Boolean getVisitConceptCountMediumTerm() {
    return visitConceptCountMediumTerm;
  }

    /**
     *
     * @param visitConceptCountMediumTerm
     */
    public void setVisitConceptCountMediumTerm(Boolean visitConceptCountMediumTerm) {
    this.visitConceptCountMediumTerm = visitConceptCountMediumTerm;
  }

  /**
   * The number of visits observed in the short term window, stratified by visit concept ID. (analysis ID 925) 
   * @return visitConceptCountShortTerm
   **/
  @Override
  public Boolean getVisitConceptCountShortTerm() {
    return visitConceptCountShortTerm;
  }

    /**
     *
     * @param visitConceptCountShortTerm
     */
    public void setVisitConceptCountShortTerm(Boolean visitConceptCountShortTerm) {
    this.visitConceptCountShortTerm = visitConceptCountShortTerm;
  }

  /**
   * What is the start day (relative to the index date) of the long-term window? 
   * @return longTermStartDays
   **/
  @Override
  public Integer getLongTermStartDays() {
    return longTermStartDays;
  }

    /**
     *
     * @param longTermStartDays
     */
    public void setLongTermStartDays(Integer longTermStartDays) {
    this.longTermStartDays = longTermStartDays;
  }

  /**
   * What is the start day (relative to the index date) of the medium-term window? 
   * @return mediumTermStartDays
   **/
  @Override
  public Integer getMediumTermStartDays() {
    return mediumTermStartDays;
  }

    /**
     *
     * @param mediumTermStartDays
     */
    public void setMediumTermStartDays(Integer mediumTermStartDays) {
    this.mediumTermStartDays = mediumTermStartDays;
  }

  /**
   * What is the start day (relative to the index date) of the short-term window? 
   * @return shortTermStartDays
   **/
  @Override
  public Integer getShortTermStartDays() {
    return shortTermStartDays;
  }

    /**
     *
     * @param shortTermStartDays
     */
    public void setShortTermStartDays(Integer shortTermStartDays) {
    this.shortTermStartDays = shortTermStartDays;
  }

  /**
   * What is the end day (relative to the index date) of the window? 
   * @return endDays
   **/
  @Override
  public Integer getEndDays() {
    return endDays;
  }

    /**
     *
     * @param endDays
     */
    public void setEndDays(Integer endDays) {
    this.endDays = endDays;
  }

    /**
     *
     * @param includedCovariateConceptIdsItem
     * @return
     */
    public CovariateSettingsImpl addIncludedCovariateConceptIdsItem(Long includedCovariateConceptIdsItem) {
    if (this.includedCovariateConceptIds == null) {
      this.includedCovariateConceptIds = new ArrayList<>();
    }
    this.includedCovariateConceptIds.add(includedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that should be d to construct covariates. 
   * @return includedCovariateConceptIds
   **/
  @Override
  public List<Long> getIncludedCovariateConceptIds() {
    return includedCovariateConceptIds;
  }

    /**
     *
     * @param includedCovariateConceptIds
     */
    public void setIncludedCovariateConceptIds(List<Long> includedCovariateConceptIds) {
    this.includedCovariateConceptIds = includedCovariateConceptIds;
  }

    
  /**
   * Should descendant concept IDs be added to the list of concepts to include? 
   * @return addDescendantsToInclude
   **/
  @Override
  public Boolean getAddDescendantsToInclude() {
    return addDescendantsToInclude;
  }

    /**
     *
     * @param addDescendantsToInclude
     */
    public void setAddDescendantsToInclude(Boolean addDescendantsToInclude) {
    this.addDescendantsToInclude = addDescendantsToInclude;
  }

    /**
     *
     * @param excludedCovariateConceptIdsItem
     * @return
     */
    public CovariateSettingsImpl addExcludedCovariateConceptIdsItem(Long excludedCovariateConceptIdsItem) {
    if (this.excludedCovariateConceptIds == null) {
      this.excludedCovariateConceptIds = new ArrayList<>();
    }
    this.excludedCovariateConceptIds.add(excludedCovariateConceptIdsItem);
    return this;
  }

  /**
   * A list of concept IDs that should NOT be d to construct covariates. 
   * @return excludedCovariateConceptIds
   **/
  @Override
  public List<Long> getExcludedCovariateConceptIds() {
    return excludedCovariateConceptIds;
  }

    /**
     *
     * @param excludedCovariateConceptIds
     */
    public void setExcludedCovariateConceptIds(List<Long> excludedCovariateConceptIds) {
    this.excludedCovariateConceptIds = excludedCovariateConceptIds;
  }

  /**
   * Should descendant concept IDs be added to the list of concepts to exclude? 
   * @return addDescendantsToExclude
   **/
  @Override
  public Boolean getAddDescendantsToExclude() {
    return addDescendantsToExclude;
  }

    /**
     *
     * @param addDescendantsToExclude
     */
    public void setAddDescendantsToExclude(Boolean addDescendantsToExclude) {
    this.addDescendantsToExclude = addDescendantsToExclude;
  }

    /**
     *
     * @param includedCovariateIdsItem
     * @return
     */
    public CovariateSettingsImpl addIncludedCovariateIdsItem(Long includedCovariateIdsItem) {
    if (this.includedCovariateIds == null) {
      this.includedCovariateIds = new ArrayList<>();
    }
    this.includedCovariateIds.add(includedCovariateIdsItem);
    return this;
  }

  /**
   * A list of covariate IDs that should be restricted to. 
   * @return includedCovariateIds
   **/
  @Override
  public List<Long> getIncludedCovariateIds() {
    return includedCovariateIds;
  }

    /**
     *
     * @param includedCovariateIds
     */
    public void setIncludedCovariateIds(List<Long> includedCovariateIds) {
    this.includedCovariateIds = includedCovariateIds;
  }

    
  /**
   * Get attrFun
   * @return attrFun
   **/
  @Override
  public String getAttrFun() {
    return attrFun;
  }

    /**
     *
     * @param attrFun
     */
    public void setAttrFun(String attrFun) {
    this.attrFun = attrFun;
  }

  /**
   * Get csAttrClass
   * @return csAttrClass
   **/
  @Override
  public String getAttrClass() {
    return csAttrClass;
  }

    /**
     *
     * @param attrClass
     */
    @Override
  public void setAttrClass(String attrClass) {
    this.csAttrClass = attrClass;
  }
}
