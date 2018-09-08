package org.ohdsi.webapi.pathway.dto;

import java.util.List;

public class PathwayPopulationResultsDTO {

    private List<PathwayCodeDTO> eventCodes;
    private List<PathwayPopulationEventDTO> pathways;

    public List<PathwayCodeDTO> getEventCodes() {

        return eventCodes;
    }

    public void setEventCodes(List<PathwayCodeDTO> eventCodes) {

        this.eventCodes = eventCodes;
    }

    public List<PathwayPopulationEventDTO> getPathways() {

        return pathways;
    }

    public void setPathways(List<PathwayPopulationEventDTO> pathways) {

        this.pathways = pathways;
    }
}
