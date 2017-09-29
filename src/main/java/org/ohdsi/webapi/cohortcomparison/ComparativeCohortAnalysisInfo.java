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
import java.util.ArrayList;

/**
 * @author Frank DeFalco <fdefalco@ohdsi.org>
 */
public class ComparativeCohortAnalysisInfo extends ComparativeCohortAnalysis implements Serializable {
  
	public ArrayList<CohortInfo> cohortInfo = new ArrayList<CohortInfo>();
	public ArrayList<ConceptSetInfo> conceptSetInfo = new ArrayList<ConceptSetInfo>();
	
  public ComparativeCohortAnalysisInfo(ComparativeCohortAnalysis analysis) {
      this.setAnalysisId(analysis.getAnalysisId());
			this.setAnalysisList(analysis.getAnalysisList());
			this.setCreatedBy(analysis.getCreatedBy());
      this.setCreatedDate(analysis.getCreatedDate());
      this.setName(analysis.getName());
			this.setModifiedBy(analysis.getModifiedBy());
      this.setModifiedDate(analysis.getModifiedDate());
			this.setOutcomeList(analysis.getOutcomeList());
			this.setTargetComparatorList(analysis.getTargetComparatorList());
			/*
      this.setAddExposureDaysToEnd(analysis.getAddExposureDaysToEnd());
      this.setDelCovariatesSmallCount(analysis.getDelCovariatesSmallCount());
      this.setMinimumDaysAtRisk(analysis.getMinimumDaysAtRisk());
      this.setMinimumWashoutPeriod(analysis.getMinimumWashoutPeriod());
      this.setModelType(analysis.getModelType());
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
      this.setUserId(analysis.getUserId());
			*/
  }

}
