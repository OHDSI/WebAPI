package org.ohdsi.webapi.cohortresults;

import java.util.List;

public class CohortMeasurementDrilldown {
	private List<ConceptQuartileRecord> ageAtFirstOccurrence;
	private List<ConceptQuartileRecord> lowerLimitDistribution;
	private List<ConceptQuartileRecord> measurementValueDistribution;
	private List<ConceptQuartileRecord> upperLimitDistribution;
	
	private List<ConceptCountRecord> observationsByType;
	private List<ConceptCountRecord> recordsByUnit;
	private List<ConceptCountRecord> valuesRelativeToNorm;
	
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
	 * @return the lowerLimitDistribution
	 */
	public List<ConceptQuartileRecord> getLowerLimitDistribution() {
		return lowerLimitDistribution;
	}
	/**
	 * @param lowerLimitDistribution the lowerLimitDistribution to set
	 */
	public void setLowerLimitDistribution(
			List<ConceptQuartileRecord> lowerLimitDistribution) {
		this.lowerLimitDistribution = lowerLimitDistribution;
	}
	/**
	 * @return the observationsByType
	 */
	public List<ConceptCountRecord> getMeasurementsByType() {
		return observationsByType;
	}
	/**
	 * @param observationsByType the observationsByType to set
	 */
	public void setMeasurementsByType(List<ConceptCountRecord> observationsByType) {
		this.observationsByType = observationsByType;
	}
	/**
	 * @return the observationValueDistribution
	 */
	public List<ConceptQuartileRecord> getMeasurementValueDistribution() {
		return measurementValueDistribution;
	}
	/**
	 * @param observationValueDistribution the observationValueDistribution to set
	 */
	public void setMeasurementValueDistribution(
			List<ConceptQuartileRecord> observationValueDistribution) {
		this.measurementValueDistribution = observationValueDistribution;
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
	/**
	 * @return the recordsByUnit
	 */
	public List<ConceptCountRecord> getRecordsByUnit() {
		return recordsByUnit;
	}
	/**
	 * @param recordsByUnit the recordsByUnit to set
	 */
	public void setRecordsByUnit(List<ConceptCountRecord> recordsByUnit) {
		this.recordsByUnit = recordsByUnit;
	}
	/**
	 * @return the upperLimitDistribution
	 */
	public List<ConceptQuartileRecord> getUpperLimitDistribution() {
		return upperLimitDistribution;
	}
	/**
	 * @param upperLimitDistribution the upperLimitDistribution to set
	 */
	public void setUpperLimitDistribution(
			List<ConceptQuartileRecord> upperLimitDistribution) {
		this.upperLimitDistribution = upperLimitDistribution;
	}
	/**
	 * @return the valuesRelativeToNorm
	 */
	public List<ConceptCountRecord> getValuesRelativeToNorm() {
		return valuesRelativeToNorm;
	}
	/**
	 * @param valuesRelativeToNorm the valuesRelativeToNorm to set
	 */
	public void setValuesRelativeToNorm(
			List<ConceptCountRecord> valuesRelativeToNorm) {
		this.valuesRelativeToNorm = valuesRelativeToNorm;
	}
}
