package com.jnj.honeur.webapi.cohortinclusionstats;

import java.io.Serializable;
import java.util.Objects;

public class CohortInclusionStatsEntityId implements Serializable {

    private Long cohortDefinitionId;
    private int ruleSequence;

    public CohortInclusionStatsEntityId() {
    }

    public CohortInclusionStatsEntityId(Long cohortDefinitionId, int ruleSequence) {
        this.cohortDefinitionId = cohortDefinitionId;
        this.ruleSequence = ruleSequence;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public int getRuleSequence() {
        return ruleSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortInclusionStatsEntityId that = (CohortInclusionStatsEntityId) o;
        return ruleSequence == that.ruleSequence &&
                Objects.equals(cohortDefinitionId, that.cohortDefinitionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, ruleSequence);
    }
}
