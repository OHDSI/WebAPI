/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortcomparison;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "ComparativeCohortAnalysisDefinition")
@Table(name = "cca_analysis")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope=ComparativeCohortAnalysisDefinition.class)
public class ComparativeCohortAnalysisDefinition implements Serializable {
		@Id
		@Column(name = "id", insertable = false, updatable = false)
		@SequenceGenerator(name = "CCA_ANALYSIS_SEQUENCE_GENERATOR", sequenceName = "CCA_ANALYSIS_SEQUENCE", allocationSize = 1, initialValue = 1)
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CCA_ANALYSIS_SEQUENCE_GENERATOR")
		private Integer id;
		  
		@ManyToOne
		@JoinColumn(name="cca_id", referencedColumnName="cca_id")
		@JsonIgnore
    private ComparativeCohortAnalysis comparativeCohortAnalysis;
		
    @Column(name = "description")
    private String description;
		
    @Column(name = "model_type")
    private String modelType;

    @Column(name = "time_at_risk_start")
    private int timeAtRiskStart;

    @Column(name = "time_at_risk_end")
    private int timeAtRiskEnd;

    @Column(name = "add_exposure_days_to_end")
    private int addExposureDaysToEnd;

    @Column(name = "minimum_washout_period")
    private int minimumWashoutPeriod;

    @Column(name = "minimum_days_at_risk")
    private int minimumDaysAtRisk;

    @Column(name = "rm_subjects_in_both_cohorts")
    private int rmSubjectsInBothCohorts;

    @Column(name = "rm_prior_outcomes")
    private int rmPriorOutcomes;

    @Column(name = "ps_adjustment")
    private int psAdjustment;

    @Column(name = "ps_demographics")
    private int psDemographics;

    @Column(name = "ps_demographics_gender")
    private int psDemographicsGender;

    @Column(name = "ps_demographics_race")
    private int psDemographicsRace;

    @Column(name = "ps_demographics_ethnicity")
    private int psDemographicsEthnicity;

    @Column(name = "ps_demographics_age")
    private int psDemographicsAge;

    @Column(name = "ps_demographics_year")
    private int psDemographicsYear;

    @Column(name = "ps_demographics_month")
    private int psDemographicsMonth;

    @Column(name = "ps_trim")
    private int psTrim;

    @Column(name = "ps_trim_fraction")
    private int psTrimFraction;

    @Column(name = "ps_match")
    private int psMatch;

    @Column(name = "ps_match_max_ratio")
    private int psMatchMaxRatio;

    @Column(name = "ps_strat")
    private int psStrat;

    @Column(name = "ps_strat_num_strata")
    private int psStratNumStrata;

    @Column(name = "ps_condition_occ")
    private int psConditionOcc;

    @Column(name = "ps_condition_occ_365d")
    private int psConditionOcc365d;

    @Column(name = "ps_condition_occ_30d")
    private int psConditionOcc30d;

    @Column(name = "ps_condition_occ_inpt180d")
    private int psConditionOccInpt180d;

    @Column(name = "ps_condition_era")
    private int psConditionEra;

    @Column(name = "ps_condition_era_ever")
    private int psConditionEraEver;

    @Column(name = "ps_condition_era_overlap")
    private int psConditionEraOverlap;

    @Column(name = "ps_condition_group")
    private int psConditionGroup;

    @Column(name = "ps_condition_group_meddra")
    private int psConditionGroupMeddra;

    @Column(name = "ps_condition_group_snomed")
    private int psConditionGroupSnomed;

    @Column(name = "ps_drug_exposure")
    private int psDrugExposure;

    @Column(name = "ps_drug_exposure_365d")
    private int psDrugExposure365d;

    @Column(name = "ps_drug_exposure_30d")
    private int psDrugExposure30d;

    @Column(name = "ps_drug_era")
    private int psDrugEra;

    @Column(name = "ps_drug_era_365d")
    private int psDrugEra365d;

    @Column(name = "ps_drug_era_30d")
    private int psDrugEra30d;

    @Column(name = "ps_drug_era_overlap")
    private int psDrugEraOverlap;

    @Column(name = "ps_drug_era_ever")
    private int psDrugEraEver;

    @Column(name = "ps_drug_group")
    private int psDrugGroup;

    @Column(name = "ps_procedure_occ")
    private int psProcedureOcc;

    @Column(name = "ps_procedure_occ_365d")
    private int psProcedureOcc365d;

    @Column(name = "ps_procedure_occ_30d")
    private int psProcedureOcc30d;

    @Column(name = "ps_procedure_group")
    private int psProcedureGroup;

    @Column(name = "ps_observation")
    private int psObservation;

    @Column(name = "ps_observation_365d")
    private int psObservation365d;

    @Column(name = "ps_observation_30d")
    private int psObservation30d;

    @Column(name = "ps_observation_count_365d")
    private int psObservationCount365d;

    @Column(name = "ps_measurement")
    private int psMeasurement;

    @Column(name = "ps_measurement_365d")
    private int psMeasurement365d;

    @Column(name = "ps_measurement_30d")
    private int psMeasurement30d;

    @Column(name = "ps_measurement_count_365d")
    private int psMeasurementCount365d;

    @Column(name = "ps_measurement_below")
    private int psMeasurementBelow;

    @Column(name = "ps_measurement_above")
    private int psMeasurementAbove;

    @Column(name = "ps_concept_counts")
    private int psConceptCounts;

    @Column(name = "ps_risk_scores")
    private int psRiskScores;

    @Column(name = "ps_risk_scores_charlson")
    private int psRiskScoresCharlson;

    @Column(name = "ps_risk_scores_dcsi")
    private int psRiskScoresDcsi;

    @Column(name = "ps_risk_scores_chads2")
    private int psRiskScoresChads2;

    @Column(name = "ps_risk_scores_chads2vasc")
    private int psRiskScoresChads2vasc;

    @Column(name = "ps_interaction_year")
    private int psInteractionYear;

    @Column(name = "ps_interaction_month")
    private int psInteractionMonth;

    @Column(name = "om_covariates")
    private int omCovariates;

    @Column(name = "om_exclusion_id")
    private int omExclusionId;

    @Column(name = "om_inclusion_id")
    private int omInclusionId;

    @Column(name = "om_demographics")
    private int omDemographics;

    @Column(name = "om_demographics_gender")
    private int omDemographicsGender;

    @Column(name = "om_demographics_race")
    private int omDemographicsRace;

    @Column(name = "om_demographics_ethnicity")
    private int omDemographicsEthnicity;

    @Column(name = "om_demographics_age")
    private int omDemographicsAge;

    @Column(name = "om_demographics_year")
    private int omDemographicsYear;

