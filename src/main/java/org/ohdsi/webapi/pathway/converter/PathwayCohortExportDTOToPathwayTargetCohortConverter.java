package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.PathwayCohortExportDTO;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortExportDTOToPathwayTargetCohortConverter extends BasePathwayCohortDTOToPathwayCohortConverter<PathwayCohortExportDTO, PathwayTargetCohort> {

    public PathwayCohortExportDTOToPathwayTargetCohortConverter(ConversionService conversionService) {

        super(conversionService);
    }

    @Override
    protected PathwayTargetCohort getResultObject() {

        return new PathwayTargetCohort();
    }
}
