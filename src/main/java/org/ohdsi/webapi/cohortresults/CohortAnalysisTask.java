package org.ohdsi.webapi.cohortresults;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohortAnalysisTask {

	@JsonProperty("SMALL_CELL_COUNT")
	private int smallCellCount;
	
	@JsonProperty("RUN_HERACLES_HEEL")
	private boolean runHeraclesHeel;
	
	@JsonProperty("COHORT_DEFINITION_IDS")
	List<String> cohortDefinitionId;
	
	@JsonProperty("ANALYSIS_IDS")
	List<String> analysisId;
	
	@JsonProperty("CONDITION_CONCEPT_IDS")
	List<String> conditionConceptIds;
	
	@JsonProperty("DRUG_CONCEPT_IDS")
	List<String> drugConceptIds;
	
	@JsonProperty("PROCEDURE_CONCEPT_IDS")
	List<String> procedureConceptIds;
	
	@JsonProperty("OBSERVATION_CONCEPT_IDS")
	List<String> observationConceptIds;
	
	@JsonProperty("MEASUREMENT_CONCEPT_IDS")
	List<String> measurementConceptIds;

	/**
	 * @return the smallCellCount
	 */
	public int getSmallCellCount() {
		return smallCellCount;
	}

	/**
	 * @param smallCellCount the smallCellCount to set
	 */
	public void setSmallCellCount(int smallCellCount) {
		this.smallCellCount = smallCellCount;
	}

	/**
	 * @return the runHeraclesHeel
	 */
	public boolean runHeraclesHeel() {
		return runHeraclesHeel;
	}

	/**
	 * @param runHeraclesHeel the runHeraclesHeel to set
	 */
	public void setRunHeraclesHeel(boolean runHeraclesHeel) {
		this.runHeraclesHeel = runHeraclesHeel;
	}

	/**
	 * @return the cohortDefinitionId
	 */
	public List<String> getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	/**
	 * @param cohortDefinitionId the cohortDefinitionId to set
	 */
	public void setCohortDefinitionId(List<String> cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	/**
	 * @return the analysisId
	 */
	public List<String> getAnalysisId() {
		return analysisId;
	}

	/**
	 * @param analysisId the analysisId to set
	 */
	public void setAnalysisId(List<String> analysisId) {
		this.analysisId = analysisId;
	}

	/**
	 * @return the conditionConceptIds
	 */
	public List<String> getConditionConceptIds() {
		return conditionConceptIds;
	}

	/**
	 * @param conditionConceptIds the conditionConceptIds to set
	 */
	public void setConditionConceptIds(List<String> conditionConceptIds) {
		this.conditionConceptIds = conditionConceptIds;
	}

	/**
	 * @return the drugConceptIds
	 */
	public List<String> getDrugConceptIds() {
		return drugConceptIds;
	}

	/**
	 * @param drugConceptIds the drugConceptIds to set
	 */
	public void setDrugConceptIds(List<String> drugConceptIds) {
		this.drugConceptIds = drugConceptIds;
	}

	/**
	 * @return the procedureConceptIds
	 */
	public List<String> getProcedureConceptIds() {
		return procedureConceptIds;
	}

	/**
	 * @param procedureConceptIds the procedureConceptIds to set
	 */
	public void setProcedureConceptIds(List<String> procedureConceptIds) {
		this.procedureConceptIds = procedureConceptIds;
	}

	/**
	 * @return the observationConceptIds
	 */
	public List<String> getObservationConceptIds() {
		return observationConceptIds;
	}

	/**
	 * @param observationConceptIds the observationConceptIds to set
	 */
	public void setObservationConceptIds(List<String> observationConceptIds) {
		this.observationConceptIds = observationConceptIds;
	}

	/**
	 * @return the measurementConceptIds
	 */
	public List<String> getMeasurementConceptIds() {
		return measurementConceptIds;
	}

	/**
	 * @param measurementConceptIds the measurementConceptIds to set
	 */
	public void setMeasurementConceptIds(List<String> measurementConceptIds) {
		this.measurementConceptIds = measurementConceptIds;
	}
}
 