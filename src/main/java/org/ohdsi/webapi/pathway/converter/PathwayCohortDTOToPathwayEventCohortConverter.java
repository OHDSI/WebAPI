package org.ohdsi.webapi.pathway.converter;

import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.springframework.stereotype.Component;

@Component
public class PathwayCohortDTOToPathwayEventCohortConverter extends BasePathwayCohortDTOToPathwayCohortConverter<PathwayEventCohort> {

    @Override
    protected PathwayEventCohort getResultObject() {

        return new PathwayEventCohort();
    }
}
