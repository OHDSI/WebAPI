package org.ohdsi.webapi.cohortresults;

/**
 * 
 * i.e. histogram record
 *
 */
public class ConceptDistributionRecord {
	private int intervalIndex;
	private double percentValue;
	private long countValue;
	/**
	 * @return the intervalIndex
	 */
	public int getIntervalIndex() {
		return intervalIndex;
	}
	/**
	 * @param intervalIndex the intervalIndex to set
	 */
	public void setIntervalIndex(int intervalIndex) {
		this.intervalIndex = intervalIndex;
	}
	/**
	 * @return the percentValue
	 */
	public double getPercentValue() {
		return percentValue;
	}
	/**
	 * @param percentValue the percentValue to set
	 */
	public void setPercentValue(double percentValue) {
		this.percentValue = percentValue;
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
