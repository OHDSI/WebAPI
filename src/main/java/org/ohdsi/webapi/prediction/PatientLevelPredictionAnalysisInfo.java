/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.prediction;

import java.io.Serializable;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 *
 * @author asena5
 */
public class PatientLevelPredictionAnalysisInfo extends PatientLevelPredictionAnalysis implements Serializable {
		private String treatmentCaption;
		private String treatmentCohortDefinition;
		private String outcomeCaption;
		private String outcomeCohortDefinition;
		private String cvExclusionCaption;
		private ConceptSetExpression cvExclusionConceptSet;
		private String cvExclusionConceptSetSql;
		private String cvInclusionCaption;
		private ConceptSetExpression cvInclusionConceptSet;
		private String cvInclusionConceptSetSql;
		
		public PatientLevelPredictionAnalysisInfo(PatientLevelPredictionAnalysis analysis) {
			this.setAddExposureDaysToEnd(analysis.getAddExposureDaysToEnd());
			this.setAnalysisId(analysis.getAnalysisId());
			this.setCreatedDate(analysis.getCreatedDate());
			this.setCreatedBy(analysis.getCreatedBy());
			this.setCvConceptCounts(analysis.getCvConceptCounts());
			this.setCvConditionEra(analysis.getCvConditionEra());
			this.setCvConditionEraEver(analysis.getCvConditionEraEver());
			this.setCvConditionEraOverlap(analysis.getCvConditionEraOverlap());
			this.setCvConditionGroup(analysis.getCvConditionGroup());
			this.setCvConditionGroupMeddra(analysis.getCvConditionGroupMeddra());
			this.setCvConditionGroupSnomed(analysis.getCvConditionGroupSnomed());
			this.setCvConditionOcc(analysis.getCvConditionOcc());
			this.setCvConditionOcc30d(analysis.getCvConditionOcc30d());
			this.setCvConditionOcc365d(analysis.getCvConditionOcc365d());
			this.setCvConditionOccInpt180d(analysis.getCvConditionOccInpt180d());
			this.setCvDemographics(analysis.getCvDemographics());
			this.setCvDemographicsAge(analysis.getCvDemographicsAge());
			this.setCvDemographicsEthnicity(analysis.getCvDemographicsEthnicity());
			this.setCvDemographicsGender(analysis.getCvDemographicsGender());
			this.setCvDemographicsMonth(analysis.getCvDemographicsMonth());
			this.setCvDemographicsRace(analysis.getCvDemographicsRace());
			this.setCvDemographicsYear(analysis.getCvDemographicsYear());
			this.setCvDrugEra(analysis.getCvDrugEra());
			this.setCvDrugEra30d(analysis.getCvDrugEra30d());
			this.setCvDrugEra365d(analysis.getCvDrugEra365d());
			this.setCvDrugEraEver(analysis.getCvDrugEraEver());
			this.setCvDrugEraOverlap(analysis.getCvDrugEraOverlap());
			this.setCvDrugExposure(analysis.getCvDrugExposure());
			this.setCvDrugExposure30d(analysis.getCvDrugExposure30d());
			this.setCvDrugExposure365d(analysis.getCvDrugExposure365d());
			this.setCvDrugGroup(analysis.getCvDrugGroup());
			this.setCvExclusionId(analysis.getCvExclusionId());
			this.setCvInclusionId(analysis.getCvInclusionId());
			this.setCvInteractionMonth(analysis.getCvInteractionMonth());
			this.setCvInteractionYear(analysis.getCvInteractionYear());
			this.setCvMeasurement(analysis.getCvMeasurement());
			this.setCvMeasurement30d(analysis.getCvMeasurement30d());
			this.setCvMeasurement365d(analysis.getCvMeasurement365d());
			this.setCvMeasurementAbove(analysis.getCvMeasurementAbove());
			this.setCvMeasurementBelow(analysis.getCvMeasurementBelow());
			this.setCvMeasurementCount365d(analysis.getCvMeasurementCount365d());
			this.setCvObservation(analysis.getCvObservation());
			this.setCvObservation30d(analysis.getCvObservation30d());
			this.setCvObservation365d(analysis.getCvObservation365d());
			this.setCvObservationCount365d(analysis.getCvObservationCount365d());
			this.setCvProcedureGroup(analysis.getCvProcedureGroup());
			this.setCvProcedureOcc(analysis.getCvProcedureOcc());
			this.setCvProcedureOcc30d(analysis.getCvProcedureOcc30d());
			this.setCvProcedureOcc365d(analysis.getCvProcedureOcc365d());
			this.setCvRiskScores(analysis.getCvRiskScores());
			this.setCvRiskScoresChads2(analysis.getCvRiskScoresChads2());
			this.setCvRiskScoresChads2vasc(analysis.getCvRiskScoresChads2vasc());
			this.setCvRiskScoresCharlson(analysis.getCvRiskScoresCharlson());
			this.setCvRiskScoresDcsi(analysis.getCvRiskScoresDcsi());
			this.setDelCovariatesSmallCount(analysis.getDelCovariatesSmallCount());
			this.setFirstExposureOnly(analysis.getFirstExposureOnly());
			this.setIncludeAllOutcomes(analysis.getIncludeAllOutcomes());
			this.setMinimumDaysAtRisk(analysis.getMinimumDaysAtRisk());
			this.setMinimumTimeAtRisk(analysis.getMinimumTimeAtRisk());
			this.setMinimumWashoutPeriod(analysis.getMinimumWashoutPeriod());
			this.setMoAlpha(analysis.getMoAlpha());
			this.setMoClassWeight(analysis.getMoClassWeight());
			this.setMoIndexFolder(analysis.getMoIndexFolder());
			this.setMoK(analysis.getMoK());
			this.setMoLearnRate(analysis.getMoLearnRate());
			this.setMoLearningRate(analysis.getMoLearningRate());
			this.setMoMTries(analysis.getMoMTries());
			this.setMoMaxDepth(analysis.getMoMaxDepth());
			this.setMoMinImpuritySplit(analysis.getMoMinImpuritySplit());
			this.setMoMinRows(analysis.getMoMinRows());
			this.setMoMinSamplesLeaf(analysis.getMoMinSamplesLeaf());
			this.setMoMinSamplesSplit(analysis.getMoMinSamplesSplit());
			this.setMoNEstimators(analysis.getMoNEstimators());
			this.setMoNThread(analysis.getMoNThread());
			this.setMoNTrees(analysis.getMoNTrees());
			this.setMoPlot(analysis.getMoPlot());
			this.setMoSeed(analysis.getMoSeed());
			this.setMoSize(analysis.getMoSize());
			this.setMoVarImp(analysis.getMoVarImp());
			this.setMoVariance(analysis.getMoVariance());
			this.setModelType(analysis.getModelType());
			this.setModifiedDate(analysis.getModifiedDate());
			this.setModifiedBy(analysis.getModifiedBy());
			this.setName(analysis.getName());
			this.setOutcomeId(analysis.getOutcomeId());
			this.setPriorOutcomeLookback(analysis.getPriorOutcomeLookback());
			this.setRequireTimeAtRisk(analysis.getRequireTimeAtRisk());
			this.setRmPriorOutcomes(analysis.getRmPriorOutcomes());
			this.setSample(analysis.getSample());
			this.setSampleSize(analysis.getSampleSize());
			this.setTestFraction(analysis.getTestFraction());
			this.setTestSplit(analysis.getTestSplit());
			this.setTimeAtRiskEnd(analysis.getTimeAtRiskEnd());
			this.setTimeAtRiskStart(analysis.getTimeAtRiskStart());
			this.setTreatmentId(analysis.getTreatmentId());
			this.setnFold(this.getnFold());
		}

