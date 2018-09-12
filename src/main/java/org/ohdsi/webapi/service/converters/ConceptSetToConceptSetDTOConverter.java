package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class ConceptSetToConceptSetDTOConverter extends BaseCommonEntityToDTOConverter<ConceptSet, ConceptSetDTO> {

  public ConceptSetToConceptSetDTOConverter(GenericConversionService conversionService) {

    conversionService.addConverter(this);
  }

  @Override
  protected ConceptSetDTO newTarget() {
    return new ConceptSetDTO();
  }

  @Override
  protected void doConvert(ConceptSetDTO target, ConceptSet source) {
    target.setId(source.getId());
    target.setName(source.getName());
  }

}
