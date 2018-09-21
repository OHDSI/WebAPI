package org.ohdsi.webapi.model.results;


/**
 * 
 * For OHDSI implementations, this corresponds to the HERACLES_RESULTS_DIST table
 *
 */
public class AnalysisResultsDist extends AnalysisResults {

	public static final String MIN_VALUE = "MIN_VALUE";
	public static final String MAX_VALUE = "MAX_VALUE";
	public static final String AVG_VALUE = "AVG_VALUE";
	public static final String STDEV_VALUE = "STDEV_VALUE";
	public static final String MEDIAN_VALUE = "MEDIAN_VALUE";
	public static final String P10_VALUE = "P10_VALUE";
	public static final String P25_VALUE = "P25_VALUE";
	public static final String P75_VALUE = "P75_VALUE";
	public static final String P90_VALUE = "P90_VALUE";
	
	private double minValue;
	
	private double maxValue;
	
	private double avgValue;
	
	private double stdevValue;
	
	private double medianValue;
	
	private double p10Value;
	
	private double p25Value;
	
	private double p75Value;
	
	private double p90Value
			;

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getAvgValue() {
		return avgValue;
	}

	public void setAvgValue(double avgValue) {
		this.avgValue = avgValue;
	}

	public double getStdevValue() {
		return stdevValue;
	}

	public void setStdevValue(double stdevValue) {
		this.stdevValue = stdevValue;
	}

	public double getMedianValue() {
		return medianValue;
	}

	public void setMedianValue(double medianValue) {
		this.medianValue = medianValue;
	}

	public double getP10Value() {
		return p10Value;
	}

	public void setP10Value(double p10Value) {
		this.p10Value = p10Value;
	}

	public double getP25Value() {
		return p25Value;
	}

	public void setP25Value(double p25Value) {
		this.p25Value = p25Value;
	}

	public double getP75Value() {
		return p75Value;
	}

	public void setP75Value(double p75Value) {
		this.p75Value = p75Value;
	}

	public double getP90Value() {
		return p90Value;
	}

	public void setP90Value(double p90Value) {
		this.p90Value = p90Value;
	}
	
	
}
