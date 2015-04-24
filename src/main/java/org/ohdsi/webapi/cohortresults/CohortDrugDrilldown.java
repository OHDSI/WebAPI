package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortDrugDrilldown {
	private List<ConceptQuartileRecord> ageAtFirstExposure;
	private List<ConceptQuartileRecord> daysSupplyDistribution;
	private List<ConceptCountRecord> drugsByType;
	private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
	private List<PrevalenceRecord> prevalenceByMonth;
	private List<ConceptQuartileRecord> quantityDistribution;
	private List<ConceptQuartileRecord> refillsDistribution;
	
	/**
	 * @return the ageAtFirstExposure
	 */
	public List<ConceptQuartileRecord> getAgeAtFirstExposure() {
		return ageAtFirstExposure;
	}
	/**
	 * @param ageAtFirstExposure the ageAtFirstExposure to set
	 */
	public void setAgeAtFirstExposure(List<ConceptQuartileRecord> ageAtFirstExposure) {
		this.ageAtFirstExposure = ageAtFirstExposure;
	}
	/**
	 * @return the daysSupplyDistribution
	 */
	public List<ConceptQuartileRecord> getDaysSupplyDistribution() {
		return daysSupplyDistribution;
	}
	/**
	 * @param daysSupplyDistribution the daysSupplyDistribution to set
	 */
	public void setDaysSupplyDistribution(
			List<ConceptQuartileRecord> daysSupplyDistribution) {
		this.daysSupplyDistribution = daysSupplyDistribution;
	}
	/**
	 * @return the drugsByType
	 */
	public List<ConceptCountRecord> getDrugsByType() {
		return drugsByType;
	}
	/**
	 * @param drugsByType the drugsByType to set
	 */
	public void setDrugsByType(List<ConceptCountRecord> drugsByType) {
		this.drugsByType = drugsByType;
	}
	/**
	 * @return the prevalenceByGenderAgeYear
	 */
	public List<ConceptDecileRecord> getPrevalenceByGenderAgeYear() {
		return prevalenceByGenderAgeYear;
	}
	/**
	 * @param prevalenceByGenderAgeYear the prevalenceByGenderAgeYear to set
	 */
	public void setPrevalenceByGenderAgeYear(
			List<ConceptDecileRecord> prevalenceByGenderAgeYear) {
		this.prevalenceByGenderAgeYear = prevalenceByGenderAgeYear;
	}
	/**
	 * @return the prevalenceByMonth
	 */
	public List<PrevalenceRecord> getPrevalenceByMonth() {
		return prevalenceByMonth;
	}
	/**
	 * @param prevalenceByMonth the prevalenceByMonth to set
	 */
	public void setPrevalenceByMonth(List<PrevalenceRecord> prevalenceByMonth) {
		this.prevalenceByMonth = prevalenceByMonth;
	}
	/**
	 * @return the quantityDistribution
	 */
	public List<ConceptQuartileRecord> getQuantityDistribution() {
		return quantityDistribution;
	}
	/**
	 * @param quantityDistribution the quantityDistribution to set
	 */
	public void setQuantityDistribution(
			List<ConceptQuartileRecord> quantityDistribution) {
		this.quantityDistribution = quantityDistribution;
	}
	/**
	 * @return the refillsDistribution
	 */
	public List<ConceptQuartileRecord> getRefillsDistribution() {
		return refillsDistribution;
	}
	/**
	 * @param refillsDistribution the refillsDistribution to set
	 */
	public void setRefillsDistribution(
			List<ConceptQuartileRecord> refillsDistribution) {
		this.refillsDistribution = refillsDistribution;
	}
	
}