    @Column(name = "om_demographics_month")
    private int omDemographicsMonth;

    @Column(name = "om_trim")
    private int omTrim;

    @Column(name = "om_trim_fraction")
    private int omTrimFraction;

    @Column(name = "om_match")
    private int omMatch;

    @Column(name = "om_match_max_ratio")
    private int omMatchMaxRatio;

    @Column(name = "om_strat")
    private int omStrat;

    @Column(name = "om_strat_num_strata")
    private int omStratNumStrata;

    @Column(name = "om_condition_occ")
    private int omConditionOcc;

    @Column(name = "om_condition_occ_365d")
    private int omConditionOcc365d;

    @Column(name = "om_condition_occ_30d")
    private int omConditionOcc30d;

    @Column(name = "om_condition_occ_inpt180d")
    private int omConditionOccInpt180d;

    @Column(name = "om_condition_era")
    private int omConditionEra;

    @Column(name = "om_condition_era_ever")
    private int omConditionEraEver;

    @Column(name = "om_condition_era_overlap")
    private int omConditionEraOverlap;

    @Column(name = "om_condition_group")
    private int omConditionGroup;

    @Column(name = "om_condition_group_meddra")
    private int omConditionGroupMeddra;

    @Column(name = "om_condition_group_snomed")
    private int omConditionGroupSnomed;

    @Column(name = "om_drug_exposure")
    private int omDrugExposure;

    @Column(name = "om_drug_exposure_365d")
    private int omDrugExposure365d;

    @Column(name = "om_drug_exposure_30d")
    private int omDrugExposure30d;

    @Column(name = "om_drug_era")
    private int omDrugEra;

    @Column(name = "om_drug_era_365d")
    private int omDrugEra365d;

    @Column(name = "om_drug_era_30d")
    private int omDrugEra30d;

    @Column(name = "om_drug_era_overlap")
    private int omDrugEraOverlap;

    @Column(name = "om_drug_era_ever")
    private int omDrugEraEver;

    @Column(name = "om_drug_group")
    private int omDrugGroup;

    @Column(name = "om_procedure_occ")
    private int omProcedureOcc;

    @Column(name = "om_procedure_occ_365d")
    private int omProcedureOcc365d;

    @Column(name = "om_procedure_occ_30d")
    private int omProcedureOcc30d;

    @Column(name = "om_procedure_group")
    private int omProcedureGroup;

    @Column(name = "om_observation")
    private int omObservation;

    @Column(name = "om_observation_365d")
    private int omObservation365d;

    @Column(name = "om_observation_30d")
    private int omObservation30d;

    @Column(name = "om_observation_count_365d")
    private int omObservationCount365d;

    @Column(name = "om_measurement")
    private int omMeasurement;

    @Column(name = "om_measurement_365d")
    private int omMeasurement365d;

    @Column(name = "om_measurement_30d")
    private int omMeasurement30d;

    @Column(name = "om_measurement_count_365d")
    private int omMeasurementCount365d;

    @Column(name = "om_measurement_below")
    private int omMeasurementBelow;

    @Column(name = "om_measurement_above")
    private int omMeasurementAbove;

    @Column(name = "om_concept_counts")
    private int omConceptCounts;

    @Column(name = "om_risk_scores")
    private int omRiskScores;

    @Column(name = "om_risk_scores_charlson")
    private int omRiskScoresCharlson;

    @Column(name = "om_risk_scores_dcsi")
    private int omRiskScoresDcsi;

    @Column(name = "om_risk_scores_chads2")
    private int omRiskScoresChads2;

    @Column(name = "om_risk_scores_chads2vasc")
    private int omRiskScoresChads2vasc;

    @Column(name = "om_interaction_year")
    private int omInteractionYear;

    @Column(name = "om_interaction_month")
    private int omInteractionMonth;

    @Column(name = "del_covariates_small_count")
    private int delCovariatesSmallCount;

    @Column(name = "negative_control_id")
    private int negativeControlId;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the modelType
	 */
	public String getModelType() {
		return modelType;
	}

	/**
	 * @param modelType the modelType to set
	 */
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	/**
	 * @return the timeAtRiskStart
	 */
	public int getTimeAtRiskStart() {
		return timeAtRiskStart;
	}

	/**
	 * @param timeAtRiskStart the timeAtRiskStart to set
	 */
	public void setTimeAtRiskStart(int timeAtRiskStart) {
		this.timeAtRiskStart = timeAtRiskStart;
	}

	/**
	 * @return the timeAtRiskEnd
	 */
	public int getTimeAtRiskEnd() {
		return timeAtRiskEnd;
	}

	/**
	 * @param timeAtRiskEnd the timeAtRiskEnd to set
	 */
	public void setTimeAtRiskEnd(int timeAtRiskEnd) {
		this.timeAtRiskEnd = timeAtRiskEnd;
	}

	/**
	 * @return the addExposureDaysToEnd
	 */
	public int getAddExposureDaysToEnd() {
		return addExposureDaysToEnd;
	}

	/**
	 * @param addExposureDaysToEnd the addExposureDaysToEnd to set
	 */
	public void setAddExposureDaysToEnd(int addExposureDaysToEnd) {
		this.addExposureDaysToEnd = addExposureDaysToEnd;
	}

	/**
	 * @return the minimumWashoutPeriod
	 */
	public int getMinimumWashoutPeriod() {
		return minimumWashoutPeriod;
	}

	/**
	 * @param minimumWashoutPeriod the minimumWashoutPeriod to set
	 */
	public void setMinimumWashoutPeriod(int minimumWashoutPeriod) {
		this.minimumWashoutPeriod = minimumWashoutPeriod;
	}

	/**
	 * @return the minimumDaysAtRisk
	 */
	public int getMinimumDaysAtRisk() {
		return minimumDaysAtRisk;
	}

	/**
	 * @param minimumDaysAtRisk the minimumDaysAtRisk to set
	 */
	public void setMinimumDaysAtRisk(int minimumDaysAtRisk) {
		this.minimumDaysAtRisk = minimumDaysAtRisk;
	}

	/**
	 * @return the rmSubjectsInBothCohorts
	 */
	public int getRmSubjectsInBothCohorts() {
		return rmSubjectsInBothCohorts;
	}

	/**
	 * @param rmSubjectsInBothCohorts the rmSubjectsInBothCohorts to set
	 */
	public void setRmSubjectsInBothCohorts(int rmSubjectsInBothCohorts) {
		this.rmSubjectsInBothCohorts = rmSubjectsInBothCohorts;
	}

	/**
	 * @return the rmPriorOutcomes
	 */
	public int getRmPriorOutcomes() {
		return rmPriorOutcomes;
	}

