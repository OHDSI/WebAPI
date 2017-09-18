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
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 * @author Frank DeFalco <fdefalco@ohdsi.org>
 */
public class ComparativeCohortAnalysisInfo extends ComparativeCohortAnalysis implements Serializable {
    private String treatmentCaption;
    private String treatmentCohortDefinition;
    private String comparatorCaption;
    private String comparatorCohortDefinition;
    private String outcomeCaption;
    private String outcomeCohortDefinition;
    private String psInclusionCaption;
    private ConceptSetExpression psInclusionConceptSet; 
    private String psInclusionConceptSetSql;
    private String psExclusionCaption;
    private ConceptSetExpression psExclusionConceptSet;
    private String psExclusionConceptSetSql;
    private String omInclusionCaption;
    private ConceptSetExpression omInclusionConceptSet;
    private String omInclusionConceptSetSql;
    private String omExclusionCaption;
    private ConceptSetExpression omExclusionConceptSet;
    private String omExclusionConceptSetSql;
    private String negativeControlCaption;
    private ConceptSetExpression negativeControlConceptSet;
    private String negativeControlConceptSetSql;
  
  public ComparativeCohortAnalysisInfo(ComparativeCohortAnalysis analysis) {
      this.setAddExposureDaysToEnd(analysis.getAddExposureDaysToEnd());
      this.setAnalysisId(analysis.getAnalysisId());
      this.setComparatorId(analysis.getComparatorId());
      this.setCreated(analysis.getCreated());
      this.setDelCovariatesSmallCount(analysis.getDelCovariatesSmallCount());
      this.setMinimumDaysAtRisk(analysis.getMinimumDaysAtRisk());
      this.setMinimumWashoutPeriod(analysis.getMinimumWashoutPeriod());
      this.setModelType(analysis.getModelType());
      this.setModified(analysis.getModified());
      this.setName(analysis.getName());
      this.setNegativeControlId(analysis.getNegativeControlId());
      this.setOmConceptCounts(analysis.getOmConceptCounts());
      this.setOmConditionEra(analysis.getOmConditionEra());
      this.setOmConditionEraEver(analysis.getOmConditionEraEver());
      this.setOmConditionEraOverlap(analysis.getOmConditionEraOverlap());
      this.setOmConditionGroup(analysis.getOmConditionGroup());
      this.setOmConditionGroupMeddra(analysis.getOmConditionGroupMeddra());
      this.setOmConditionGroupSnomed(analysis.getOmConditionGroupSnomed());
      this.setOmConditionOcc(analysis.getOmConditionOcc());
      this.setOmConditionOcc30d(analysis.getOmConditionOcc30d());
      this.setOmConditionOcc365d(analysis.getOmConditionOcc365d());
      this.setOmConditionOccInpt180d(analysis.getOmConditionOccInpt180d());
      this.setOmCovariates(analysis.getOmCovariates());
      this.setOmDemographics(analysis.getOmDemographics());
      this.setOmDemographicsAge(analysis.getOmDemographicsAge());
      this.setOmDemographicsEthnicity(analysis.getOmDemographicsEthnicity());
      this.setOmDemographicsGender(analysis.getOmDemographicsGender());
      this.setOmDemographicsMonth(analysis.getOmDemographicsMonth());
      this.setOmDemographicsRace(analysis.getOmDemographicsRace());
      this.setOmDemographicsYear(analysis.getOmDemographicsYear());
      this.setOmDrugEra(analysis.getOmDrugEra());
      this.setOmDrugEra30d(analysis.getOmDrugEra());
      this.setOmDrugEra365d(analysis.getOmDrugEra365d());
      this.setOmDrugEraEver(analysis.getOmDrugEraEver());
      this.setOmDrugEraOverlap(analysis.getOmDrugEraOverlap());
      this.setOmDrugExposure(analysis.getOmDrugExposure());
      this.setOmDrugExposure30d(analysis.getOmDrugExposure30d());
      this.setOmDrugExposure365d(analysis.getOmDrugExposure365d());
      this.setOmDrugGroup(analysis.getOmDrugGroup());
      this.setOmExclusionId(analysis.getOmExclusionId());
      this.setOmInclusionId(analysis.getOmInclusionId());
      this.setOmInteractionMonth(analysis.getOmInteractionMonth());
      this.setOmInteractionYear(analysis.getOmInteractionYear());
      this.setOmMatch(analysis.getOmMatch());
      this.setOmMatchMaxRatio(analysis.getOmMatchMaxRatio());
      this.setOmMeasurement(analysis.getOmMeasurement());
      this.setOmMeasurement30d(analysis.getOmMeasurement30d());
      this.setOmMeasurement365d(analysis.getOmMeasurement365d());
      this.setOmMeasurementAbove(analysis.getOmMeasurementAbove());
      this.setOmMeasurementBelow(analysis.getOmMeasurementBelow());
      this.setOmMeasurementCount365d(analysis.getOmMeasurementCount365d());
      this.setOmObservation(analysis.getOmObservation());
      this.setOmObservation30d(analysis.getOmObservation30d());
      this.setOmObservation365d(analysis.getOmObservation365d());
      this.setOmObservationCount365d(analysis.getOmObservationCount365d());
      this.setOmProcedureGroup(analysis.getOmProcedureGroup());
      this.setOmProcedureOcc(analysis.getOmProcedureOcc());
      this.setOmProcedureOcc30d(analysis.getOmProcedureOcc30d());
      this.setOmProcedureOcc365d(analysis.getOmProcedureOcc365d());
      this.setOmRiskScores(analysis.getOmRiskScores());
      this.setOmRiskScoresChads2(analysis.getOmRiskScoresChads2());
      this.setOmRiskScoresChads2vasc(analysis.getOmRiskScoresChads2vasc());
      this.setOmRiskScoresCharlson(analysis.getOmRiskScoresCharlson());
      this.setOmRiskScoresDcsi(analysis.getOmRiskScoresDcsi());
      this.setOmStrat(analysis.getOmStrat());
      this.setOmStratNumStrata(analysis.getOmStratNumStrata());
      this.setOmTrim(analysis.getOmTrim());
      this.setOmTrimFraction(analysis.getOmTrimFraction());
      this.setOutcomeId(analysis.getOutcomeId());
      this.setPsAdjustment(analysis.getPsAdjustment());
      this.setPsConceptCounts(analysis.getPsConceptCounts());
      this.setPsConditionEra(analysis.getPsConditionEra());
      this.setPsConditionEraEver(analysis.getPsConditionEraEver());
      this.setPsConditionEraOverlap(analysis.getPsConditionEraOverlap());
      this.setPsConditionGroup(analysis.getPsConditionGroup());
      this.setPsConditionGroupMeddra(analysis.getPsConditionGroupMeddra());
      this.setPsConditionGroupSnomed(analysis.getPsConditionGroupSnomed());
      this.setPsConditionOcc(analysis.getPsConditionOcc());
      this.setPsConditionOcc30d(analysis.getPsConditionOcc30d());
      this.setPsConditionOcc365d(analysis.getPsConditionOcc365d());
      this.setPsConditionOccInpt180d(analysis.getPsConditionOccInpt180d());
      this.setPsDemographics(analysis.getPsDemographics());
      this.setPsDemographicsAge(analysis.getPsDemographicsAge());
      this.setPsDemographicsEthnicity(analysis.getPsDemographicsEthnicity());
      this.setPsDemographicsGender(analysis.getPsDemographicsGender());
      this.setPsDemographicsMonth(analysis.getPsDemographicsMonth());
      this.setPsDemographicsRace(analysis.getPsDemographicsRace());
      this.setPsDemographicsYear(analysis.getPsDemographicsYear());
      this.setPsDrugEra(analysis.getPsDrugEra());
      this.setPsDrugEra30d(analysis.getPsDrugEra30d());
      this.setPsDrugEra365d(analysis.getPsDrugEra365d());
      this.setPsDrugEraEver(analysis.getPsDrugEraEver());
      this.setPsDrugEraOverlap(analysis.getPsDrugEraOverlap());
      this.setPsDrugExposure(analysis.getPsDrugExposure());
      this.setPsDrugExposure30d(analysis.getPsDrugExposure30d());
      this.setPsDrugExposure365d(analysis.getPsDrugExposure365d());
      this.setPsDrugGroup(analysis.getPsDrugGroup());
      this.setPsExclusionId(analysis.getPsExclusionId());
      this.setPsInclusionId(analysis.getPsInclusionId());
      this.setPsInteractionMonth(analysis.getPsInteractionMonth());
      this.setPsInteractionYear(analysis.getPsInteractionYear());
      this.setPsMatch(analysis.getPsMatch());
      this.setPsMatchMaxRatio(analysis.getPsMatchMaxRatio());
      this.setPsMeasurement(analysis.getPsMeasurement());
      this.setPsMeasurement30d(analysis.getPsMeasurement30d());
      this.setPsMeasurement365d(analysis.getPsMeasurement365d());
      this.setPsMeasurementAbove(analysis.getPsMeasurementAbove());
      this.setPsMeasurementBelow(analysis.getPsMeasurementBelow());
      this.setPsMeasurementCount365d(analysis.getPsMeasurementCount365d());
      this.setPsObservation(analysis.getPsObservation());
      this.setPsObservation30d(analysis.getPsObservation30d());
      this.setPsObservation365d(analysis.getPsObservation365d());
      this.setPsObservationCount365d(analysis.getPsObservationCount365d());
      this.setPsProcedureGroup(analysis.getPsProcedureGroup());
      this.setPsProcedureOcc(analysis.getPsProcedureOcc());
      this.setPsProcedureOcc30d(analysis.getPsProcedureOcc30d());
      this.setPsProcedureOcc365d(analysis.getPsProcedureOcc365d());
      this.setPsRiskScores(analysis.getPsRiskScores());
      this.setPsRiskScoresChads2(analysis.getPsRiskScoresChads2());
      this.setPsRiskScoresChads2vasc(analysis.getPsRiskScoresChads2vasc());
      this.setPsRiskScoresCharlson(analysis.getPsRiskScoresCharlson());
      this.setPsRiskScoresDcsi(analysis.getPsRiskScoresDcsi());
      this.setPsStrat(analysis.getPsStrat());
      this.setPsStratNumStrata(analysis.getPsStratNumStrata());
      this.setPsTrim(analysis.getPsTrim());
      this.setPsTrimFraction(analysis.getPsTrimFraction());
      this.setRmPriorOutcomes(analysis.getRmPriorOutcomes());
      this.setRmSubjectsInBothCohorts(analysis.getRmSubjectsInBothCohorts());
      this.setTimeAtRiskEnd(analysis.getTimeAtRiskEnd());
      this.setTimeAtRiskStart(analysis.getTimeAtRiskStart());
      this.setTreatmentId(analysis.getTreatmentId());
      this.setUserId(analysis.getUserId());
  }

