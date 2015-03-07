package org.ohdsi.webapi.cohortresults;

import java.util.List;
import java.util.Map;

public class CohortDrugDrilldown {
	private List<Map<String, String>> ageAtFirstExposure;
	private List<Map<String, String>> daysSupplyDistribution;
	private List<Map<String, String>> drugsByType;
	private List<Map<String, String>> prevalenceByGenderAgeYear;
	private List<Map<String, String>> prevalenceByMonth;
	private List<Map<String, String>> quantityDistribution;
	private List<Map<String, String>> refillsDistribution;
	
	/**
	 * @return the ageAtFirstExposure
	 */
	public List<Map<String, String>> getAgeAtFirstExposure() {
		return ageAtFirstExposure;
	}
	/**
	 * @param ageAtFirstExposure the ageAtFirstExposure to set
	 */
	public void setAgeAtFirstExposure(List<Map<String, String>> ageAtFirstExposure) {
		this.ageAtFirstExposure = ageAtFirstExposure;
	}
	/**
	 * @return the daysSupplyDistribution
	 */
	public List<Map<String, String>> getDaysSupplyDistribution() {
		return daysSupplyDistribution;
	}
	/**
	 * @param daysSupplyDistribution the daysSupplyDistribution to set
	 */
	public void setDaysSupplyDistribution(
			List<Map<String, String>> daysSupplyDistribution) {
		this.daysSupplyDistribution = daysSupplyDistribution;
	}
	/**
	 * @return the drugsByType
	 */
	public List<Map<String, String>> getDrugsByType() {
		return drugsByType;
	}
	/**
	 * @param drugsByType the drugsByType to set
	 */
	public void setDrugsByType(List<Map<String, String>> drugsByType) {
		this.drugsByType = drugsByType;
	}
	/**
	 * @return the prevalenceByGenderAgeYear
	 */
	public List<Map<String, String>> getPrevalenceByGenderAgeYear() {
		return prevalenceByGenderAgeYear;
	}
	/**
	 * @param prevalenceByGenderAgeYear the prevalenceByGenderAgeYear to set
	 */
	public void setPrevalenceByGenderAgeYear(
			List<Map<String, String>> prevalenceByGenderAgeYear) {
		this.prevalenceByGenderAgeYear = prevalenceByGenderAgeYear;
	}
	/**
	 * @return the prevalenceByMonth
	 */
	public List<Map<String, String>> getPrevalenceByMonth() {
		return prevalenceByMonth;
	}
	/**
	 * @param prevalenceByMonth the prevalenceByMonth to set
	 */
	public void setPrevalenceByMonth(List<Map<String, String>> prevalenceByMonth) {
		this.prevalenceByMonth = prevalenceByMonth;
	}
	/**
	 * @return the quantityDistribution
	 */
	public List<Map<String, String>> getQuantityDistribution() {
		return quantityDistribution;
	}
	/**
	 * @param quantityDistribution the quantityDistribution to set
	 */
	public void setQuantityDistribution(
			List<Map<String, String>> quantityDistribution) {
		this.quantityDistribution = quantityDistribution;
	}
	/**
	 * @return the refillsDistribution
	 */
	public List<Map<String, String>> getRefillsDistribution() {
		return refillsDistribution;
	}
	/**
	 * @param refillsDistribution the refillsDistribution to set
	 */
	public void setRefillsDistribution(List<Map<String, String>> refillsDistribution) {
		this.refillsDistribution = refillsDistribution;
	}
	
}
