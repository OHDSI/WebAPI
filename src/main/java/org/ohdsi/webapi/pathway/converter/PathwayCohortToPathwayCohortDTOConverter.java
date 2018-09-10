package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortToPathwayCohortDTOConverter extends BasePathwayCohortToPathwayCohortDTOConverter<PathwayCohortDTO> {

    @Override
    protected PathwayCohortDTO getResultObject() {

        return new PathwayCohortDTO();
    }
}