    /**
     * @return the comparatorCaption
     */
    public String getComparatorCaption() {
        return comparatorCaption;
    }

    /**
     * @return the comparatorCohortDefinition
     */
    public String getComparatorCohortDefinition() {
        return comparatorCohortDefinition;
    }

    /**
     * @return the negativeControlCaption
     */
    public String getNegativeControlCaption() {
        return negativeControlCaption;
    }

    /**
     * @return the negativeControlConceptSet
     */
    public ConceptSetExpression getNegativeControlConceptSet() {
        return negativeControlConceptSet;
    }

    /**
     * @return the omExclusionCaption
     */
    public String getOmExclusionCaption() {
        return omExclusionCaption;
    }

    /**
     * @return the omExclusionConceptSet
     */
    public ConceptSetExpression getOmExclusionConceptSet() {
        return omExclusionConceptSet;
    }

    /**
     * @return the omInclusionCaption
     */
    public String getOmInclusionCaption() {
        return omInclusionCaption;
    }

    /**
     * @return the omInclusionConceptSet
     */
    public ConceptSetExpression getOmInclusionConceptSet() {
        return omInclusionConceptSet;
    }

    /**
     * @return the outcomeCaption
     */
    public String getOutcomeCaption() {
        return outcomeCaption;
    }

    /**
     * @return the outcomeCohortDefinition
     */
    public String getOutcomeCohortDefinition() {
        return outcomeCohortDefinition;
    }

