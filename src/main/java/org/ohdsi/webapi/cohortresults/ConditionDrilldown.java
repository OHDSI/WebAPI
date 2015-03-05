package org.ohdsi.webapi.cohortresults;

import java.util.List;
import java.util.Map;

public class ConditionDrilldown {
	private List<Map<String, String>> ageAtFirstDiagnosis;
	
	private List<Map<String, String>> conditionsByType;
	
	private List<Map<String, String>> prevalenceByGenderAgeYear;
	
	private List<Map<String, String>> prevalenceByMonth;

	/**
	 * @return the ageAtFirstDiagnosis
	 */
	public List<Map<String, String>> getAgeAtFirstDiagnosis() {
		return ageAtFirstDiagnosis;
	}

	/**
	 * @param ageAtFirstDiagnosis the ageAtFirstDiagnosis to set
	 */
	public void setAgeAtFirstDiagnosis(List<Map<String, String>> ageAtFirstDiagnosis) {
		this.ageAtFirstDiagnosis = ageAtFirstDiagnosis;
	}

	/**
	 * @return the conditionsByType
	 */
	public List<Map<String, String>> getConditionsByType() {
		return conditionsByType;
	}

	/**
	 * @param conditionsByType the conditionsByType to set
	 */
	public void setConditionsByType(List<Map<String, String>> conditionsByType) {
		this.conditionsByType = conditionsByType;
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
