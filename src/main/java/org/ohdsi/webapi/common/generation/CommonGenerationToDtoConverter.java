package org.ohdsi.webapi.common.generation;

import org.springframework.stereotype.Component;

@Component
public class CommonGenerationToDtoConverter extends BaseCommonGenerationToDtoConverter<CommonGeneration, CommonGenerationDTO> {

  @Override
  protected CommonGenerationDTO createResultObject() {
    return new CommonGenerationDTO();
  }
}
