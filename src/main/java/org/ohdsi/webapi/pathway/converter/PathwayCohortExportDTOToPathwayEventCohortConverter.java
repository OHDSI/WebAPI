package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortExportDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortExportDTOToPathwayEventCohortConverter extends BasePathwayCohortDTOToPathwayCohortConverter<PathwayCohortExportDTO, PathwayEventCohort> {

    public PathwayCohortExportDTOToPathwayEventCohortConverter(ConversionService conversionService) {

        super(conversionService);
    }

    @Override
    protected PathwayEventCohort getResultObject() {

        return new PathwayEventCohort();
    }
}
