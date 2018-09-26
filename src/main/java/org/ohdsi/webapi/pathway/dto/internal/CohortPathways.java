package org.ohdsi.webapi.pathway.dto.internal;

import java.util.Map;

public class CohortPathways {

    private Integer cohortId;
    private Map<String, Integer> pathwaysCounts;

    public Integer getCohortId() {

        return cohortId;
    }

    public void setCohortId(Integer cohortId) {

        this.cohortId = cohortId;
    }

    public Map<String, Integer> getPathwaysCounts() {

        return pathwaysCounts;
    }

    public void setPathwaysCounts(Map<String, Integer> pathwaysCounts) {

        this.pathwaysCounts = pathwaysCounts;
    }
}