	/**
	 * @param rmPriorOutcomes the rmPriorOutcomes to set
	 */
	public void setRmPriorOutcomes(int rmPriorOutcomes) {
		this.rmPriorOutcomes = rmPriorOutcomes;
	}

	/**
	 * @return the psAdjustment
	 */
	public int getPsAdjustment() {
		return psAdjustment;
	}

	/**
	 * @param psAdjustment the psAdjustment to set
	 */
	public void setPsAdjustment(int psAdjustment) {
		this.psAdjustment = psAdjustment;
	}

	/**
	 * @return the psDemographics
	 */
	public int getPsDemographics() {
		return psDemographics;
	}

	/**
	 * @param psDemographics the psDemographics to set
	 */
	public void setPsDemographics(int psDemographics) {
		this.psDemographics = psDemographics;
	}

	/**
	 * @return the psDemographicsGender
	 */
	public int getPsDemographicsGender() {
		return psDemographicsGender;
	}

	/**
	 * @param psDemographicsGender the psDemographicsGender to set
	 */
	public void setPsDemographicsGender(int psDemographicsGender) {
		this.psDemographicsGender = psDemographicsGender;
	}

	/**
	 * @return the psDemographicsRace
	 */
	public int getPsDemographicsRace() {
		return psDemographicsRace;
	}

	/**
	 * @param psDemographicsRace the psDemographicsRace to set
	 */
	public void setPsDemographicsRace(int psDemographicsRace) {
		this.psDemographicsRace = psDemographicsRace;
	}

	/**
	 * @return the psDemographicsEthnicity
	 */
	public int getPsDemographicsEthnicity() {
		return psDemographicsEthnicity;
	}

	/**
	 * @param psDemographicsEthnicity the psDemographicsEthnicity to set
	 */
	public void setPsDemographicsEthnicity(int psDemographicsEthnicity) {
		this.psDemographicsEthnicity = psDemographicsEthnicity;
	}

	/**
	 * @return the psDemographicsAge
	 */
	public int getPsDemographicsAge() {
		return psDemographicsAge;
	}

	/**
	 * @param psDemographicsAge the psDemographicsAge to set
	 */
	public void setPsDemographicsAge(int psDemographicsAge) {
		this.psDemographicsAge = psDemographicsAge;
	}

	/**
	 * @return the psDemographicsYear
	 */
	public int getPsDemographicsYear() {
		return psDemographicsYear;
	}

	/**
	 * @param psDemographicsYear the psDemographicsYear to set
	 */
	public void setPsDemographicsYear(int psDemographicsYear) {
		this.psDemographicsYear = psDemographicsYear;
	}

	/**
	 * @return the psDemographicsMonth
	 */
	public int getPsDemographicsMonth() {
		return psDemographicsMonth;
	}

	/**
	 * @param psDemographicsMonth the psDemographicsMonth to set
	 */
	public void setPsDemographicsMonth(int psDemographicsMonth) {
		this.psDemographicsMonth = psDemographicsMonth;
	}

	/**
	 * @return the psTrim
	 */
	public int getPsTrim() {
		return psTrim;
	}

	/**
	 * @param psTrim the psTrim to set
	 */
	public void setPsTrim(int psTrim) {
		this.psTrim = psTrim;
	}

	/**
	 * @return the psTrimFraction
	 */
	public int getPsTrimFraction() {
		return psTrimFraction;
	}

	/**
	 * @param psTrimFraction the psTrimFraction to set
	 */
	public void setPsTrimFraction(int psTrimFraction) {
		this.psTrimFraction = psTrimFraction;
	}

	/**
	 * @return the psMatch
	 */
	public int getPsMatch() {
		return psMatch;
	}

	/**
	 * @param psMatch the psMatch to set
	 */
	public void setPsMatch(int psMatch) {
		this.psMatch = psMatch;
	}

	/**
	 * @return the psMatchMaxRatio
	 */
	public int getPsMatchMaxRatio() {
		return psMatchMaxRatio;
	}

	/**
	 * @param psMatchMaxRatio the psMatchMaxRatio to set
	 */
	public void setPsMatchMaxRatio(int psMatchMaxRatio) {
		this.psMatchMaxRatio = psMatchMaxRatio;
	}

	/**
	 * @return the psStrat
	 */
	public int getPsStrat() {
		return psStrat;
	}

	/**
	 * @param psStrat the psStrat to set
	 */
	public void setPsStrat(int psStrat) {
		this.psStrat = psStrat;
	}

	/**
	 * @return the psStratNumStrata
	 */
	public int getPsStratNumStrata() {
		return psStratNumStrata;
	}

	/**
	 * @param psStratNumStrata the psStratNumStrata to set
	 */
	public void setPsStratNumStrata(int psStratNumStrata) {
		this.psStratNumStrata = psStratNumStrata;
	}

	/**
	 * @return the psConditionOcc
	 */
	public int getPsConditionOcc() {
		return psConditionOcc;
	}

	/**
	 * @param psConditionOcc the psConditionOcc to set
	 */
	public void setPsConditionOcc(int psConditionOcc) {
		this.psConditionOcc = psConditionOcc;
	}

	/**
	 * @return the psConditionOcc365d
	 */
	public int getPsConditionOcc365d() {
		return psConditionOcc365d;
	}

	/**
	 * @param psConditionOcc365d the psConditionOcc365d to set
	 */
	public void setPsConditionOcc365d(int psConditionOcc365d) {
		this.psConditionOcc365d = psConditionOcc365d;
	}

	/**
	 * @return the psConditionOcc30d
	 */
	public int getPsConditionOcc30d() {
		return psConditionOcc30d;
	}

	/**
	 * @param psConditionOcc30d the psConditionOcc30d to set
	 */
	public void setPsConditionOcc30d(int psConditionOcc30d) {
		this.psConditionOcc30d = psConditionOcc30d;
	}

	/**
	 * @return the psConditionOccInpt180d
	 */
	public int getPsConditionOccInpt180d() {
		return psConditionOccInpt180d;
	}

	/**
	 * @param psConditionOccInpt180d the psConditionOccInpt180d to set
	 */
	public void setPsConditionOccInpt180d(int psConditionOccInpt180d) {
		this.psConditionOccInpt180d = psConditionOccInpt180d;
	}

	/**
	 * @return the psConditionEra
	 */
	public int getPsConditionEra() {
		return psConditionEra;
	}

	/**
	 * @param psConditionEra the psConditionEra to set
	 */
	public void setPsConditionEra(int psConditionEra) {
		this.psConditionEra = psConditionEra;
	}

	/**
	 * @return the psConditionEraEver
	 */
	public int getPsConditionEraEver() {
		return psConditionEraEver;
	}

