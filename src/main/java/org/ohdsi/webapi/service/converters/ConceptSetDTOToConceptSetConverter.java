package org.ohdsi.webapi.service.converters;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class ConceptSetDTOToConceptSetConverter implements Converter<ConceptSetDTO, ConceptSet> {

  public ConceptSetDTOToConceptSetConverter(GenericConversionService conversionService) {

    conversionService.addConverter(this);
  }

  @Override
  public ConceptSet convert(ConceptSetDTO dto) {

    ConceptSet conceptSet = new ConceptSet();
    conceptSet.setId(dto.getId());
    conceptSet.setName(StringUtils.trim(dto.getName()));
    conceptSet.setDescription(dto.getDescription());
    return conceptSet;
  }
}
