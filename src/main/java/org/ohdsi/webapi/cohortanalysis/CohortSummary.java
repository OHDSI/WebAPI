package org.ohdsi.webapi.cohortanalysis;

import java.util.List;

import org.ohdsi.webapi.model.CohortDefinition;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CohortSummary {

	@JsonProperty("COHORT_DEFINITION")
	private CohortDefinition definition;
	
	@JsonProperty("TOTAL_PATIENTS")
	private String totalPatients;
	
	@JsonProperty("MEAN_AGE")
	private String meanAge;
	
	@JsonProperty("MEAN_OBS_PERIOD")
	private String meanObsPeriod;
	
	@JsonProperty("GENDER_DISTRIBUTION")
	private List<?> genderDistribution;
	
	@JsonProperty("AGE_DISTRIBUTION")
	private List<?> ageDistribution;
	
	@JsonProperty("ANALYSES")
	private List<CohortAnalysis> analyses;

	/**
	 * @return the definition
	 */
	public CohortDefinition getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(CohortDefinition definition) {
		this.definition = definition;
	}

	/**
	 * @return the totalPatients
	 */
	public String getTotalPatients() {
		return totalPatients;
	}

	/**
	 * @param totalPatients the totalPatients to set
	 */
	public void setTotalPatients(String totalPatients) {
		this.totalPatients = totalPatients;
	}

	/**
	 * @return the meanAge
	 */
	public String getMeanAge() {
		return meanAge;
	}

	/**
	 * @param meanAge the meanAge to set
	 */
	public void setMeanAge(String meanAge) {
		this.meanAge = meanAge;
	}

	/**
	 * @return the meanObsPeriod
	 */
	public String getMeanObsPeriod() {
		return meanObsPeriod;
	}

	/**
	 * @param meanObsPeriod the meanObsPeriod to set
	 */
	public void setMeanObsPeriod(String meanObsPeriod) {
		this.meanObsPeriod = meanObsPeriod;
	}

	/**
	 * @return the genderDistribution
	 */
	public List<?> getGenderDistribution() {
		return genderDistribution;
	}

	/**
	 * @param genderDistribution the genderDistribution to set
	 */
	public void setGenderDistribution(List<?> genderDistribution) {
		this.genderDistribution = genderDistribution;
	}

	/**
	 * @return the ageDistribution
	 */
	public List<?> getAgeDistribution() {
		return ageDistribution;
	}

	/**
	 * @param ageDistribution the ageDistribution to set
	 */
	public void setAgeDistribution(List<?> ageDistribution) {
		this.ageDistribution = ageDistribution;
	}

	/**
	 * @return the analyses
	 */
	public List<CohortAnalysis> getAnalyses() {
		return analyses;
	}

	/**
	 * @param analyses the analyses to set
	 */
	public void setAnalyses(List<CohortAnalysis> analyses) {
		this.analyses = analyses;
	}
}