	/**
	 * @param psConditionEraEver the psConditionEraEver to set
	 */
	public void setPsConditionEraEver(int psConditionEraEver) {
		this.psConditionEraEver = psConditionEraEver;
	}

	/**
	 * @return the psConditionEraOverlap
	 */
	public int getPsConditionEraOverlap() {
		return psConditionEraOverlap;
	}

	/**
	 * @param psConditionEraOverlap the psConditionEraOverlap to set
	 */
	public void setPsConditionEraOverlap(int psConditionEraOverlap) {
		this.psConditionEraOverlap = psConditionEraOverlap;
	}

	/**
	 * @return the psConditionGroup
	 */
	public int getPsConditionGroup() {
		return psConditionGroup;
	}

	/**
	 * @param psConditionGroup the psConditionGroup to set
	 */
	public void setPsConditionGroup(int psConditionGroup) {
		this.psConditionGroup = psConditionGroup;
	}

	/**
	 * @return the psConditionGroupMeddra
	 */
	public int getPsConditionGroupMeddra() {
		return psConditionGroupMeddra;
	}

	/**
	 * @param psConditionGroupMeddra the psConditionGroupMeddra to set
	 */
	public void setPsConditionGroupMeddra(int psConditionGroupMeddra) {
		this.psConditionGroupMeddra = psConditionGroupMeddra;
	}

	/**
	 * @return the psConditionGroupSnomed
	 */
	public int getPsConditionGroupSnomed() {
		return psConditionGroupSnomed;
	}

	/**
	 * @param psConditionGroupSnomed the psConditionGroupSnomed to set
	 */
	public void setPsConditionGroupSnomed(int psConditionGroupSnomed) {
		this.psConditionGroupSnomed = psConditionGroupSnomed;
	}

	/**
	 * @return the psDrugExposure
	 */
	public int getPsDrugExposure() {
		return psDrugExposure;
	}

	/**
	 * @param psDrugExposure the psDrugExposure to set
	 */
	public void setPsDrugExposure(int psDrugExposure) {
		this.psDrugExposure = psDrugExposure;
	}

	/**
	 * @return the psDrugExposure365d
	 */
	public int getPsDrugExposure365d() {
		return psDrugExposure365d;
	}

	/**
	 * @param psDrugExposure365d the psDrugExposure365d to set
	 */
	public void setPsDrugExposure365d(int psDrugExposure365d) {
		this.psDrugExposure365d = psDrugExposure365d;
	}

	/**
	 * @return the psDrugExposure30d
	 */
	public int getPsDrugExposure30d() {
		return psDrugExposure30d;
	}

	/**
	 * @param psDrugExposure30d the psDrugExposure30d to set
	 */
	public void setPsDrugExposure30d(int psDrugExposure30d) {
		this.psDrugExposure30d = psDrugExposure30d;
	}

	/**
	 * @return the psDrugEra
	 */
	public int getPsDrugEra() {
		return psDrugEra;
	}

	/**
	 * @param psDrugEra the psDrugEra to set
	 */
	public void setPsDrugEra(int psDrugEra) {
		this.psDrugEra = psDrugEra;
	}

	/**
	 * @return the psDrugEra365d
	 */
	public int getPsDrugEra365d() {
		return psDrugEra365d;
	}

	/**
	 * @param psDrugEra365d the psDrugEra365d to set
	 */
	public void setPsDrugEra365d(int psDrugEra365d) {
		this.psDrugEra365d = psDrugEra365d;
	}

	/**
	 * @return the psDrugEra30d
	 */
	public int getPsDrugEra30d() {
		return psDrugEra30d;
	}

	/**
	 * @param psDrugEra30d the psDrugEra30d to set
	 */
	public void setPsDrugEra30d(int psDrugEra30d) {
		this.psDrugEra30d = psDrugEra30d;
	}

	/**
	 * @return the psDrugEraOverlap
	 */
	public int getPsDrugEraOverlap() {
		return psDrugEraOverlap;
	}

	/**
	 * @param psDrugEraOverlap the psDrugEraOverlap to set
	 */
	public void setPsDrugEraOverlap(int psDrugEraOverlap) {
		this.psDrugEraOverlap = psDrugEraOverlap;
	}

	/**
	 * @return the psDrugEraEver
	 */
	public int getPsDrugEraEver() {
		return psDrugEraEver;
	}

	/**
	 * @param psDrugEraEver the psDrugEraEver to set
	 */
	public void setPsDrugEraEver(int psDrugEraEver) {
		this.psDrugEraEver = psDrugEraEver;
	}

	/**
	 * @return the psDrugGroup
	 */
	public int getPsDrugGroup() {
		return psDrugGroup;
	}

	/**
	 * @param psDrugGroup the psDrugGroup to set
	 */
	public void setPsDrugGroup(int psDrugGroup) {
		this.psDrugGroup = psDrugGroup;
	}

	/**
	 * @return the psProcedureOcc
	 */
	public int getPsProcedureOcc() {
		return psProcedureOcc;
	}

	/**
	 * @param psProcedureOcc the psProcedureOcc to set
	 */
	public void setPsProcedureOcc(int psProcedureOcc) {
		this.psProcedureOcc = psProcedureOcc;
	}

	/**
	 * @return the psProcedureOcc365d
	 */
	public int getPsProcedureOcc365d() {
		return psProcedureOcc365d;
	}

	/**
	 * @param psProcedureOcc365d the psProcedureOcc365d to set
	 */
	public void setPsProcedureOcc365d(int psProcedureOcc365d) {
		this.psProcedureOcc365d = psProcedureOcc365d;
	}

	/**
	 * @return the psProcedureOcc30d
	 */
	public int getPsProcedureOcc30d() {
		return psProcedureOcc30d;
	}

	/**
	 * @param psProcedureOcc30d the psProcedureOcc30d to set
	 */
	public void setPsProcedureOcc30d(int psProcedureOcc30d) {
		this.psProcedureOcc30d = psProcedureOcc30d;
	}

	/**
	 * @return the psProcedureGroup
	 */
	public int getPsProcedureGroup() {
		return psProcedureGroup;
	}

	/**
	 * @param psProcedureGroup the psProcedureGroup to set
	 */
	public void setPsProcedureGroup(int psProcedureGroup) {
		this.psProcedureGroup = psProcedureGroup;
	}

	/**
	 * @return the psObservation
	 */
	public int getPsObservation() {
		return psObservation;
	}

	/**
	 * @param psObservation the psObservation to set
	 */
	public void setPsObservation(int psObservation) {
		this.psObservation = psObservation;
	}

	/**
	 * @return the psObservation365d
	 */
	public int getPsObservation365d() {
		return psObservation365d;
	}

