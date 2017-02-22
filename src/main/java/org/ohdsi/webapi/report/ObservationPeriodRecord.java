package org.ohdsi.webapi.report;

public class ObservationPeriodRecord {
	private int cohortDefinitionId;
	private double pctPersons;
	private int countValue;
	private int duration;
	/**
	 * @return the cohortDefinitionId
	 */
	public int getCohortDefinitionId() {
		return cohortDefinitionId;
	}
	/**
	 * @param cohortDefinitionId the cohortDefinitionId to set
	 */
	public void setCohortDefinitionId(int cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
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
	
	
}
