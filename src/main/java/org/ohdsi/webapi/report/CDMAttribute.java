package org.ohdsi.webapi.report;

import java.util.Objects;

public class CDMAttribute {
	private String attributeName;
	private String attributeValue;
	
	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return the attributeValue
	 */
	public String getAttributeValue() {
		return attributeValue;
	}
	/**
	 * @param attributeValue the attributeValue to set
	 */
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CDMAttribute that = (CDMAttribute) o;
		return Objects.equals(attributeName, that.attributeName) &&
				Objects.equals(attributeValue, that.attributeValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributeName, attributeValue);
	}
}
