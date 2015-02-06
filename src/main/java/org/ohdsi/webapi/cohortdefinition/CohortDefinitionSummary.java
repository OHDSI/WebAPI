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
}
