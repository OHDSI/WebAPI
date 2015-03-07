package org.ohdsi.webapi.cohortresults;

import java.util.List;
import java.util.Map;

public class CohortDrugEraDrilldown {
	private List<Map<String, String>> ageAtFirstExposure;
	private List<Map<String, String>> lengthOfEra;
	private List<Map<String, String>> prevalenceByGenderAgeYear;
	private List<Map<String, String>> prevalenceByMonth;
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
	 * @return the lengthOfEra
	 */
	public List<Map<String, String>> getLengthOfEra() {
		return lengthOfEra;
	}
	/**
	 * @param lengthOfEra the lengthOfEra to set
	 */
	public void setLengthOfEra(List<Map<String, String>> lengthOfEra) {
		this.lengthOfEra = lengthOfEra;
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
	
	
}
