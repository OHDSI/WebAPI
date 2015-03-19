package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortSpecificSummary {
	private List<ObservationPeriodRecord> personsByDurationFromStartToEnd;
	private List<PrevalenceRecord> prevalenceByMonth;
	private List<ConceptDecileRecord> numPersonsByCohortStartByGenderByAge;

	/**
	 * @return the numPersonsByCohortStartByGenderByAge
	 */
	public List<ConceptDecileRecord> getNumPersonsByCohortStartByGenderByAge() {
		return numPersonsByCohortStartByGenderByAge;
	}

	/**
	 * @param numPersonsByCohortStartByGenderByAge the numPersonsByCohortStartByGenderByAge to set
	 */
	public void setNumPersonsByCohortStartByGenderByAge(
			List<ConceptDecileRecord> numPersonsByCohortStartByGenderByAge) {
		this.numPersonsByCohortStartByGenderByAge = numPersonsByCohortStartByGenderByAge;
	}

	/**
	 * @return the personsByDurationFromStartToEnd
	 */
	public List<ObservationPeriodRecord> getPersonsByDurationFromStartToEnd() {
		return personsByDurationFromStartToEnd;
	}

	/**
	 * @param personsByDurationFromStartToEnd the personsByDurationFromStartToEnd to set
	 */
	public void setPersonsByDurationFromStartToEnd(
			List<ObservationPeriodRecord> personsByDurationFromStartToEnd) {
		this.personsByDurationFromStartToEnd = personsByDurationFromStartToEnd;
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
