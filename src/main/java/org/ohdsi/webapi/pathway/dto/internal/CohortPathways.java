package org.ohdsi.webapi.pathway.dto.internal;

import java.util.Map;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(getCohortId(), getTargetCohortCount(), getTotalPathwaysCount(), getPathwaysCounts());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CohortPathways)) return false;
        CohortPathways that = (CohortPathways) o;
        return Objects.equals(getCohortId(), that.getCohortId()) &&
                Objects.equals(getTargetCohortCount(), that.getTargetCohortCount()) &&
                Objects.equals(getTotalPathwaysCount(), that.getTotalPathwaysCount()) &&
                Objects.equals(getPathwaysCounts(), that.getPathwaysCounts());
    }
}
