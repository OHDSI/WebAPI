package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortToPathwayCohortDTOConverter extends BaseConversionServiceAwareConverter<PathwayCohort, PathwayCohortDTO> {

    @Override
    public PathwayCohortDTO convert(PathwayCohort source) {

        PathwayCohortDTO result = new PathwayCohortDTO();
        result.setId(source.getId());
        result.setName(source.getName());
        result.setDescription(source.getCohortDefinition().getDescription());
        result.setCohortDefinitionId(source.getCohortDefinition().getId());
        return result;
    }
}
