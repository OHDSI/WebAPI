package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortMetadataDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<CohortMetadataDTO> {

  @Override
  protected CohortMetadataDTO getResultObject() {
    return new CohortMetadataDTO();
  }
}
