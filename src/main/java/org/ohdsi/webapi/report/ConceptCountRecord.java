package org.ohdsi.webapi.report;

import java.util.Objects;

/**
 * 
 * i.e. donut
 *
 */
public class ConceptCountRecord {

	private String conditionConceptName;
	private long conditionConceptId;
	
	private String observationConceptName;
	private long observationConceptId;
	
	private String conceptName;
	private long conceptId;
	
	private long countValue;
	
	/**
	 * @return the conditionConceptName
	 */
	public String getConditionConceptName() {
		return conditionConceptName;
	}
	/**
	 * @param conditionConceptName the conditionConceptName to set
	 */
	public void setConditionConceptName(String conditionConceptName) {
		this.conditionConceptName = conditionConceptName;
	}
	/**
	 * @return the conditionConceptId
	 */
	public long getConditionConceptId() {
		return conditionConceptId;
	}
	/**
	 * @param conditionConceptId the conditionConceptId to set
	 */
	public void setConditionConceptId(long conditionConceptId) {
		this.conditionConceptId = conditionConceptId;
	}
	/**
	 * @return the observationConceptName
	 */
	public String getObservationConceptName() {
		return observationConceptName;
	}
	/**
	 * @param observationConceptName the observationConceptName to set
	 */
	public void setObservationConceptName(String observationConceptName) {
		this.observationConceptName = observationConceptName;
	}
	/**
	 * @return the observationConceptId
	 */
	public long getObservationConceptId() {
		return observationConceptId;
	}
	/**
	 * @param observationConceptId the observationConceptId to set
	 */
	public void setObservationConceptId(long observationConceptId) {
		this.observationConceptId = observationConceptId;
	}
	/**
	 * @return the conceptName
	 */
	public String getConceptName() {
		return conceptName;
	}
	/**
	 * @param conceptName the conceptName to set
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
	/**
	 * @return the conceptId
	 */
	public long getConceptId() {
		return conceptId;
	}
	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(long conceptId) {
		this.conceptId = conceptId;
	}
	/**
	 * @return the countValue
	 */
	public long getCountValue() {
		return countValue;
	}
	/**
	 * @param countValue the countValue to set
	 */
	public void setCountValue(long countValue) {
		this.countValue = countValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConceptCountRecord that = (ConceptCountRecord) o;
		return conditionConceptId == that.conditionConceptId &&
				observationConceptId == that.observationConceptId &&
				conceptId == that.conceptId &&
				countValue == that.countValue &&
				Objects.equals(conditionConceptName, that.conditionConceptName) &&
				Objects.equals(observationConceptName, that.observationConceptName) &&
				Objects.equals(conceptName, that.conceptName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(conditionConceptName, conditionConceptId, observationConceptName, observationConceptId, conceptName, conceptId, countValue);
	}
}
