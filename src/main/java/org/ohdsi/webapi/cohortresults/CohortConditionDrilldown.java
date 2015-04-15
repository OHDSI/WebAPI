package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortConditionDrilldown {
	private List<ConceptQuartileRecord> ageAtFirstDiagnosis;
	
	private List<ConceptCountRecord> conditionsByType;
	
	private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
	
	private List<PrevalenceRecord> prevalenceByMonth;

	/**
	 * @return the ageAtFirstDiagnosis
	 */
	public List<ConceptQuartileRecord> getAgeAtFirstDiagnosis() {
		return ageAtFirstDiagnosis;
	}

	/**
	 * @param ageAtFirstDiagnosis the ageAtFirstDiagnosis to set
	 */
	public void setAgeAtFirstDiagnosis(List<ConceptQuartileRecord> ageAtFirstDiagnosis) {
		this.ageAtFirstDiagnosis = ageAtFirstDiagnosis;
	}

	/**
	 * @return the conditionsByType
	 */
	public List<ConceptCountRecord> getConditionsByType() {
		return conditionsByType;
	}

	/**
	 * @param conditionsByType the conditionsByType to set
	 */
	public void setConditionsByType(List<ConceptCountRecord> conditionsByType) {
		this.conditionsByType = conditionsByType;
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
