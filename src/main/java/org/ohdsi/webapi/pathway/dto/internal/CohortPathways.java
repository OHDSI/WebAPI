package org.ohdsi.webapi.pathway.dto.internal;

import java.util.Map;

public class CohortPathways {

    private Integer cohortId;
    private Integer targetCohortCount;
    private Integer totalPathwaysCount;
    private Map<String, Integer> pathwaysCounts;

    public Integer getCohortId() {

        return cohortId;
    }

    public void setCohortId(Integer cohortId) {

        this.cohortId = cohortId;
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

    public Map<String, Integer> getPathwaysCounts() {

        return pathwaysCounts;
    }

    public void setPathwaysCounts(Map<String, Integer> pathwaysCounts) {

        this.pathwaysCounts = pathwaysCounts;
    }
}