	/**
	 * @param psObservation365d the psObservation365d to set
	 */
	public void setPsObservation365d(int psObservation365d) {
		this.psObservation365d = psObservation365d;
	}

	/**
	 * @return the psObservation30d
	 */
	public int getPsObservation30d() {
		return psObservation30d;
	}

	/**
	 * @param psObservation30d the psObservation30d to set
	 */
	public void setPsObservation30d(int psObservation30d) {
		this.psObservation30d = psObservation30d;
	}

	/**
	 * @return the psObservationCount365d
	 */
	public int getPsObservationCount365d() {
		return psObservationCount365d;
	}

	/**
	 * @param psObservationCount365d the psObservationCount365d to set
	 */
	public void setPsObservationCount365d(int psObservationCount365d) {
		this.psObservationCount365d = psObservationCount365d;
	}

	/**
	 * @return the psMeasurement
	 */
	public int getPsMeasurement() {
		return psMeasurement;
	}

	/**
	 * @param psMeasurement the psMeasurement to set
	 */
	public void setPsMeasurement(int psMeasurement) {
		this.psMeasurement = psMeasurement;
	}

	/**
	 * @return the psMeasurement365d
	 */
	public int getPsMeasurement365d() {
		return psMeasurement365d;
	}

	/**
	 * @param psMeasurement365d the psMeasurement365d to set
	 */
	public void setPsMeasurement365d(int psMeasurement365d) {
		this.psMeasurement365d = psMeasurement365d;
	}

	/**
	 * @return the psMeasurement30d
	 */
	public int getPsMeasurement30d() {
		return psMeasurement30d;
	}

	/**
	 * @param psMeasurement30d the psMeasurement30d to set
	 */
	public void setPsMeasurement30d(int psMeasurement30d) {
		this.psMeasurement30d = psMeasurement30d;
	}

	/**
	 * @return the psMeasurementCount365d
	 */
	public int getPsMeasurementCount365d() {
		return psMeasurementCount365d;
	}

	/**
	 * @param psMeasurementCount365d the psMeasurementCount365d to set
	 */
	public void setPsMeasurementCount365d(int psMeasurementCount365d) {
		this.psMeasurementCount365d = psMeasurementCount365d;
	}

	/**
	 * @return the psMeasurementBelow
	 */
	public int getPsMeasurementBelow() {
		return psMeasurementBelow;
	}

	/**
	 * @param psMeasurementBelow the psMeasurementBelow to set
	 */
	public void setPsMeasurementBelow(int psMeasurementBelow) {
		this.psMeasurementBelow = psMeasurementBelow;
	}

	/**
	 * @return the psMeasurementAbove
	 */
	public int getPsMeasurementAbove() {
		return psMeasurementAbove;
	}

	/**
	 * @param psMeasurementAbove the psMeasurementAbove to set
	 */
	public void setPsMeasurementAbove(int psMeasurementAbove) {
		this.psMeasurementAbove = psMeasurementAbove;
	}

	/**
	 * @return the psConceptCounts
	 */
	public int getPsConceptCounts() {
		return psConceptCounts;
	}

	/**
	 * @param psConceptCounts the psConceptCounts to set
	 */
	public void setPsConceptCounts(int psConceptCounts) {
		this.psConceptCounts = psConceptCounts;
	}

	/**
	 * @return the psRiskScores
	 */
	public int getPsRiskScores() {
		return psRiskScores;
	}

	/**
	 * @param psRiskScores the psRiskScores to set
	 */
	public void setPsRiskScores(int psRiskScores) {
		this.psRiskScores = psRiskScores;
	}

	/**
	 * @return the psRiskScoresCharlson
	 */
	public int getPsRiskScoresCharlson() {
		return psRiskScoresCharlson;
	}

	/**
	 * @param psRiskScoresCharlson the psRiskScoresCharlson to set
	 */
	public void setPsRiskScoresCharlson(int psRiskScoresCharlson) {
		this.psRiskScoresCharlson = psRiskScoresCharlson;
	}

	/**
	 * @return the psRiskScoresDcsi
	 */
	public int getPsRiskScoresDcsi() {
		return psRiskScoresDcsi;
	}

	/**
	 * @param psRiskScoresDcsi the psRiskScoresDcsi to set
	 */
	public void setPsRiskScoresDcsi(int psRiskScoresDcsi) {
		this.psRiskScoresDcsi = psRiskScoresDcsi;
	}

	/**
	 * @return the psRiskScoresChads2
	 */
	public int getPsRiskScoresChads2() {
		return psRiskScoresChads2;
	}

	/**
	 * @param psRiskScoresChads2 the psRiskScoresChads2 to set
	 */
	public void setPsRiskScoresChads2(int psRiskScoresChads2) {
		this.psRiskScoresChads2 = psRiskScoresChads2;
	}

	/**
	 * @return the psRiskScoresChads2vasc
	 */
	public int getPsRiskScoresChads2vasc() {
		return psRiskScoresChads2vasc;
	}

	/**
	 * @param psRiskScoresChads2vasc the psRiskScoresChads2vasc to set
	 */
	public void setPsRiskScoresChads2vasc(int psRiskScoresChads2vasc) {
		this.psRiskScoresChads2vasc = psRiskScoresChads2vasc;
	}

	/**
	 * @return the psInteractionYear
	 */
	public int getPsInteractionYear() {
		return psInteractionYear;
	}

	/**
	 * @param psInteractionYear the psInteractionYear to set
	 */
	public void setPsInteractionYear(int psInteractionYear) {
		this.psInteractionYear = psInteractionYear;
	}

	/**
	 * @return the psInteractionMonth
	 */
	public int getPsInteractionMonth() {
		return psInteractionMonth;
	}

	/**
	 * @param psInteractionMonth the psInteractionMonth to set
	 */
	public void setPsInteractionMonth(int psInteractionMonth) {
		this.psInteractionMonth = psInteractionMonth;
	}

	/**
	 * @return the omCovariates
	 */
	public int getOmCovariates() {
		return omCovariates;
	}

	/**
	 * @param omCovariates the omCovariates to set
	 */
	public void setOmCovariates(int omCovariates) {
		this.omCovariates = omCovariates;
	}

	/**
	 * @return the omExclusionId
	 */
	public int getOmExclusionId() {
		return omExclusionId;
	}

	/**
	 * @param omExclusionId the omExclusionId to set
	 */
	public void setOmExclusionId(int omExclusionId) {
		this.omExclusionId = omExclusionId;
	}

	/**
	 * @return the omInclusionId
	 */
	public int getOmInclusionId() {
		return omInclusionId;
	}

	/**
	 * @param omInclusionId the omInclusionId to set
	 */
	public void setOmInclusionId(int omInclusionId) {
		this.omInclusionId = omInclusionId;
	}

