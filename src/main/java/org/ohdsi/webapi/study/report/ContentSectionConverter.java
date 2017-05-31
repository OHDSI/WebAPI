/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.study.report;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Converter(autoApply = true)
public class ContentSectionConverter implements AttributeConverter<ContentSection, String> {

	@Override
	public String convertToDatabaseColumn(ContentSection attribute) {
		switch (attribute) {
			case BACKGROUND:
				return "BACKGROUND";
			case METHODS:
				return "METHODS";
			case RESULTS:
				return "RESULTS";
			case CONCLUSION:
				return "CONCLUSION";
			default:
				throw new IllegalArgumentException("Unknown" + attribute);
		}
	}

	@Override
	public ContentSection convertToEntityAttribute(String dbData) {
		switch (dbData) {
			case "BACKGROUND":
				return ContentSection.BACKGROUND;
			case "METHODS":
				return ContentSection.METHODS;
			case "RESULTS":
				return ContentSection.RESULTS;
			case "CONCLUSION":
				return ContentSection.CONCLUSION;
			default:
				throw new IllegalArgumentException("Unknown" + dbData);
		}
	}
}
