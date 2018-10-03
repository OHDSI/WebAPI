package org.ohdsi.webapi.pathway.dto.internal;

public class PathwayGenerationStats {

    private Integer targetCohortId;
    private Integer targetCohortCount;
    private Integer pathwaysCount;

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

    public Integer getPathwaysCount() {

        return pathwaysCount;
    }

    public void setPathwaysCount(Integer pathwaysCount) {

        this.pathwaysCount = pathwaysCount;
    }
}
