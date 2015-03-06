package org.ohdsi.webapi.cohortresults;

import java.util.List;
import java.util.Map;

public class CohortDashboard {

	private List<Map<String, String>> gender;
	private List<Map<String, String>> ageAtFirstObservation;
	private List<Map<String, String>> cumulativeObservation;
	private List<Map<String, String>> observedByMonth;
	
	/**
	 * @return the gender
	 */
	public List<Map<String, String>> getGender() {
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(List<Map<String, String>> gender) {
		this.gender = gender;
	}
	/**
	 * @return the ageAtFirstObservation
	 */
	public List<Map<String, String>> getAgeAtFirstObservation() {
		return ageAtFirstObservation;
	}
	/**
	 * @param ageAtFirstObservation the ageAtFirstObservation to set
	 */
	public void setAgeAtFirstObservation(
			List<Map<String, String>> ageAtFirstObservation) {
		this.ageAtFirstObservation = ageAtFirstObservation;
	}
	/**
	 * @return the cumulativeObservation
	 */
	public List<Map<String, String>> getCumulativeObservation() {
		return cumulativeObservation;
	}
	/**
	 * @param cumulativeObservation the cumulativeObservation to set
	 */
	public void setCumulativeObservation(
			List<Map<String, String>> cumulativeObservation) {
		this.cumulativeObservation = cumulativeObservation;
	}
	/**
	 * @return the observedByMonth
	 */
	public List<Map<String, String>> getObservedByMonth() {
		return observedByMonth;
	}
	/**
	 * @param observedByMonth the observedByMonth to set
	 */
	public void setObservedByMonth(List<Map<String, String>> observedByMonth) {
		this.observedByMonth = observedByMonth;
	}
}