	/**
	 * @return the omDemographics
	 */
	public int getOmDemographics() {
		return omDemographics;
	}

	/**
	 * @param omDemographics the omDemographics to set
	 */
	public void setOmDemographics(int omDemographics) {
		this.omDemographics = omDemographics;
	}

	/**
	 * @return the omDemographicsGender
	 */
	public int getOmDemographicsGender() {
		return omDemographicsGender;
	}

	/**
	 * @param omDemographicsGender the omDemographicsGender to set
	 */
	public void setOmDemographicsGender(int omDemographicsGender) {
		this.omDemographicsGender = omDemographicsGender;
	}

	/**
	 * @return the omDemographicsRace
	 */
	public int getOmDemographicsRace() {
		return omDemographicsRace;
	}

	/**
	 * @param omDemographicsRace the omDemographicsRace to set
	 */
	public void setOmDemographicsRace(int omDemographicsRace) {
		this.omDemographicsRace = omDemographicsRace;
	}

	/**
	 * @return the omDemographicsEthnicity
	 */
	public int getOmDemographicsEthnicity() {
		return omDemographicsEthnicity;
	}

	/**
	 * @param omDemographicsEthnicity the omDemographicsEthnicity to set
	 */
	public void setOmDemographicsEthnicity(int omDemographicsEthnicity) {
		this.omDemographicsEthnicity = omDemographicsEthnicity;
	}

	/**
	 * @return the omDemographicsAge
	 */
	public int getOmDemographicsAge() {
		return omDemographicsAge;
	}

	/**
	 * @param omDemographicsAge the omDemographicsAge to set
	 */
	public void setOmDemographicsAge(int omDemographicsAge) {
		this.omDemographicsAge = omDemographicsAge;
	}

	/**
	 * @return the omDemographicsYear
	 */
	public int getOmDemographicsYear() {
		return omDemographicsYear;
	}

	/**
	 * @param omDemographicsYear the omDemographicsYear to set
	 */
	public void setOmDemographicsYear(int omDemographicsYear) {
		this.omDemographicsYear = omDemographicsYear;
	}

	/**
	 * @return the omDemographicsMonth
	 */
	public int getOmDemographicsMonth() {
		return omDemographicsMonth;
	}

	/**
	 * @param omDemographicsMonth the omDemographicsMonth to set
	 */
	public void setOmDemographicsMonth(int omDemographicsMonth) {
		this.omDemographicsMonth = omDemographicsMonth;
	}

	/**
	 * @return the omTrim
	 */
	public int getOmTrim() {
		return omTrim;
	}

	/**
	 * @param omTrim the omTrim to set
	 */
	public void setOmTrim(int omTrim) {
		this.omTrim = omTrim;
	}

	/**
	 * @return the omTrimFraction
	 */
	public int getOmTrimFraction() {
		return omTrimFraction;
	}

	/**
	 * @param omTrimFraction the omTrimFraction to set
	 */
	public void setOmTrimFraction(int omTrimFraction) {
		this.omTrimFraction = omTrimFraction;
	}

	/**
	 * @return the omMatch
	 */
	public int getOmMatch() {
		return omMatch;
	}

	/**
	 * @param omMatch the omMatch to set
	 */
	public void setOmMatch(int omMatch) {
		this.omMatch = omMatch;
	}

	/**
	 * @return the omMatchMaxRatio
	 */
	public int getOmMatchMaxRatio() {
		return omMatchMaxRatio;
	}

	/**
	 * @param omMatchMaxRatio the omMatchMaxRatio to set
	 */
	public void setOmMatchMaxRatio(int omMatchMaxRatio) {
		this.omMatchMaxRatio = omMatchMaxRatio;
	}

	/**
	 * @return the omStrat
	 */
	public int getOmStrat() {
		return omStrat;
	}

	/**
	 * @param omStrat the omStrat to set
	 */
	public void setOmStrat(int omStrat) {
		this.omStrat = omStrat;
	}

	/**
	 * @return the omStratNumStrata
	 */
	public int getOmStratNumStrata() {
		return omStratNumStrata;
	}

	/**
	 * @param omStratNumStrata the omStratNumStrata to set
	 */
	public void setOmStratNumStrata(int omStratNumStrata) {
		this.omStratNumStrata = omStratNumStrata;
	}

	/**
	 * @return the omConditionOcc
	 */
	public int getOmConditionOcc() {
		return omConditionOcc;
	}

	/**
	 * @param omConditionOcc the omConditionOcc to set
	 */
	public void setOmConditionOcc(int omConditionOcc) {
		this.omConditionOcc = omConditionOcc;
	}

	/**
	 * @return the omConditionOcc365d
	 */
	public int getOmConditionOcc365d() {
		return omConditionOcc365d;
	}

	/**
	 * @param omConditionOcc365d the omConditionOcc365d to set
	 */
	public void setOmConditionOcc365d(int omConditionOcc365d) {
		this.omConditionOcc365d = omConditionOcc365d;
	}

	/**
	 * @return the omConditionOcc30d
	 */
	public int getOmConditionOcc30d() {
		return omConditionOcc30d;
	}

	/**
	 * @param omConditionOcc30d the omConditionOcc30d to set
	 */
	public void setOmConditionOcc30d(int omConditionOcc30d) {
		this.omConditionOcc30d = omConditionOcc30d;
	}

	/**
	 * @return the omConditionOccInpt180d
	 */
	public int getOmConditionOccInpt180d() {
		return omConditionOccInpt180d;
	}

	/**
	 * @param omConditionOccInpt180d the omConditionOccInpt180d to set
	 */
	public void setOmConditionOccInpt180d(int omConditionOccInpt180d) {
		this.omConditionOccInpt180d = omConditionOccInpt180d;
	}

	/**
	 * @return the omConditionEra
	 */
	public int getOmConditionEra() {
		return omConditionEra;
	}

	/**
	 * @param omConditionEra the omConditionEra to set
	 */
	public void setOmConditionEra(int omConditionEra) {
		this.omConditionEra = omConditionEra;
	}

	/**
	 * @return the omConditionEraEver
	 */
	public int getOmConditionEraEver() {
		return omConditionEraEver;
	}

	/**
	 * @param omConditionEraEver the omConditionEraEver to set
	 */
	public void setOmConditionEraEver(int omConditionEraEver) {
		this.omConditionEraEver = omConditionEraEver;
	}

	/**
	 * @return the omConditionEraOverlap
	 */
	public int getOmConditionEraOverlap() {
		return omConditionEraOverlap;
	}

