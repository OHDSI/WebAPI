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
public class CovariateSectionConverter implements AttributeConverter<CovariateSection, String> {

	@Override
	public String convertToDatabaseColumn(CovariateSection attribute) {
		switch (attribute) {
			case DEMOGRAPHICS:
				return "DEMOGRAPHICS";
			case CONDITIONS:
				return "CONDITIONS";
			case DRUGS:
				return "DRUGS";
			case MEASUREMENTS:
				return "MEASUREMENTS";
			case OBSERVATIONS:
				return "OBSERVATIONS";
			case PROCEDURES:
				return "PROCEDURES";
			default:
				throw new IllegalArgumentException("Unknown" + attribute);
		}
	}

	@Override
	public CovariateSection convertToEntityAttribute(String dbData) {
		switch (dbData) {
			case "DEMOGRAPHICS":
				return CovariateSection.DEMOGRAPHICS;
			case "CONDITIONS":
				return CovariateSection.CONDITIONS;
			case "DRUGS":
				return CovariateSection.DRUGS;
			case "MEASUREMENTS":
				return CovariateSection.MEASUREMENTS;
			case "OBSERVATIONS":
				return CovariateSection.OBSERVATIONS;
			case "PROCEDURES":
				return CovariateSection.PROCEDURES;
			default:
				throw new IllegalArgumentException("Unknown" + dbData);
		}
	}
}
