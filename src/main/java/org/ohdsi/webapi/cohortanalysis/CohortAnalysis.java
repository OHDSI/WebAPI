package org.ohdsi.webapi.cohortanalysis;

import org.ohdsi.webapi.model.results.Analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohortAnalysis extends Analysis {

	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String ANALYSIS_COMPLETE = "ANALYSIS_COMPLETE";
	
	@JsonProperty(COHORT_DEFINITION_ID)
	private int cohortDefinitionId;
	
	@JsonProperty(ANALYSIS_COMPLETE)
	private boolean analysisComplete;

	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public boolean isAnalysisComplete() {
		return analysisComplete;
	}

	public void setAnalysisComplete(boolean analysisComplete) {
		this.analysisComplete = analysisComplete;
	}
	
}
