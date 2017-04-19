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
public class ReportStatusConverter implements AttributeConverter<ReportStatus, String> {

  @Override
  public String convertToDatabaseColumn(ReportStatus attribute) {
		if (attribute == null)
			return null;
		
    switch (attribute) {
      case DRAFT:
        return "DRAFT";
      case PUBLISHED:
        return "PUBLISHED";
      case DELETED:
        return "DELETED";
      default:
        throw new IllegalArgumentException("Unknown" + attribute);
    }
  }

  @Override
  public ReportStatus convertToEntityAttribute(String dbData) {
		if (dbData == null)
			return null;
		
    switch (dbData) {
      case "DRAFT":
        return ReportStatus.DRAFT;
      case "PUBLISHED":
        return ReportStatus.PUBLISHED;
      case "DELETED":
        return ReportStatus.DELETED;
      default:
        throw new IllegalArgumentException("Unknown" + dbData);
    }
  }
}
