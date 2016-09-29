package org.ohdsi.webapi.report;

/**
 * 
 * i.e. treemap
 *
 */
public class HierarchicalConceptRecord {

	private long conceptId;
	private String conceptPath;
	private double recordsPerPerson;
	private double percentPersons;
	private long numPersons;
	private double lengthOfEra;
	private double percentPersonsBefore;
	private double percentPersonsAfter;
	private double riskDiffAfterBefore;
	private double logRRAfterBefore;
        private long countValue;
	
	public HierarchicalConceptRecord() {
		// default constructor
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
	 * @return the conceptPath
	 */
	public String getConceptPath() {
		return conceptPath;
	}
	/**
	 * @param conceptPath the conceptPath to set
	 */
	public void setConceptPath(String conceptPath) {
		this.conceptPath = conceptPath;
	}
	/**
	 * @return the recordsPerPerson
	 */
	public double getRecordsPerPerson() {
		return recordsPerPerson;
	}
	/**
	 * @param recordsPerPerson the recordsPerPerson to set
	 */
	public void setRecordsPerPerson(double recordsPerPerson) {
		this.recordsPerPerson = recordsPerPerson;
	}
	/**
	 * @return the percentPersons
	 */
	public double getPercentPersons() {
		return percentPersons;
	}
	/**
	 * @param percentPersons the percentPersons to set
	 */
	public void setPercentPersons(double percentPersons) {
		this.percentPersons = percentPersons;
	}
	/**
	 * @return the numPersons
	 */
	public long getNumPersons() {
		return numPersons;
	}
	/**
	 * @param numPersons the numPersons to set
	 */
	public void setNumPersons(long numPersons) {
		this.numPersons = numPersons;
	}
	/**
	 * @return the lengthOfEra
	 */
	public double getLengthOfEra() {
		return lengthOfEra;
	}
	/**
	 * @param lengthOfEra the lengthOfEra to set
	 */
	public void setLengthOfEra(double lengthOfEra) {
		this.lengthOfEra = lengthOfEra;
	}
	/**
	 * @return the percentPersonsBefore
	 */
	public double getPercentPersonsBefore() {
		return percentPersonsBefore;
	}

	/**
	 * @param percentPersonsBefore the percentPersonsBefore to set
	 */
	public void setPercentPersonsBefore(double percentPersonsBefore) {
		this.percentPersonsBefore = percentPersonsBefore;
	}

	/**
	 * @return the percentPersonsAfter
	 */
	public double getPercentPersonsAfter() {
		return percentPersonsAfter;
	}

	/**
	 * @param percentPersonsAfter the percentPersonsAfter to set
	 */
	public void setPercentPersonsAfter(double percentPersonsAfter) {
		this.percentPersonsAfter = percentPersonsAfter;
	}

	/**
	 * @return the riskDiffAfterBefore
	 */
	public double getRiskDiffAfterBefore() {
		return riskDiffAfterBefore;
	}

	/**
	 * @param riskDiffAfterBefore the riskDiffAfterBefore to set
	 */
	public void setRiskDiffAfterBefore(double riskDiffAfterBefore) {
		this.riskDiffAfterBefore = riskDiffAfterBefore;
	}

	/**
	 * @return the logRRAfterBefore
	 */
	public double getLogRRAfterBefore() {
		return logRRAfterBefore;
	}

	/**
	 * @param logRRAfterBefore the logRRAfterBefore to set
	 */
	public void setLogRRAfterBefore(double logRRAfterBefore) {
		this.logRRAfterBefore = logRRAfterBefore;
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
