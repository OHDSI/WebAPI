/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.study;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Converter(autoApply = true)
public class RelationshipTypeConverter implements AttributeConverter<RelationshipType, String> {

  @Override
  public String convertToDatabaseColumn(RelationshipType attribute) {
    switch (attribute) {
      case INDICATION:
        return "INDICATION";
      default:
        throw new IllegalArgumentException("Unknown" + attribute);
    }
  }

  @Override
  public RelationshipType convertToEntityAttribute(String dbData) {
    switch (dbData) {
      case "INDICATION":
        return RelationshipType.INDICATION;
      default:
        throw new IllegalArgumentException("Unknown" + dbData);
    }
  }
}