	/**
	 * @param omConditionEraOverlap the omConditionEraOverlap to set
	 */
	public void setOmConditionEraOverlap(int omConditionEraOverlap) {
		this.omConditionEraOverlap = omConditionEraOverlap;
	}

	/**
	 * @return the omConditionGroup
	 */
	public int getOmConditionGroup() {
		return omConditionGroup;
	}

	/**
	 * @param omConditionGroup the omConditionGroup to set
	 */
	public void setOmConditionGroup(int omConditionGroup) {
		this.omConditionGroup = omConditionGroup;
	}

	/**
	 * @return the omConditionGroupMeddra
	 */
	public int getOmConditionGroupMeddra() {
		return omConditionGroupMeddra;
	}

	/**
	 * @param omConditionGroupMeddra the omConditionGroupMeddra to set
	 */
	public void setOmConditionGroupMeddra(int omConditionGroupMeddra) {
		this.omConditionGroupMeddra = omConditionGroupMeddra;
	}

	/**
	 * @return the omConditionGroupSnomed
	 */
	public int getOmConditionGroupSnomed() {
		return omConditionGroupSnomed;
	}

	/**
	 * @param omConditionGroupSnomed the omConditionGroupSnomed to set
	 */
	public void setOmConditionGroupSnomed(int omConditionGroupSnomed) {
		this.omConditionGroupSnomed = omConditionGroupSnomed;
	}

	/**
	 * @return the omDrugExposure
	 */
	public int getOmDrugExposure() {
		return omDrugExposure;
	}

	/**
	 * @param omDrugExposure the omDrugExposure to set
	 */
	public void setOmDrugExposure(int omDrugExposure) {
		this.omDrugExposure = omDrugExposure;
	}

	/**
	 * @return the omDrugExposure365d
	 */
	public int getOmDrugExposure365d() {
		return omDrugExposure365d;
	}

	/**
	 * @param omDrugExposure365d the omDrugExposure365d to set
	 */
	public void setOmDrugExposure365d(int omDrugExposure365d) {
		this.omDrugExposure365d = omDrugExposure365d;
	}

	/**
	 * @return the omDrugExposure30d
	 */
	public int getOmDrugExposure30d() {
		return omDrugExposure30d;
	}

	/**
	 * @param omDrugExposure30d the omDrugExposure30d to set
	 */
	public void setOmDrugExposure30d(int omDrugExposure30d) {
		this.omDrugExposure30d = omDrugExposure30d;
	}

	/**
	 * @return the omDrugEra
	 */
	public int getOmDrugEra() {
		return omDrugEra;
	}

	/**
	 * @param omDrugEra the omDrugEra to set
	 */
	public void setOmDrugEra(int omDrugEra) {
		this.omDrugEra = omDrugEra;
	}

	/**
	 * @return the omDrugEra365d
	 */
	public int getOmDrugEra365d() {
		return omDrugEra365d;
	}

	/**
	 * @param omDrugEra365d the omDrugEra365d to set
	 */
	public void setOmDrugEra365d(int omDrugEra365d) {
		this.omDrugEra365d = omDrugEra365d;
	}

	/**
	 * @return the omDrugEra30d
	 */
	public int getOmDrugEra30d() {
		return omDrugEra30d;
	}

	/**
	 * @param omDrugEra30d the omDrugEra30d to set
	 */
	public void setOmDrugEra30d(int omDrugEra30d) {
		this.omDrugEra30d = omDrugEra30d;
	}

	/**
	 * @return the omDrugEraOverlap
	 */
	public int getOmDrugEraOverlap() {
		return omDrugEraOverlap;
	}

	/**
	 * @param omDrugEraOverlap the omDrugEraOverlap to set
	 */
	public void setOmDrugEraOverlap(int omDrugEraOverlap) {
		this.omDrugEraOverlap = omDrugEraOverlap;
	}

	/**
	 * @return the omDrugEraEver
	 */
	public int getOmDrugEraEver() {
		return omDrugEraEver;
	}

	/**
	 * @param omDrugEraEver the omDrugEraEver to set
	 */
	public void setOmDrugEraEver(int omDrugEraEver) {
		this.omDrugEraEver = omDrugEraEver;
	}

	/**
	 * @return the omDrugGroup
	 */
	public int getOmDrugGroup() {
		return omDrugGroup;
	}

	/**
	 * @param omDrugGroup the omDrugGroup to set
	 */
	public void setOmDrugGroup(int omDrugGroup) {
		this.omDrugGroup = omDrugGroup;
	}

	/**
	 * @return the omProcedureOcc
	 */
	public int getOmProcedureOcc() {
		return omProcedureOcc;
	}

	/**
	 * @param omProcedureOcc the omProcedureOcc to set
	 */
	public void setOmProcedureOcc(int omProcedureOcc) {
		this.omProcedureOcc = omProcedureOcc;
	}

	/**
	 * @return the omProcedureOcc365d
	 */
	public int getOmProcedureOcc365d() {
		return omProcedureOcc365d;
	}

	/**
	 * @param omProcedureOcc365d the omProcedureOcc365d to set
	 */
	public void setOmProcedureOcc365d(int omProcedureOcc365d) {
		this.omProcedureOcc365d = omProcedureOcc365d;
	}

	/**
	 * @return the omProcedureOcc30d
	 */
	public int getOmProcedureOcc30d() {
		return omProcedureOcc30d;
	}

	/**
	 * @param omProcedureOcc30d the omProcedureOcc30d to set
	 */
	public void setOmProcedureOcc30d(int omProcedureOcc30d) {
		this.omProcedureOcc30d = omProcedureOcc30d;
	}

	/**
	 * @return the omProcedureGroup
	 */
	public int getOmProcedureGroup() {
		return omProcedureGroup;
	}

	/**
	 * @param omProcedureGroup the omProcedureGroup to set
	 */
	public void setOmProcedureGroup(int omProcedureGroup) {
		this.omProcedureGroup = omProcedureGroup;
	}

	/**
	 * @return the omObservation
	 */
	public int getOmObservation() {
		return omObservation;
	}

	/**
	 * @param omObservation the omObservation to set
	 */
	public void setOmObservation(int omObservation) {
		this.omObservation = omObservation;
	}

	/**
	 * @return the omObservation365d
	 */
	public int getOmObservation365d() {
		return omObservation365d;
	}

	/**
	 * @param omObservation365d the omObservation365d to set
	 */
	public void setOmObservation365d(int omObservation365d) {
		this.omObservation365d = omObservation365d;
	}

	/**
	 * @return the omObservation30d
	 */
	public int getOmObservation30d() {
		return omObservation30d;
	}

	/**
	 * @param omObservation30d the omObservation30d to set
	 */
	public void setOmObservation30d(int omObservation30d) {
		this.omObservation30d = omObservation30d;
	}

