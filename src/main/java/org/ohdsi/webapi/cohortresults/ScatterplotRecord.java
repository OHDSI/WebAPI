package org.ohdsi.webapi.cohortresults;

public class ScatterplotRecord {

	private String recordType;
	private long conceptId;
	private String conceptName;
	private int duration;
	private int countValue;
	private double pctPersons;
	
	/**
	 * @return the recordType
	 */
	public String getRecordType() {
		return recordType;
	}
	/**
	 * @param recordType the recordType to set
	 */
	public void setRecordType(String recordType) {
		this.recordType = recordType;
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
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/**
	 * @return the countValue
	 */
	public int getCountValue() {
		return countValue;
	}
	/**
	 * @param countValue the countValue to set
	 */
	public void setCountValue(int countValue) {
		this.countValue = countValue;
	}
	/**
	 * @return the pctPersons
	 */
	public double getPctPersons() {
		return pctPersons;
	}
	/**
	 * @param pctPersons the pctPersons to set
	 */
	public void setPctPersons(double pctPersons) {
		this.pctPersons = pctPersons;
	}
}
