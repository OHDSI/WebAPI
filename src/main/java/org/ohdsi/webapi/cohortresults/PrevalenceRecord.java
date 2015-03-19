package org.ohdsi.webapi.cohortresults;

public class PrevalenceRecord {

	private int xCalendarMonth;
	private long conceptId;
	private double yPrevalence1000Pp;
	private int numPersons;
	
	/**
	 * @return the xCalendarMonth
	 */
	public int getxCalendarMonth() {
		return xCalendarMonth;
	}
	/**
	 * @param xCalendarMonth the xCalendarMonth to set
	 */
	public void setxCalendarMonth(int xCalendarMonth) {
		this.xCalendarMonth = xCalendarMonth;
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
	 * @return the yPrevalence1000Pp
	 */
	public double getyPrevalence1000Pp() {
		return yPrevalence1000Pp;
	}
	/**
	 * @param yPrevalence1000Pp the yPrevalence1000Pp to set
	 */
	public void setyPrevalence1000Pp(double yPrevalence1000Pp) {
		this.yPrevalence1000Pp = yPrevalence1000Pp;
	}
	/**
	 * @return the numPersons
	 */
	public int getNumPersons() {
		return numPersons;
	}
	/**
	 * @param numPersons the numPersons to set
	 */
	public void setNumPersons(int numPersons) {
		this.numPersons = numPersons;
	}
	
}
