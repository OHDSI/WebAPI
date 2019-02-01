package com.jnj.honeur.webapi.cohortsummarystats;

import java.io.Serializable;
import java.util.Objects;

public class CohortSummaryStatsEntityId implements Serializable {

    private Long cohortDefinitionId;
    private int modeId;

    public CohortSummaryStatsEntityId() {
    }

    public CohortSummaryStatsEntityId(Long cohortDefinitionId, int modeId) {
        this.cohortDefinitionId = cohortDefinitionId;
        this.modeId = modeId;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public int getModeId() {
        return modeId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortSummaryStatsEntityId that = (CohortSummaryStatsEntityId) o;
        return modeId == that.modeId &&
                Objects.equals(cohortDefinitionId, that.cohortDefinitionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, modeId);
    }
}