    /**
     * @return the psExclusionCaption
     */
    public String getPsExclusionCaption() {
        return psExclusionCaption;
    }

    /**
     * @return the psExclusionConceptSet
     */
    public ConceptSetExpression getPsExclusionConceptSet() {
        return psExclusionConceptSet;
    }

    /**
     * @return the psInclusionCaption
     */
    public String getPsInclusionCaption() {
        return psInclusionCaption;
    }

    /**
     * @return the psInclusionConceptSet
     */
    public ConceptSetExpression getPsInclusionConceptSet() {
        return psInclusionConceptSet;
    }

    /**
     * @return the treatmentCaption
     */
    public String getTreatmentCaption() {
        return treatmentCaption;
    }

    /**
     * @return the treatmentCohortDefinition
     */
    public String getTreatmentCohortDefinition() {
        return treatmentCohortDefinition;
    }

    /**
     * @param comparatorCaption the comparatorCaption to set
     */
    public void setComparatorCaption(String comparatorCaption) {
        this.comparatorCaption = comparatorCaption;
    }

    /**
     * @param comparatorCohortDefinition the comparatorCohortDefinition to set
     */
    public void setComparatorCohortDefinition(String comparatorCohortDefinition) {
        this.comparatorCohortDefinition = comparatorCohortDefinition;
    }

    /**
     * @param negativeControlCaption the negativeControlCaption to set
     */
    public void setNegativeControlCaption(String negativeControlCaption) {
        this.negativeControlCaption = negativeControlCaption;
    }

    /**
     * @param negativeControlConceptSet the negativeControlConceptSet to set
     */
    public void setNegativeControlConceptSet(ConceptSetExpression negativeControlConceptSet) {
        this.negativeControlConceptSet = negativeControlConceptSet;
    }

