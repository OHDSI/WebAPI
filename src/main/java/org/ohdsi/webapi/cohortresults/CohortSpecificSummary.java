package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortSpecificSummary {
	private List<ObservationPeriodRecord> personsByDurationFromStartToEnd;
	private List<PrevalenceRecord> prevalenceByMonth;
	private List<ConceptDecileRecord> numPersonsByCohortStartByGenderByAge;
	private List<ConceptQuartileRecord> ageAtIndexDistribution;
	private List<MonthObservationRecord> personsInCohortFromCohortStartToEnd;

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

	/**
	 * @return the ageAtIndexDistribution
	 */
	public List<ConceptQuartileRecord> getAgeAtIndexDistribution() {
		return ageAtIndexDistribution;
	}

	/**
	 * @param ageAtIndexDistribution the ageAtIndexDistribution to set
	 */
	public void setAgeAtIndexDistribution(
			List<ConceptQuartileRecord> ageAtIndexDistribution) {
		this.ageAtIndexDistribution = ageAtIndexDistribution;
	}

	/**
	 * @return the personsInCohortFromCohortStartToEnd
	 */
	public List<MonthObservationRecord> getPersonsInCohortFromCohortStartToEnd() {
		return personsInCohortFromCohortStartToEnd;
	}

	/**
	 * @param personsInCohortFromCohortStartToEnd the personsInCohortFromCohortStartToEnd to set
	 */
	public void setPersonsInCohortFromCohortStartToEnd(
			List<MonthObservationRecord> personsInCohortFromCohortStartToEnd) {
		this.personsInCohortFromCohortStartToEnd = personsInCohortFromCohortStartToEnd;
	}
}
