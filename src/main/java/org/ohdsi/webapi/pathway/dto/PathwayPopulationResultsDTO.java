package org.ohdsi.webapi.pathway.dto;

import java.util.List;

public class PathwayPopulationResultsDTO {

    private List<PathwayCodeDTO> eventCodes;
    private List<TargetCohortPathwaysDTO> pathwayGroups;

    public PathwayPopulationResultsDTO(List<PathwayCodeDTO> eventCodes, List<TargetCohortPathwaysDTO> pathwayGroups) {

        this.eventCodes = eventCodes;
        this.pathwayGroups = pathwayGroups;
    }

    public List<PathwayCodeDTO> getEventCodes() {

        return eventCodes;
    }

    public void setEventCodes(List<PathwayCodeDTO> eventCodes) {

        this.eventCodes = eventCodes;
    }

    public List<TargetCohortPathwaysDTO> getPathwayGroups() {

        return pathwayGroups;
    }

    public void setPathwayGroups(List<TargetCohortPathwaysDTO> pathwayGroups) {

        this.pathwayGroups = pathwayGroups;
    }
}
