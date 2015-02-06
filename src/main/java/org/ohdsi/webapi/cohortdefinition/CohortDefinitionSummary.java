package org.ohdsi.webapi.cohortdefinition;

import java.util.List;

import org.ohdsi.webapi.cohortresults.CohortAnalysis;
import org.ohdsi.webapi.model.CohortDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohortDefinitionSummary {

	@JsonProperty("COHORT_DEFINITION")
	private CohortDefinition cohortDefinition;
	
	@JsonProperty("COHORT_ANALYSES")
	private List<CohortAnalysis> cohortAnalyses;
	
	@JsonProperty("GENDER_SUMMARY_DATA")
	private List<?> genderSummaryData;
	
	@JsonProperty("AGE_SUMMARY_DATA")
	private List<?> ageSummaryData;

	/**
	 * @return the cohortDefintion
	 */
	public CohortDefinition getCohortDefinition() {
		return cohortDefinition;
	}

	/**
	 * @param cohortDefintion the cohortDefintion to set
	 */
	public void setCohortDefinition(CohortDefinition cohortDefinition) {
		this.cohortDefinition = cohortDefinition;
	}

	/**
	 * @return the cohortAnalyses
	 */
	public List<CohortAnalysis> getCohortAnalyses() {
		return cohortAnalyses;
	}

	/**
	 * @param cohortAnalyses the cohortAnalyses to set
	 */
	public void setCohortAnalyses(List<CohortAnalysis> cohortAnalyses) {
		this.cohortAnalyses = cohortAnalyses;
	}

	/**
	 * @return the genderData
	 */
	public List<?> getGenderSummaryData() {
		return genderSummaryData;
	}

	/**
	 * @param genderData the genderData to set
	 */
	public void setGenderData(List<?> genderSummaryData) {
		this.genderSummaryData = genderSummaryData;
	}

	/**
	 * @return the ageSummaryData
	 */
	public List<?> getAgeSummaryData() {
		return ageSummaryData;
	}

	/**
	 * @param ageSummaryData the ageSummaryData to set
	 */
	public void setAgeSummaryData(List<?> ageSummaryData) {
		this.ageSummaryData = ageSummaryData;
	}
}