	/**
	 * @return the omObservationCount365d
	 */
	public int getOmObservationCount365d() {
		return omObservationCount365d;
	}

	/**
	 * @param omObservationCount365d the omObservationCount365d to set
	 */
	public void setOmObservationCount365d(int omObservationCount365d) {
		this.omObservationCount365d = omObservationCount365d;
	}

	/**
	 * @return the omMeasurement
	 */
	public int getOmMeasurement() {
		return omMeasurement;
	}

	/**
	 * @param omMeasurement the omMeasurement to set
	 */
	public void setOmMeasurement(int omMeasurement) {
		this.omMeasurement = omMeasurement;
	}

	/**
	 * @return the omMeasurement365d
	 */
	public int getOmMeasurement365d() {
		return omMeasurement365d;
	}

	/**
	 * @param omMeasurement365d the omMeasurement365d to set
	 */
	public void setOmMeasurement365d(int omMeasurement365d) {
		this.omMeasurement365d = omMeasurement365d;
	}

	/**
	 * @return the omMeasurement30d
	 */
	public int getOmMeasurement30d() {
		return omMeasurement30d;
	}

	/**
	 * @param omMeasurement30d the omMeasurement30d to set
	 */
	public void setOmMeasurement30d(int omMeasurement30d) {
		this.omMeasurement30d = omMeasurement30d;
	}

	/**
	 * @return the omMeasurementCount365d
	 */
	public int getOmMeasurementCount365d() {
		return omMeasurementCount365d;
	}

	/**
	 * @param omMeasurementCount365d the omMeasurementCount365d to set
	 */
	public void setOmMeasurementCount365d(int omMeasurementCount365d) {
		this.omMeasurementCount365d = omMeasurementCount365d;
	}

	/**
	 * @return the omMeasurementBelow
	 */
	public int getOmMeasurementBelow() {
		return omMeasurementBelow;
	}

	/**
	 * @param omMeasurementBelow the omMeasurementBelow to set
	 */
	public void setOmMeasurementBelow(int omMeasurementBelow) {
		this.omMeasurementBelow = omMeasurementBelow;
	}

	/**
	 * @return the omMeasurementAbove
	 */
	public int getOmMeasurementAbove() {
		return omMeasurementAbove;
	}

	/**
	 * @param omMeasurementAbove the omMeasurementAbove to set
	 */
	public void setOmMeasurementAbove(int omMeasurementAbove) {
		this.omMeasurementAbove = omMeasurementAbove;
	}

	/**
	 * @return the omConceptCounts
	 */
	public int getOmConceptCounts() {
		return omConceptCounts;
	}

	/**
	 * @param omConceptCounts the omConceptCounts to set
	 */
	public void setOmConceptCounts(int omConceptCounts) {
		this.omConceptCounts = omConceptCounts;
	}

	/**
	 * @return the omRiskScores
	 */
	public int getOmRiskScores() {
		return omRiskScores;
	}

	/**
	 * @param omRiskScores the omRiskScores to set
	 */
	public void setOmRiskScores(int omRiskScores) {
		this.omRiskScores = omRiskScores;
	}

	/**
	 * @return the omRiskScoresCharlson
	 */
	public int getOmRiskScoresCharlson() {
		return omRiskScoresCharlson;
	}

	/**
	 * @param omRiskScoresCharlson the omRiskScoresCharlson to set
	 */
	public void setOmRiskScoresCharlson(int omRiskScoresCharlson) {
		this.omRiskScoresCharlson = omRiskScoresCharlson;
	}

	/**
	 * @return the omRiskScoresDcsi
	 */
	public int getOmRiskScoresDcsi() {
		return omRiskScoresDcsi;
	}

	/**
	 * @param omRiskScoresDcsi the omRiskScoresDcsi to set
	 */
	public void setOmRiskScoresDcsi(int omRiskScoresDcsi) {
		this.omRiskScoresDcsi = omRiskScoresDcsi;
	}

	/**
	 * @return the omRiskScoresChads2
	 */
	public int getOmRiskScoresChads2() {
		return omRiskScoresChads2;
	}

	/**
	 * @param omRiskScoresChads2 the omRiskScoresChads2 to set
	 */
	public void setOmRiskScoresChads2(int omRiskScoresChads2) {
		this.omRiskScoresChads2 = omRiskScoresChads2;
	}

	/**
	 * @return the omRiskScoresChads2vasc
	 */
	public int getOmRiskScoresChads2vasc() {
		return omRiskScoresChads2vasc;
	}

	/**
	 * @param omRiskScoresChads2vasc the omRiskScoresChads2vasc to set
	 */
	public void setOmRiskScoresChads2vasc(int omRiskScoresChads2vasc) {
		this.omRiskScoresChads2vasc = omRiskScoresChads2vasc;
	}

	/**
	 * @return the omInteractionYear
	 */
	public int getOmInteractionYear() {
		return omInteractionYear;
	}

	/**
	 * @param omInteractionYear the omInteractionYear to set
	 */
	public void setOmInteractionYear(int omInteractionYear) {
		this.omInteractionYear = omInteractionYear;
	}

	/**
	 * @return the omInteractionMonth
	 */
	public int getOmInteractionMonth() {
		return omInteractionMonth;
	}

	/**
	 * @param omInteractionMonth the omInteractionMonth to set
	 */
	public void setOmInteractionMonth(int omInteractionMonth) {
		this.omInteractionMonth = omInteractionMonth;
	}

	/**
	 * @return the delCovariatesSmallCount
	 */
	public int getDelCovariatesSmallCount() {
		return delCovariatesSmallCount;
	}

	/**
	 * @param delCovariatesSmallCount the delCovariatesSmallCount to set
	 */
	public void setDelCovariatesSmallCount(int delCovariatesSmallCount) {
		this.delCovariatesSmallCount = delCovariatesSmallCount;
	}

	/**
	 * @return the negativeControlId
	 */
	public int getNegativeControlId() {
		return negativeControlId;
	}

	/**
	 * @param negativeControlId the negativeControlId to set
	 */
	public void setNegativeControlId(int negativeControlId) {
		this.negativeControlId = negativeControlId;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the comparativeCohortAnalysis
	 */
	@JsonIgnore
	public ComparativeCohortAnalysis getComparativeCohortAnalysis() {
		return comparativeCohortAnalysis;
	}

	/**
	 * @param comparativeCohortAnalysis the comparativeCohortAnalysis to set
	 */
	@JsonProperty
	public void setComparativeCohortAnalysis(ComparativeCohortAnalysis comparativeCohortAnalysis) {
		this.comparativeCohortAnalysis = comparativeCohortAnalysis;
	}

}
