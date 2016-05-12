package org.ohdsi.webapi.report;

/**
 * 
 * i.e. Trellis plot record
 *
 */
public class ConceptDecileRecord {

	private String trellisName;
	private long conceptId;
	private String seriesName;
	private double yPrevalence1000Pp;
	private int xCalendarYear;
	private int numPersons;
	
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
	/**
	 * @return the trellisName
	 */
	public String getTrellisName() {
		return trellisName;
	}
	/**
	 * @param trellisName the trellisName to set
	 */
	public void setTrellisName(String trellisName) {
		this.trellisName = trellisName;
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
	 * @return the seriesName
	 */
	public String getSeriesName() {
		return seriesName;
	}
	/**
	 * @param seriesName the seriesName to set
	 */
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
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
	 * @return the xCalendarYear
	 */
	public int getxCalendarYear() {
		return xCalendarYear;
	}
	/**
	 * @param xCalendarYear the xCalendarYear to set
	 */
	public void setxCalendarYear(int xCalendarYear) {
		this.xCalendarYear = xCalendarYear;
	}
	
	
}
