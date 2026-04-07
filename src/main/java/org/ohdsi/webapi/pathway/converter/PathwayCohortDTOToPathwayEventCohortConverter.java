package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortDTOToPathwayEventCohortConverter extends BasePathwayCohortDTOToPathwayCohortConverter<PathwayCohortDTO, PathwayEventCohort> {

    public PathwayCohortDTOToPathwayEventCohortConverter(ConversionService conversionService) {

        super(conversionService);
    }

    @Override
    protected PathwayEventCohort getResultObject() {

        return new PathwayEventCohort();
    }
}
