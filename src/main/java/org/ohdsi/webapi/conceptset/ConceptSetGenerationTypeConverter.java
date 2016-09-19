/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.conceptset;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */
@Converter(autoApply = true)
public class ConceptSetGenerationTypeConverter implements AttributeConverter<ConceptSetGenerationType, Integer>{

  @Override
  public Integer convertToDatabaseColumn(ConceptSetGenerationType status) {
    switch (status) {
      case NEGATIVE_CONTROLS:
        return 0;
      default:
        throw new IllegalArgumentException("Unknown value: " + status);
    }
  }

  @Override
  public ConceptSetGenerationType convertToEntityAttribute(Integer statusValue) {
    switch (statusValue) {
      case 0: return ConceptSetGenerationType.NEGATIVE_CONTROLS;
      default: throw new IllegalArgumentException("Unknown status value: " + statusValue);
    }
  }
  
}



