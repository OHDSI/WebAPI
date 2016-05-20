package org.ohdsi.webapi.characterization;

import org.ohdsi.webapi.cohortresults.ConceptCountRecord;
import org.ohdsi.webapi.cohortresults.ConceptDistributionRecord;
import org.ohdsi.webapi.cohortresults.CumulativeObservationRecord;
import org.ohdsi.webapi.cohortresults.MonthObservationRecord;

import java.util.List;

public class Dashboard {

	private List<ConceptCountRecord> gender;
	private List<ConceptDistributionRecord> ageAtFirstObservation;
	private List<CumulativeObservationRecord> cumulativeObservation;
	private List<MonthObservationRecord> observedByMonth;
	/**
	 * @return the gender
	 */
	public List<ConceptCountRecord> getGender() {
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(List<ConceptCountRecord> gender) {
		this.gender = gender;
	}
	/**
	 * @return the ageAtFirstObservation
	 */
	public List<ConceptDistributionRecord> getAgeAtFirstObservation() {
		return ageAtFirstObservation;
	}
	/**
	 * @param ageAtFirstObservation the ageAtFirstObservation to set
	 */
	public void setAgeAtFirstObservation(
			List<ConceptDistributionRecord> ageAtFirstObservation) {
		this.ageAtFirstObservation = ageAtFirstObservation;
	}
	/**
	 * @return the cumulativeObservation
	 */
	public List<CumulativeObservationRecord> getCumulativeObservation() {
		return cumulativeObservation;
	}
	/**
	 * @param cumulativeObservation the cumulativeObservation to set
	 */
	public void setCumulativeObservation(
			List<CumulativeObservationRecord> cumulativeObservation) {
		this.cumulativeObservation = cumulativeObservation;
	}
	/**
	 * @return the observedByMonth
	 */
	public List<MonthObservationRecord> getObservedByMonth() {
		return observedByMonth;
	}
	/**
	 * @param observedByMonth the observedByMonth to set
	 */
	public void setObservedByMonth(List<MonthObservationRecord> observedByMonth) {
		this.observedByMonth = observedByMonth;
	}
	
	
}
