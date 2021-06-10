package org.ohdsi.webapi.report;

import java.util.Objects;

public class CohortStatsRecord {

	private int minValue;
	private int maxValue;
	private int intervalSize;
	
	/**
	 * @return the minValue
	 */
	public int getMinValue() {
		return minValue;
	}
	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
	/**
	 * @return the maxValue
	 */
	public int getMaxValue() {
		return maxValue;
	}
	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	/**
	 * @return the intervalSize
	 */
	public int getIntervalSize() {
		return intervalSize;
	}
	/**
	 * @param intervalSize the intervalSize to set
	 */
	public void setIntervalSize(int intervalSize) {
		this.intervalSize = intervalSize;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CohortStatsRecord that = (CohortStatsRecord) o;
		return minValue == that.minValue &&
				maxValue == that.maxValue &&
				intervalSize == that.intervalSize;
	}

	@Override
	public int hashCode() {
		return Objects.hash(minValue, maxValue, intervalSize);
	}
}
