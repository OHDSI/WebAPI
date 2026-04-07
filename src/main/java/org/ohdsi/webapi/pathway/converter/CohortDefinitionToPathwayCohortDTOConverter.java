package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.cohortdefinition.converter.BaseCohortDefinitionToCohortMetadataDTOConverter;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToPathwayCohortDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<PathwayCohortDTO> {
    @Override
    protected PathwayCohortDTO createResultObject() {
        return new PathwayCohortDTO();
    }
}
