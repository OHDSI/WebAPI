package org.ohdsi.webapi.pathway.dto;

import java.util.List;

public class TargetCohortPathwaysDTO {

    private Integer targetCohortId;
    List<PathwayPopulationEventDTO> pathways;

    public TargetCohortPathwaysDTO(Integer targetCohortId, List<PathwayPopulationEventDTO> pathways) {

        this.targetCohortId = targetCohortId;
        this.pathways = pathways;
    }

    public Integer getTargetCohortId() {

        return targetCohortId;
    }

    public void setTargetCohortId(Integer targetCohortId) {

        this.targetCohortId = targetCohortId;
    }

    public List<PathwayPopulationEventDTO> getPathways() {

        return pathways;
    }

    public void setPathways(List<PathwayPopulationEventDTO> pathways) {

        this.pathways = pathways;
    }
}
