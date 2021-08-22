package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataImplDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortMetadataDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<CohortMetadataImplDTO> {

  @Override
  protected CohortMetadataImplDTO createResultObject() {
    return new CohortMetadataImplDTO();
  }
}
