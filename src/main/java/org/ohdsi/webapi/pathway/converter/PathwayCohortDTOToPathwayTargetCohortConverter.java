package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortDTOToPathwayTargetCohortConverter extends BasePathwayCohortDTOToPathwayCohortConverter<PathwayCohortDTO, PathwayTargetCohort> {

    public PathwayCohortDTOToPathwayTargetCohortConverter(ConversionService conversionService) {

        super(conversionService);
    }

    @Override
    protected PathwayTargetCohort getResultObject() {

        return new PathwayTargetCohort();
    }
}
