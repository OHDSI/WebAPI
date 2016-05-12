package org.ohdsi.webapi.report;

public class SeriesPerPerson {
	private String seriesName;
	private int xCalendarMonth;
	private long yRecordCount;
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
	 * @return the yRecordCount
	 */
	public long getyRecordCount() {
		return yRecordCount;
	}
	/**
	 * @param yRecordCount the yRecordCount to set
	 */
	public void setyRecordCount(long yRecordCount) {
		this.yRecordCount = yRecordCount;
	} 
}
