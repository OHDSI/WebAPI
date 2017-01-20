package org.ohdsi.webapi.report;

import java.util.List;

public class CohortConditionEraDrilldown {

	private List<ConceptQuartileRecord> ageAtFirstDiagnosis;
	private List<ConceptQuartileRecord> lengthOfEra;
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
