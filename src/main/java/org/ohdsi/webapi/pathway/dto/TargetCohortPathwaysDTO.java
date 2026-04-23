package org.ohdsi.webapi.pathway.dto;

import java.util.List;

public class TargetCohortPathwaysDTO {

    private Integer targetCohortId;
    private Integer targetCohortCount;
    private Integer totalPathwaysCount;
    private List<PathwayPopulationEventDTO> pathways;

    public TargetCohortPathwaysDTO(Integer targetCohortId, Integer targetCohortCount, Integer totalPathwaysCount, List<PathwayPopulationEventDTO> pathways) {

        this.targetCohortId = targetCohortId;
        this.targetCohortCount = targetCohortCount;
        this.totalPathwaysCount = totalPathwaysCount;
        this.pathways = pathways;
    }

    public Integer getTargetCohortId() {

        return targetCohortId;
    }

    public void setTargetCohortId(Integer targetCohortId) {

        this.targetCohortId = targetCohortId;
    }

    public Integer getTargetCohortCount() {

        return targetCohortCount;
    }

    public void setTargetCohortCount(Integer targetCohortCount) {

        this.targetCohortCount = targetCohortCount;
    }

    public Integer getTotalPathwaysCount() {

        return totalPathwaysCount;
    }

    public void setTotalPathwaysCount(Integer totalPathwaysCount) {

        this.totalPathwaysCount = totalPathwaysCount;
    }

    public List<PathwayPopulationEventDTO> getPathways() {

        return pathways;
    }

    public void setPathways(List<PathwayPopulationEventDTO> pathways) {

        this.pathways = pathways;
    }
}
