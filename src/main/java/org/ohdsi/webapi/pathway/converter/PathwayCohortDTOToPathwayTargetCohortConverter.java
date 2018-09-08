package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortDTOToPathwayTargetCohortConverter extends BasePathwayCohortDTOToPathwayCohortConverter<PathwayTargetCohort> {

    @Override
    protected PathwayTargetCohort getResultObject() {

        return new PathwayTargetCohort();
    }
}