	/**
	 * @return the treatmentCaption
	 */
	public String getTreatmentCaption() {
		return treatmentCaption;
	}

	/**
	 * @param treatmentCaption the treatmentCaption to set
	 */
	public void setTreatmentCaption(String treatmentCaption) {
		this.treatmentCaption = treatmentCaption;
	}

	/**
	 * @return the treatmentCohortDefinition
	 */
	public String getTreatmentCohortDefinition() {
		return treatmentCohortDefinition;
	}

	/**
	 * @param treatmentCohortDefinition the treatmentCohortDefinition to set
	 */
	public void setTreatmentCohortDefinition(String treatmentCohortDefinition) {
		this.treatmentCohortDefinition = treatmentCohortDefinition;
	}

	/**
	 * @return the outcomeCaption
	 */
	public String getOutcomeCaption() {
		return outcomeCaption;
	}

	/**
	 * @param outcomeCaption the outcomeCaption to set
	 */
	public void setOutcomeCaption(String outcomeCaption) {
		this.outcomeCaption = outcomeCaption;
	}

	/**
	 * @return the outcomeCohortDefinition
	 */
	public String getOutcomeCohortDefinition() {
		return outcomeCohortDefinition;
	}

	/**
	 * @param outcomeCohortDefinition the outcomeCohortDefinition to set
	 */
	public void setOutcomeCohortDefinition(String outcomeCohortDefinition) {
		this.outcomeCohortDefinition = outcomeCohortDefinition;
	}

