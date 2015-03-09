package org.ohdsi.webapi.cohortresults;

/**
 * 
 * i.e. donut
 *
 */
public class ConceptCountRecord {

	private String conditionConceptName;
	private long conditionConceptId;
	
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
}
