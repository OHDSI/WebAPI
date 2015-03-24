package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortVisitsDrilldown {
	private List<ConceptQuartileRecord> ageAtFirstOccurrence;
	
	private List<ConceptQuartileRecord> visitDurationByType;
	
	private List<ConceptDecileRecord> prevalenceByGenderAgeYear;
	
	private List<PrevalenceRecord> prevalenceByMonth;

	/**
	 * @return the ageAtFirstOccurrence
	 */
	public List<ConceptQuartileRecord> getAgeAtFirstOccurrence() {
		return ageAtFirstOccurrence;
	}

	/**
	 * @param ageAtFirstOccurrence the ageAtFirstOccurrence to set
	 */
	public void setAgeAtFirstOccurrence(
			List<ConceptQuartileRecord> ageAtFirstOccurrence) {
		this.ageAtFirstOccurrence = ageAtFirstOccurrence;
	}

	/**
	 * @return the visitDurationByType
	 */
	public List<ConceptQuartileRecord> getVisitDurationByType() {
		return visitDurationByType;
	}

	/**
	 * @param visitDurationByType the visitDurationByType to set
	 */
	public void setVisitDurationByType(List<ConceptQuartileRecord> visitDurationByType) {
		this.visitDurationByType = visitDurationByType;
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