	/**
	 * @return the cvExclusionCaption
	 */
	public String getCvExclusionCaption() {
		return cvExclusionCaption;
	}

	/**
	 * @param cvExclusionCaption the cvExclusionCaption to set
	 */
	public void setCvExclusionCaption(String cvExclusionCaption) {
		this.cvExclusionCaption = cvExclusionCaption;
	}

	/**
	 * @return the cvExclusionConceptSet
	 */
	public ConceptSetExpression getCvExclusionConceptSet() {
		return cvExclusionConceptSet;
	}

	/**
	 * @param cvExclusionConceptSet the cvExclusionConceptSet to set
	 */
	public void setCvExclusionConceptSet(ConceptSetExpression cvExclusionConceptSet) {
		this.cvExclusionConceptSet = cvExclusionConceptSet;
	}

	/**
	 * @return the cvExclusionConceptSetSql
	 */
	public String getCvExclusionConceptSetSql() {
		return cvExclusionConceptSetSql;
	}

	/**
	 * @param cvExclusionConceptSetSql the cvExclusionConceptSetSql to set
	 */
	public void setCvExclusionConceptSetSql(String cvExclusionConceptSetSql) {
		this.cvExclusionConceptSetSql = cvExclusionConceptSetSql;
	}

	/**
	 * @return the cvInclusionCaption
	 */
	public String getCvInclusionCaption() {
		return cvInclusionCaption;
	}

	/**
	 * @param cvInclusionCaption the cvInclusionCaption to set
	 */
	public void setCvInclusionCaption(String cvInclusionCaption) {
		this.cvInclusionCaption = cvInclusionCaption;
	}

	/**
	 * @return the cvInclusionConceptSet
	 */
	public ConceptSetExpression getCvInclusionConceptSet() {
		return cvInclusionConceptSet;
	}

	/**
	 * @param cvInclusionConceptSet the cvInclusionConceptSet to set
	 */
	public void setCvInclusionConceptSet(ConceptSetExpression cvInclusionConceptSet) {
		this.cvInclusionConceptSet = cvInclusionConceptSet;
	}

	/**
	 * @return the cvInclusionConceptSetSql
	 */
	public String getCvInclusionConceptSetSql() {
		return cvInclusionConceptSetSql;
	}

	/**
	 * @param cvInclusionConceptSetSql the cvInclusionConceptSetSql to set
	 */
	public void setCvInclusionConceptSetSql(String cvInclusionConceptSetSql) {
		this.cvInclusionConceptSetSql = cvInclusionConceptSetSql;
	}
	
}
