package org.ohdsi.webapi.report;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CumulativeObservationRecord that = (CumulativeObservationRecord) o;
		return xLengthOfObservation == that.xLengthOfObservation &&
				Double.compare(that.yPercentPersons, yPercentPersons) == 0 &&
				Objects.equals(seriesName, that.seriesName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(seriesName, xLengthOfObservation, yPercentPersons);
	}
}
