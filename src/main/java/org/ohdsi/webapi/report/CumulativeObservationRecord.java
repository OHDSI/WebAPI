package org.ohdsi.webapi.report;

public class CumulativeObservationRecord {

	private String seriesName;
	private int xLengthOfObservation;
	private double yPercentPersons;
	
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
	 * @return the xLengthOfObservation
	 */
	public int getxLengthOfObservation() {
		return xLengthOfObservation;
	}
	/**
	 * @param xLengthOfObservation the xLengthOfObservation to set
	 */
	public void setxLengthOfObservation(int xLengthOfObservation) {
		this.xLengthOfObservation = xLengthOfObservation;
	}
	/**
	 * @return the yPercentPersons
	 */
	public double getyPercentPersons() {
		return yPercentPersons;
	}
	/**
	 * @param yPercentPersons the yPercentPersons to set
	 */
	public void setyPercentPersons(double yPercentPersons) {
		this.yPercentPersons = yPercentPersons;
	}
}
