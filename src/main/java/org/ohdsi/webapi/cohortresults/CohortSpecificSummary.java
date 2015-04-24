package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortSpecificSummary {
	private List<ObservationPeriodRecord> personsByDurationFromStartToEnd;
	private List<PrevalenceRecord> prevalenceByMonth;
	private List<ConceptDecileRecord> numPersonsByCohortStartByGenderByAge;
	private List<ConceptQuartileRecord> ageAtIndexDistribution;
	private List<MonthObservationRecord> personsInCohortFromCohortStartToEnd;
	private List<ConceptQuartileRecord> distributionAgeCohortStartByCohortStartYear;
	private List<ConceptQuartileRecord> distributionAgeCohortStartByGender;
	private List<ScatterplotRecord> drugOccursRelativeToIndex;
	private List<ScatterplotRecord> firstConditionRelativeToIndex;
	private List<ScatterplotRecord> procedureOccursRelativeToIndex;

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

	/**
	 * @return the distributionAgeCohortStartByCohortStartYear
	 */
	public List<ConceptQuartileRecord> getDistributionAgeCohortStartByCohortStartYear() {
		return distributionAgeCohortStartByCohortStartYear;
	}

	/**
	 * @param distributionAgeCohortStartByCohortStartYear the distributionAgeCohortStartByCohortStartYear to set
	 */
	public void setDistributionAgeCohortStartByCohortStartYear(
			List<ConceptQuartileRecord> distributionAgeCohortStartByCohortStartYear) {
		this.distributionAgeCohortStartByCohortStartYear = distributionAgeCohortStartByCohortStartYear;
	}

	/**
	 * @return the distributionAgeCohortStartByGender
	 */
	public List<ConceptQuartileRecord> getDistributionAgeCohortStartByGender() {
		return distributionAgeCohortStartByGender;
	}

	/**
	 * @param distributionAgeCohortStartByGender the distributionAgeCohortStartByGender to set
	 */
	public void setDistributionAgeCohortStartByGender(
			List<ConceptQuartileRecord> distributionAgeCohortStartByGender) {
		this.distributionAgeCohortStartByGender = distributionAgeCohortStartByGender;
	}

	/**
	 * @return the drugOccursRelativeToIndex
	 */
	public List<ScatterplotRecord> getDrugOccursRelativeToIndex() {
		return drugOccursRelativeToIndex;
	}

	/**
	 * @param drugOccursRelativeToIndex the drugOccursRelativeToIndex to set
	 */
	public void setDrugOccursRelativeToIndex(
			List<ScatterplotRecord> drugOccursRelativeToIndex) {
		this.drugOccursRelativeToIndex = drugOccursRelativeToIndex;
	}

	/**
	 * @return the firstConditionRelativeToIndex
	 */
	public List<ScatterplotRecord> getFirstConditionRelativeToIndex() {
		return firstConditionRelativeToIndex;
	}

	/**
	 * @param firstConditionRelativeToIndex the firstConditionRelativeToIndex to set
	 */
	public void setFirstConditionRelativeToIndex(
			List<ScatterplotRecord> firstConditionRelativeToIndex) {
		this.firstConditionRelativeToIndex = firstConditionRelativeToIndex;
	}

	/**
	 * @return the procedureOccursRelativeToIndex
	 */
	public List<ScatterplotRecord> getProcedureOccursRelativeToIndex() {
		return procedureOccursRelativeToIndex;
	}

	/**
	 * @param procedureOccursRelativeToIndex the procedureOccursRelativeToIndex to set
	 */
	public void setProcedureOccursRelativeToIndex(
			List<ScatterplotRecord> procedureOccursRelativeToIndex) {
		this.procedureOccursRelativeToIndex = procedureOccursRelativeToIndex;
	}

}
