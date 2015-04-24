package org.ohdsi.webapi.cohortresults;

/**
 * 
 * i.e. box plot
 *
 */
public class ConceptQuartileRecord {
	
	private String category;
	private long conceptId;
	
	private int p10Value;
	private int p25Value;
	private int p75Value;
	private int p90Value;
	
	private int minValue;
	private int medianValue;
	private int maxValue;
	
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
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
	 * @return the p10Value
	 */
	public int getP10Value() {
		return p10Value;
	}
	/**
	 * @param p10Value the p10Value to set
	 */
	public void setP10Value(int p10Value) {
		this.p10Value = p10Value;
	}
	/**
	 * @return the p25Value
	 */
	public int getP25Value() {
		return p25Value;
	}
	/**
	 * @param p25Value the p25Value to set
	 */
	public void setP25Value(int p25Value) {
		this.p25Value = p25Value;
	}
	/**
	 * @return the p75Value
	 */
	public int getP75Value() {
		return p75Value;
	}
	/**
	 * @param p75Value the p75Value to set
	 */
	public void setP75Value(int p75Value) {
		this.p75Value = p75Value;
	}
	/**
	 * @return the p90Value
	 */
	public int getP90Value() {
		return p90Value;
	}
	/**
	 * @param p90Value the p90Value to set
	 */
	public void setP90Value(int p90Value) {
		this.p90Value = p90Value;
	}
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
	 * @return the medianValue
	 */
	public int getMedianValue() {
		return medianValue;
	}
	/**
	 * @param medianValue the medianValue to set
	 */
	public void setMedianValue(int medianValue) {
		this.medianValue = medianValue;
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
	
}
