package com.jnj.honeur.webapi.cohortinclusionresult;

import java.io.Serializable;
import java.util.Objects;

public class CohortInclusionResultEntityId implements Serializable {

    private Long cohortDefinitionId;
    private Long inclusionRuleMask;

    public CohortInclusionResultEntityId() {
    }

    public CohortInclusionResultEntityId(Long cohortDefinitionId, Long inclusionRuleMask) {
        this.cohortDefinitionId = cohortDefinitionId;
        this.inclusionRuleMask = inclusionRuleMask;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public Long getInclusionRuleMask() {
        return inclusionRuleMask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortInclusionResultEntityId that = (CohortInclusionResultEntityId) o;
        return Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(inclusionRuleMask, that.inclusionRuleMask);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, inclusionRuleMask);
    }
}