    /**
     * @param omExclusionCaption the omExclusionCaption to set
     */
    public void setOmExclusionCaption(String omExclusionCaption) {
        this.omExclusionCaption = omExclusionCaption;
    }

    /**
     * @param omExclusionConceptSet the omExclusionConceptSet to set
     */
    public void setOmExclusionConceptSet(ConceptSetExpression omExclusionConceptSet) {
        this.omExclusionConceptSet = omExclusionConceptSet;
    }

    /**
     * @param omInclusionCaption the omInclusionCaption to set
     */
    public void setOmInclusionCaption(String omInclusionCaption) {
        this.omInclusionCaption = omInclusionCaption;
    }

    /**
     * @param omInclusionConceptSet the omInclusionConceptSet to set
     */
    public void setOmInclusionConceptSet(ConceptSetExpression omInclusionConceptSet) {
        this.omInclusionConceptSet = omInclusionConceptSet;
    }

    /**
     * @param outcomeCaption the outcomeCaption to set
     */
    public void setOutcomeCaption(String outcomeCaption) {
        this.outcomeCaption = outcomeCaption;
    }

    /**
     * @param outcomeCohortDefinition the outcomeCohortDefinition to set
     */
    public void setOutcomeCohortDefinition(String outcomeCohortDefinition) {
        this.outcomeCohortDefinition = outcomeCohortDefinition;
    }

    /**
     * @param psExclusionCaption the psExclusionCaption to set
     */
    public void setPsExclusionCaption(String psExclusionCaption) {
        this.psExclusionCaption = psExclusionCaption;
    }

    /**
     * @param psExclusionConceptSet the psExclusionConceptSet to set
     */
    public void setPsExclusionConceptSet(ConceptSetExpression psExclusionConceptSet) {
        this.psExclusionConceptSet = psExclusionConceptSet;
    }

    /**
     * @param psInclusionCaption the psInclusionCaption to set
     */
    public void setPsInclusionCaption(String psInclusionCaption) {
        this.psInclusionCaption = psInclusionCaption;
    }

    /**
     * @param psInclusionConceptSet the psInclusionConceptSet to set
     */
    public void setPsInclusionConceptSet(ConceptSetExpression psInclusionConceptSet) {
        this.psInclusionConceptSet = psInclusionConceptSet;
    }

    /**
     * @param treatmentCaption the treatmentCaption to set
     */
    public void setTreatmentCaption(String treatmentCaption) {
        this.treatmentCaption = treatmentCaption;
    }

    /**
     * @param treatmentCohortDefinition the treatmentCohortDefinition to set
     */
    public void setTreatmentCohortDefinition(String treatmentCohortDefinition) {
        this.treatmentCohortDefinition = treatmentCohortDefinition;
    }

    /**
     * @return the negativeControlConceptSetSql
     */
    public String getNegativeControlConceptSetSql() {
        return negativeControlConceptSetSql;
    }

    /**
     * @return the omExclusionConceptSetSql
     */
    public String getOmExclusionConceptSetSql() {
        return omExclusionConceptSetSql;
    }

    /**
     * @return the omInclusionConceptSetSql
     */
    public String getOmInclusionConceptSetSql() {
        return omInclusionConceptSetSql;
    }

    /**
     * @return the psExclusionConceptSetSql
     */
    public String getPsExclusionConceptSetSql() {
        return psExclusionConceptSetSql;
    }

    /**
     * @return the psInclusionConceptSetSql
     */
    public String getPsInclusionConceptSetSql() {
        return psInclusionConceptSetSql;
    }

    /**
     * @param negativeControlConceptSetSql the negativeControlConceptSetSql to set
     */
    public void setNegativeControlConceptSetSql(String negativeControlConceptSetSql) {
        this.negativeControlConceptSetSql = negativeControlConceptSetSql;
    }

    /**
     * @param omExclusionConceptSetSql the omExclusionConceptSetSql to set
     */
    public void setOmExclusionConceptSetSql(String omExclusionConceptSetSql) {
        this.omExclusionConceptSetSql = omExclusionConceptSetSql;
    }

    /**
     * @param omInclusionConceptSetSql the omInclusionConceptSetSql to set
     */
    public void setOmInclusionConceptSetSql(String omInclusionConceptSetSql) {
        this.omInclusionConceptSetSql = omInclusionConceptSetSql;
    }

    /**
     * @param psExclusionConceptSetSql the psExclusionConceptSetSql to set
     */
    public void setPsExclusionConceptSetSql(String psExclusionConceptSetSql) {
        this.psExclusionConceptSetSql = psExclusionConceptSetSql;
    }

    /**
     * @param psInclusionConceptSetSql the psInclusionConceptSetSql to set
     */
    public void setPsInclusionConceptSetSql(String psInclusionConceptSetSql) {
        this.psInclusionConceptSetSql = psInclusionConceptSetSql;
    }
}
