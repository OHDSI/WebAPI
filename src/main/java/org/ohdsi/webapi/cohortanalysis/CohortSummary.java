package org.ohdsi.webapi.cohortanalysis;

import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;

import java.util.List;

public class CohortSummary {

	private CohortDTO cohortDefinition;
	
	private String totalPatients;
	
	private String meanAge;
	
	private String meanObsPeriod;
	
	private List<?> genderDistribution;
	
	private List<?> ageDistribution;
	
	private List<CohortAnalysis> analyses;

	/**
	 * @return the definition
	 */
	public CohortDTO getCohortDefinition() {
		return cohortDefinition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setCohortDefinition(CohortDTO definition) {
		this.cohortDefinition = definition;
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
