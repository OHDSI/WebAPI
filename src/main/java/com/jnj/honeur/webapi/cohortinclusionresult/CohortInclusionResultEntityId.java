package com.jnj.honeur.webapi.cohortinclusionresult;

import java.io.Serializable;
import java.util.Objects;

public class CohortInclusionResultEntityId implements Serializable {

    private Long cohortDefinitionId;
    private Integer modeId;
    private Long inclusionRuleMask;

    public CohortInclusionResultEntityId() {
    }

    public CohortInclusionResultEntityId(Long cohortDefinitionId, Integer modeId, Long inclusionRuleMask) {
        this.cohortDefinitionId = cohortDefinitionId;
        this.modeId = modeId;
        this.inclusionRuleMask = inclusionRuleMask;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public Long getInclusionRuleMask() {
        return inclusionRuleMask;
    }

    public Integer getModeId() {
        return modeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortInclusionResultEntityId that = (CohortInclusionResultEntityId) o;
        return Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(inclusionRuleMask, that.inclusionRuleMask) &&
                Objects.equals(modeId, that.modeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, modeId, inclusionRuleMask);
    }
}
