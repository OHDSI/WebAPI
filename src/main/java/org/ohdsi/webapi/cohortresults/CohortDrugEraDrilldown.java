package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortDrugEraDrilldown {
	private List<ConceptQuartileRecord> ageAtFirstExposure;
	private List<ConceptQuartileRecord> lengthOfEra;
	private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
	private List<PrevalenceRecord> prevalenceByMonth;
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
	 * @return the lengthOfEra
	 */
	public List<ConceptQuartileRecord> getLengthOfEra() {
		return lengthOfEra;
	}
	/**
	 * @param lengthOfEra the lengthOfEra to set
	 */
	public void setLengthOfEra(List<ConceptQuartileRecord> lengthOfEra) {
		this.lengthOfEra = lengthOfEra;
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
	
	
}
