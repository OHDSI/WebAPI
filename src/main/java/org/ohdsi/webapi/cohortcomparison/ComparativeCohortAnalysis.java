/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortcomparison;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.ohdsi.webapi.model.CommonEntity;

/**
 * @author Frank DeFalco <fdefalco@ohdsi.org>
 */
@Entity(name = "ComparativeCohortAnalysis")
@Table(name = "cca")
@AttributeOverrides({
        @AttributeOverride(name = "createdDate", column = @Column(name = "created")),
        @AttributeOverride(name = "modifiedDate", column = @Column(name = "modified"))
})
public class ComparativeCohortAnalysis extends CommonEntity implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "cca_id")
    private Integer analysisId;

    @Column(name = "name")
    private String name;

    @Column(name = "treatment_id")
    private Integer treatmentId;

    @Column(name = "comparator_id")
    private Integer comparatorId;

    @Column(name = "outcome_id")
    private Integer outcomeId;

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

    @Column(name = "ps_exclusion_id")
    private int psExclusionId;

    @Column(name = "ps_inclusion_id")
    private int psInclusionId;

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

    @Column(name = "sec_user_id")
    private Integer userId = 0;

    public Integer getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Integer id) {
        this.analysisId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(Integer treatmentId) {
        this.treatmentId = treatmentId;
    }

    public Integer getComparatorId() {
        return comparatorId;
    }

    public void setComparatorId(Integer comparatorId) {
        this.comparatorId = comparatorId;
    }

    public Integer getOutcomeId() {
        return outcomeId;
    }

    public void setOutcomeId(Integer outcomeId) {
        this.outcomeId = outcomeId;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public int getTimeAtRiskStart() {
        return timeAtRiskStart;
    }

    public void setTimeAtRiskStart(int timeAtRiskStart) {
        this.timeAtRiskStart = timeAtRiskStart;
    }

    public int getTimeAtRiskEnd() {
        return timeAtRiskEnd;
    }

    public void setTimeAtRiskEnd(int timeAtRiskEnd) {
        this.timeAtRiskEnd = timeAtRiskEnd;
    }

    public int getAddExposureDaysToEnd() {
        return addExposureDaysToEnd;
    }

    public void setAddExposureDaysToEnd(int addExposureDaysToEnd) {
        this.addExposureDaysToEnd = addExposureDaysToEnd;
    }

    public int getMinimumWashoutPeriod() {
        return minimumWashoutPeriod;
    }

    public void setMinimumWashoutPeriod(int minimumWashoutPeriod) {
        this.minimumWashoutPeriod = minimumWashoutPeriod;
    }

    public int getMinimumDaysAtRisk() {
        return minimumDaysAtRisk;
    }

    public void setMinimumDaysAtRisk(int minimumDaysAtRisk) {
        this.minimumDaysAtRisk = minimumDaysAtRisk;
    }

    public int getRmSubjectsInBothCohorts() {
        return rmSubjectsInBothCohorts;
    }

    public void setRmSubjectsInBothCohorts(int rmSubjectsInBothCohorts) {
        this.rmSubjectsInBothCohorts = rmSubjectsInBothCohorts;
    }

    public int getRmPriorOutcomes() {
        return rmPriorOutcomes;
    }

    public void setRmPriorOutcomes(int rmPriorOutcomes) {
        this.rmPriorOutcomes = rmPriorOutcomes;
    }

    public int getPsAdjustment() {
        return psAdjustment;
    }

    public void setPsAdjustment(int psAdjustment) {
        this.psAdjustment = psAdjustment;
    }

    public int getPsExclusionId() {
        return psExclusionId;
    }

    public void setPsExclusionId(int psExclusionId) {
        this.psExclusionId = psExclusionId;
    }

    public int getPsInclusionId() {
        return psInclusionId;
    }

    public void setPsInclusionId(int psInclusionId) {
        this.psInclusionId = psInclusionId;
    }

    public int getPsDemographics() {
        return psDemographics;
    }

    public void setPsDemographics(int psDemographics) {
        this.psDemographics = psDemographics;
    }

    public int getPsDemographicsGender() {
        return psDemographicsGender;
    }

    public void setPsDemographicsGender(int psDemographicsGender) {
        this.psDemographicsGender = psDemographicsGender;
    }

    public int getPsDemographicsRace() {
        return psDemographicsRace;
    }

    public void setPsDemographicsRace(int psDemographicsRace) {
        this.psDemographicsRace = psDemographicsRace;
    }

    public int getPsDemographicsEthnicity() {
        return psDemographicsEthnicity;
    }

    public void setPsDemographicsEthnicity(int psDemographicsEthnicity) {
        this.psDemographicsEthnicity = psDemographicsEthnicity;
    }

    public int getPsDemographicsAge() {
        return psDemographicsAge;
    }

    public void setPsDemographicsAge(int psDemographicsAge) {
        this.psDemographicsAge = psDemographicsAge;
    }

    public int getPsDemographicsYear() {
        return psDemographicsYear;
    }

    public void setPsDemographicsYear(int psDemographicsYear) {
        this.psDemographicsYear = psDemographicsYear;
    }

    public int getPsDemographicsMonth() {
        return psDemographicsMonth;
    }

    public void setPsDemographicsMonth(int psDemographicsMonth) {
        this.psDemographicsMonth = psDemographicsMonth;
    }

    public int getPsTrim() {
        return psTrim;
    }

    public void setPsTrim(int psTrim) {
        this.psTrim = psTrim;
    }

    public int getPsTrimFraction() {
        return psTrimFraction;
    }

    public void setPsTrimFraction(int psTrimFraction) {
        this.psTrimFraction = psTrimFraction;
    }

    public int getPsMatch() {
        return psMatch;
    }

    public void setPsMatch(int psMatch) {
        this.psMatch = psMatch;
    }

    public int getPsMatchMaxRatio() {
        return psMatchMaxRatio;
    }

    public void setPsMatchMaxRatio(int psMatchMaxRatio) {
        this.psMatchMaxRatio = psMatchMaxRatio;
    }

    public int getPsStrat() {
        return psStrat;
    }

    public void setPsStrat(int psStrat) {
        this.psStrat = psStrat;
    }

    public int getPsStratNumStrata() {
        return psStratNumStrata;
    }

    public void setPsStratNumStrata(int psStratNumStrata) {
        this.psStratNumStrata = psStratNumStrata;
    }

    public int getPsConditionOcc() {
        return psConditionOcc;
    }

    public void setPsConditionOcc(int psConditionOcc) {
        this.psConditionOcc = psConditionOcc;
    }

    public int getPsConditionOcc365d() {
        return psConditionOcc365d;
    }

    public void setPsConditionOcc365d(int psConditionOcc365d) {
        this.psConditionOcc365d = psConditionOcc365d;
    }

    public int getPsConditionOcc30d() {
        return psConditionOcc30d;
    }

    public void setPsConditionOcc30d(int psConditionOcc30d) {
        this.psConditionOcc30d = psConditionOcc30d;
    }

    public int getPsConditionOccInpt180d() {
        return psConditionOccInpt180d;
    }

    public void setPsConditionOccInpt180d(int psConditionOccInpt180d) {
        this.psConditionOccInpt180d = psConditionOccInpt180d;
    }

    public int getPsConditionEra() {
        return psConditionEra;
    }

    public void setPsConditionEra(int psConditionEra) {
        this.psConditionEra = psConditionEra;
    }

    public int getPsConditionEraEver() {
        return psConditionEraEver;
    }

    public void setPsConditionEraEver(int psConditionEraEver) {
        this.psConditionEraEver = psConditionEraEver;
    }

    public int getPsConditionEraOverlap() {
        return psConditionEraOverlap;
    }

    public void setPsConditionEraOverlap(int psConditionEraOverlap) {
        this.psConditionEraOverlap = psConditionEraOverlap;
    }

    public int getPsConditionGroup() {
        return psConditionGroup;
    }

    public void setPsConditionGroup(int psConditionGroup) {
        this.psConditionGroup = psConditionGroup;
    }

    public int getPsConditionGroupMeddra() {
        return psConditionGroupMeddra;
    }

    public void setPsConditionGroupMeddra(int psConditionGroupMeddra) {
        this.psConditionGroupMeddra = psConditionGroupMeddra;
    }

    public int getPsConditionGroupSnomed() {
        return psConditionGroupSnomed;
    }

    public void setPsConditionGroupSnomed(int psConditionGroupSnomed) {
        this.psConditionGroupSnomed = psConditionGroupSnomed;
    }

    public int getPsDrugExposure() {
        return psDrugExposure;
    }

    public void setPsDrugExposure(int psDrugExposure) {
        this.psDrugExposure = psDrugExposure;
    }

    public int getPsDrugExposure365d() {
        return psDrugExposure365d;
    }

    public void setPsDrugExposure365d(int psDrugExposure365d) {
        this.psDrugExposure365d = psDrugExposure365d;
    }

    public int getPsDrugExposure30d() {
        return psDrugExposure30d;
    }

    public void setPsDrugExposure30d(int psDrugExposure30d) {
        this.psDrugExposure30d = psDrugExposure30d;
    }

    public int getPsDrugEra() {
        return psDrugEra;
    }

    public void setPsDrugEra(int psDrugEra) {
        this.psDrugEra = psDrugEra;
    }

    public int getPsDrugEra365d() {
        return psDrugEra365d;
    }

    public void setPsDrugEra365d(int psDrugEra365d) {
        this.psDrugEra365d = psDrugEra365d;
    }

    public int getPsDrugEra30d() {
        return psDrugEra30d;
    }

    public void setPsDrugEra30d(int psDrugEra30d) {
        this.psDrugEra30d = psDrugEra30d;
    }

    public int getPsDrugEraOverlap() {
        return psDrugEraOverlap;
    }

    public void setPsDrugEraOverlap(int psDrugEraOverlap) {
        this.psDrugEraOverlap = psDrugEraOverlap;
    }

    public int getPsDrugEraEver() {
        return psDrugEraEver;
    }

    public void setPsDrugEraEver(int psDrugEraEver) {
        this.psDrugEraEver = psDrugEraEver;
    }

    public int getPsDrugGroup() {
        return psDrugGroup;
    }

    public void setPsDrugGroup(int psDrugGroup) {
        this.psDrugGroup = psDrugGroup;
    }

    public int getPsProcedureOcc() {
        return psProcedureOcc;
    }

    public void setPsProcedureOcc(int psProcedureOcc) {
        this.psProcedureOcc = psProcedureOcc;
    }

    public int getPsProcedureOcc365d() {
        return psProcedureOcc365d;
    }

    public void setPsProcedureOcc365d(int psProcedureOcc365d) {
        this.psProcedureOcc365d = psProcedureOcc365d;
    }

    public int getPsProcedureOcc30d() {
        return psProcedureOcc30d;
    }

    public void setPsProcedureOcc30d(int psProcedureOcc30d) {
        this.psProcedureOcc30d = psProcedureOcc30d;
    }

    public int getPsProcedureGroup() {
        return psProcedureGroup;
    }

    public void setPsProcedureGroup(int psProcedureGroup) {
        this.psProcedureGroup = psProcedureGroup;
    }

    public int getPsObservation() {
        return psObservation;
    }

    public void setPsObservation(int psObservation) {
        this.psObservation = psObservation;
    }

    public int getPsObservation365d() {
        return psObservation365d;
    }

    public void setPsObservation365d(int psObservation365d) {
        this.psObservation365d = psObservation365d;
    }

    public int getPsObservation30d() {
        return psObservation30d;
    }

    public void setPsObservation30d(int psObservation30d) {
        this.psObservation30d = psObservation30d;
    }

    public int getPsObservationCount365d() {
        return psObservationCount365d;
    }

    public void setPsObservationCount365d(int psObservationCount365d) {
        this.psObservationCount365d = psObservationCount365d;
    }

    public int getPsMeasurement() {
        return psMeasurement;
    }

    public void setPsMeasurement(int psMeasurement) {
        this.psMeasurement = psMeasurement;
    }

    public int getPsMeasurement365d() {
        return psMeasurement365d;
    }

    public void setPsMeasurement365d(int psMeasurement365d) {
        this.psMeasurement365d = psMeasurement365d;
    }

    public int getPsMeasurement30d() {
        return psMeasurement30d;
    }

    public void setPsMeasurement30d(int psMeasurement30d) {
        this.psMeasurement30d = psMeasurement30d;
    }

    public int getPsMeasurementCount365d() {
        return psMeasurementCount365d;
    }

    public void setPsMeasurementCount365d(int psMeasurementCount365d) {
        this.psMeasurementCount365d = psMeasurementCount365d;
    }

    public int getPsMeasurementBelow() {
        return psMeasurementBelow;
    }

    public void setPsMeasurementBelow(int psMeasurementBelow) {
        this.psMeasurementBelow = psMeasurementBelow;
    }

    public int getPsMeasurementAbove() {
        return psMeasurementAbove;
    }

    public void setPsMeasurementAbove(int psMeasurementAbove) {
        this.psMeasurementAbove = psMeasurementAbove;
    }

    public int getPsConceptCounts() {
        return psConceptCounts;
    }

    public void setPsConceptCounts(int psConceptCounts) {
        this.psConceptCounts = psConceptCounts;
    }

    public int getPsRiskScores() {
        return psRiskScores;
    }

    public void setPsRiskScores(int psRiskScores) {
        this.psRiskScores = psRiskScores;
    }

    public int getPsRiskScoresCharlson() {
        return psRiskScoresCharlson;
    }

    public void setPsRiskScoresCharlson(int psRiskScoresCharlson) {
        this.psRiskScoresCharlson = psRiskScoresCharlson;
    }

    public int getPsRiskScoresDcsi() {
        return psRiskScoresDcsi;
    }

    public void setPsRiskScoresDcsi(int psRiskScoresDcsi) {
        this.psRiskScoresDcsi = psRiskScoresDcsi;
    }

    public int getPsRiskScoresChads2() {
        return psRiskScoresChads2;
    }

    public void setPsRiskScoresChads2(int psRiskScoresChads2) {
        this.psRiskScoresChads2 = psRiskScoresChads2;
    }

    public int getPsRiskScoresChads2vasc() {
        return psRiskScoresChads2vasc;
    }

    public void setPsRiskScoresChads2vasc(int psRiskScoresChads2vasc) {
        this.psRiskScoresChads2vasc = psRiskScoresChads2vasc;
    }

    public int getPsInteractionYear() {
        return psInteractionYear;
    }

    public void setPsInteractionYear(int psInteractionYear) {
        this.psInteractionYear = psInteractionYear;
    }

    public int getPsInteractionMonth() {
        return psInteractionMonth;
    }

    public void setPsInteractionMonth(int psInteractionMonth) {
        this.psInteractionMonth = psInteractionMonth;
    }

    public int getOmCovariates() {
        return omCovariates;
    }

    public void setOmCovariates(int omCovariates) {
        this.omCovariates = omCovariates;
    }

    public int getOmExclusionId() {
        return omExclusionId;
    }

    public void setOmExclusionId(int omExclusionId) {
        this.omExclusionId = omExclusionId;
    }

    public int getOmInclusionId() {
        return omInclusionId;
    }

    public void setOmInclusionId(int omInclusionId) {
        this.omInclusionId = omInclusionId;
    }

    public int getOmDemographics() {
        return omDemographics;
    }

    public void setOmDemographics(int omDemographics) {
        this.omDemographics = omDemographics;
    }

    public int getOmDemographicsGender() {
        return omDemographicsGender;
    }

    public void setOmDemographicsGender(int omDemographicsGender) {
        this.omDemographicsGender = omDemographicsGender;
    }

    public int getOmDemographicsRace() {
        return omDemographicsRace;
    }

    public void setOmDemographicsRace(int omDemographicsRace) {
        this.omDemographicsRace = omDemographicsRace;
    }

    public int getOmDemographicsEthnicity() {
        return omDemographicsEthnicity;
    }

    public void setOmDemographicsEthnicity(int omDemographicsEthnicity) {
        this.omDemographicsEthnicity = omDemographicsEthnicity;
    }

    public int getOmDemographicsAge() {
        return omDemographicsAge;
    }

    public void setOmDemographicsAge(int omDemographicsAge) {
        this.omDemographicsAge = omDemographicsAge;
    }

    public int getOmDemographicsYear() {
        return omDemographicsYear;
    }

    public void setOmDemographicsYear(int omDemographicsYear) {
        this.omDemographicsYear = omDemographicsYear;
    }

    public int getOmDemographicsMonth() {
        return omDemographicsMonth;
    }

    public void setOmDemographicsMonth(int omDemographicsMonth) {
        this.omDemographicsMonth = omDemographicsMonth;
    }

    public int getOmTrim() {
        return omTrim;
    }

    public void setOmTrim(int omTrim) {
        this.omTrim = omTrim;
    }

    public int getOmTrimFraction() {
        return omTrimFraction;
    }

    public void setOmTrimFraction(int omTrimFraction) {
        this.omTrimFraction = omTrimFraction;
    }

    public int getOmMatch() {
        return omMatch;
    }

    public void setOmMatch(int omMatch) {
        this.omMatch = omMatch;
    }

    public int getOmMatchMaxRatio() {
        return omMatchMaxRatio;
    }

    public void setOmMatchMaxRatio(int omMatchMaxRatio) {
        this.omMatchMaxRatio = omMatchMaxRatio;
    }

    public int getOmStrat() {
        return omStrat;
    }

    public void setOmStrat(int omStrat) {
        this.omStrat = omStrat;
    }

    public int getOmStratNumStrata() {
        return omStratNumStrata;
    }

    public void setOmStratNumStrata(int omStratNumStrata) {
        this.omStratNumStrata = omStratNumStrata;
    }

    public int getOmConditionOcc() {
        return omConditionOcc;
    }

    public void setOmConditionOcc(int omConditionOcc) {
        this.omConditionOcc = omConditionOcc;
    }

    public int getOmConditionOcc365d() {
        return omConditionOcc365d;
    }

    public void setOmConditionOcc365d(int omConditionOcc365d) {
        this.omConditionOcc365d = omConditionOcc365d;
    }

    public int getOmConditionOcc30d() {
        return omConditionOcc30d;
    }

    public void setOmConditionOcc30d(int omConditionOcc30d) {
        this.omConditionOcc30d = omConditionOcc30d;
    }

    public int getOmConditionOccInpt180d() {
        return omConditionOccInpt180d;
    }

    public void setOmConditionOccInpt180d(int omConditionOccInpt180d) {
        this.omConditionOccInpt180d = omConditionOccInpt180d;
    }

    public int getOmConditionEra() {
        return omConditionEra;
    }

    public void setOmConditionEra(int omConditionEra) {
        this.omConditionEra = omConditionEra;
    }

    public int getOmConditionEraEver() {
        return omConditionEraEver;
    }

    public void setOmConditionEraEver(int omConditionEraEver) {
        this.omConditionEraEver = omConditionEraEver;
    }

    public int getOmConditionEraOverlap() {
        return omConditionEraOverlap;
    }

    public void setOmConditionEraOverlap(int omConditionEraOverlap) {
        this.omConditionEraOverlap = omConditionEraOverlap;
    }

    public int getOmConditionGroup() {
        return omConditionGroup;
    }

    public void setOmConditionGroup(int omConditionGroup) {
        this.omConditionGroup = omConditionGroup;
    }

    public int getOmConditionGroupMeddra() {
        return omConditionGroupMeddra;
    }

    public void setOmConditionGroupMeddra(int omConditionGroupMeddra) {
        this.omConditionGroupMeddra = omConditionGroupMeddra;
    }

    public int getOmConditionGroupSnomed() {
        return omConditionGroupSnomed;
    }

    public void setOmConditionGroupSnomed(int omConditionGroupSnomed) {
        this.omConditionGroupSnomed = omConditionGroupSnomed;
    }

    public int getOmDrugExposure() {
        return omDrugExposure;
    }

    public void setOmDrugExposure(int omDrugExposure) {
        this.omDrugExposure = omDrugExposure;
    }

    public int getOmDrugExposure365d() {
        return omDrugExposure365d;
    }

    public void setOmDrugExposure365d(int omDrugExposure365d) {
        this.omDrugExposure365d = omDrugExposure365d;
    }

    public int getOmDrugExposure30d() {
        return omDrugExposure30d;
    }

    public void setOmDrugExposure30d(int omDrugExposure30d) {
        this.omDrugExposure30d = omDrugExposure30d;
    }

    public int getOmDrugEra() {
        return omDrugEra;
    }

    public void setOmDrugEra(int omDrugEra) {
        this.omDrugEra = omDrugEra;
    }

    public int getOmDrugEra365d() {
        return omDrugEra365d;
    }

    public void setOmDrugEra365d(int omDrugEra365d) {
        this.omDrugEra365d = omDrugEra365d;
    }

    public int getOmDrugEra30d() {
        return omDrugEra30d;
    }

    public void setOmDrugEra30d(int omDrugEra30d) {
        this.omDrugEra30d = omDrugEra30d;
    }

    public int getOmDrugEraOverlap() {
        return omDrugEraOverlap;
    }

    public void setOmDrugEraOverlap(int omDrugEraOverlap) {
        this.omDrugEraOverlap = omDrugEraOverlap;
    }

    public int getOmDrugEraEver() {
        return omDrugEraEver;
    }

    public void setOmDrugEraEver(int omDrugEraEver) {
        this.omDrugEraEver = omDrugEraEver;
    }

    public int getOmDrugGroup() {
        return omDrugGroup;
    }

    public void setOmDrugGroup(int omDrugGroup) {
        this.omDrugGroup = omDrugGroup;
    }

    public int getOmProcedureOcc() {
        return omProcedureOcc;
    }

    public void setOmProcedureOcc(int omProcedureOcc) {
        this.omProcedureOcc = omProcedureOcc;
    }

    public int getOmProcedureOcc365d() {
        return omProcedureOcc365d;
    }

    public void setOmProcedureOcc365d(int omProcedureOcc365d) {
        this.omProcedureOcc365d = omProcedureOcc365d;
    }

    public int getOmProcedureOcc30d() {
        return omProcedureOcc30d;
    }

    public void setOmProcedureOcc30d(int omProcedureOcc30d) {
        this.omProcedureOcc30d = omProcedureOcc30d;
    }

    public int getOmProcedureGroup() {
        return omProcedureGroup;
    }

    public void setOmProcedureGroup(int omProcedureGroup) {
        this.omProcedureGroup = omProcedureGroup;
    }

    public int getOmObservation() {
        return omObservation;
    }

    public void setOmObservation(int omObservation) {
        this.omObservation = omObservation;
    }

    public int getOmObservation365d() {
        return omObservation365d;
    }

    public void setOmObservation365d(int omObservation365d) {
        this.omObservation365d = omObservation365d;
    }

    public int getOmObservation30d() {
        return omObservation30d;
    }

    public void setOmObservation30d(int omObservation30d) {
        this.omObservation30d = omObservation30d;
    }

    public int getOmObservationCount365d() {
        return omObservationCount365d;
    }

    public void setOmObservationCount365d(int omObservationCount365d) {
        this.omObservationCount365d = omObservationCount365d;
    }

    public int getOmMeasurement() {
        return omMeasurement;
    }

    public void setOmMeasurement(int omMeasurement) {
        this.omMeasurement = omMeasurement;
    }

    public int getOmMeasurement365d() {
        return omMeasurement365d;
    }

    public void setOmMeasurement365d(int omMeasurement365d) {
        this.omMeasurement365d = omMeasurement365d;
    }

    public int getOmMeasurement30d() {
        return omMeasurement30d;
    }

    public void setOmMeasurement30d(int omMeasurement30d) {
        this.omMeasurement30d = omMeasurement30d;
    }

    public int getOmMeasurementCount365d() {
        return omMeasurementCount365d;
    }

    public void setOmMeasurementCount365d(int omMeasurementCount365d) {
        this.omMeasurementCount365d = omMeasurementCount365d;
    }

    public int getOmMeasurementBelow() {
        return omMeasurementBelow;
    }

    public void setOmMeasurementBelow(int omMeasurementBelow) {
        this.omMeasurementBelow = omMeasurementBelow;
    }

    public int getOmMeasurementAbove() {
        return omMeasurementAbove;
    }

    public void setOmMeasurementAbove(int omMeasurementAbove) {
        this.omMeasurementAbove = omMeasurementAbove;
    }

    public int getOmConceptCounts() {
        return omConceptCounts;
    }

    public void setOmConceptCounts(int omConceptCounts) {
        this.omConceptCounts = omConceptCounts;
    }

    public int getOmRiskScores() {
        return omRiskScores;
    }

    public void setOmRiskScores(int omRiskScores) {
        this.omRiskScores = omRiskScores;
    }

    public int getOmRiskScoresCharlson() {
        return omRiskScoresCharlson;
    }

    public void setOmRiskScoresCharlson(int omRiskScoresCharlson) {
        this.omRiskScoresCharlson = omRiskScoresCharlson;
    }

    public int getOmRiskScoresDcsi() {
        return omRiskScoresDcsi;
    }

    public void setOmRiskScoresDcsi(int omRiskScoresDcsi) {
        this.omRiskScoresDcsi = omRiskScoresDcsi;
    }

    public int getOmRiskScoresChads2() {
        return omRiskScoresChads2;
    }

    public void setOmRiskScoresChads2(int omRiskScoresChads2) {
        this.omRiskScoresChads2 = omRiskScoresChads2;
    }

    public int getOmRiskScoresChads2vasc() {
        return omRiskScoresChads2vasc;
    }

    public void setOmRiskScoresChads2vasc(int omRiskScoresChads2vasc) {
        this.omRiskScoresChads2vasc = omRiskScoresChads2vasc;
    }

    public int getOmInteractionYear() {
        return omInteractionYear;
    }

    public void setOmInteractionYear(int omInteractionYear) {
        this.omInteractionYear = omInteractionYear;
    }

    public int getOmInteractionMonth() {
        return omInteractionMonth;
    }

    public void setOmInteractionMonth(int omInteractionMonth) {
        this.omInteractionMonth = omInteractionMonth;
    }

    public int getDelCovariatesSmallCount() {
        return delCovariatesSmallCount;
    }

    public void setDelCovariatesSmallCount(int delCovariatesSmallCount) {
        this.delCovariatesSmallCount = delCovariatesSmallCount;
    }

    public int getNegativeControlId() {
        return negativeControlId;
    }

    public void setNegativeControlId(int negativeControlId) {
        this.negativeControlId = negativeControlId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
