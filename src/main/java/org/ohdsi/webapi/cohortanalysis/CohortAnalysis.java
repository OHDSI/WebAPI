package org.ohdsi.webapi.cohortanalysis;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.ohdsi.webapi.model.results.Analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohortAnalysis extends Analysis {

	public static final String COHORT_DEFINITION_ID = "COHORT_DEFINITION_ID";
	public static final String ANALYSIS_COMPLETE = "ANALYSIS_COMPLETE";
	public static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";
	public static final String LAST_UPDATE_TIME_FORMATTED = "LAST_UPDATE_TIME_FORMATTED";
	
	private int cohortDefinitionId;
	
	private boolean analysisComplete;
	
	private Timestamp lastUpdateTime;
	
	private String lastUpdateTimeFormatted;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm aaa");

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

	/**
	 * @return the lastUpdateTime
	 */
	public Timestamp getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * @param lastUpdateTime the lastUpdateTime to set
	 */
	public void setLastUpdateTime(Timestamp lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * @return the lastUpdateTimeFormatted
	 */
	public String getLastUpdateTimeFormatted() {
		if (this.lastUpdateTime != null) {
			this.lastUpdateTimeFormatted = formatter.format(lastUpdateTime);
		}
		return lastUpdateTimeFormatted;
	}

	
}
