package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortSpecificSummary {
	private List<ObservationPeriodRecord> personsByDurationFromStartToEnd;

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
}
