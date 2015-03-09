package org.ohdsi.webapi.cohortresults;

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
	private double lengthOfEra;
	
	
}
