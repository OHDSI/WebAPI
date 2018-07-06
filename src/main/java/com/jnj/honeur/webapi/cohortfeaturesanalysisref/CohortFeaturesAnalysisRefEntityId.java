package com.jnj.honeur.webapi.cohortfeaturesanalysisref;

import java.io.Serializable;
import java.util.Objects;

public class CohortFeaturesAnalysisRefEntityId implements Serializable {

    private Long cohortDefinitionId;
    private Integer analysisId;
    private String domainId;

    public CohortFeaturesAnalysisRefEntityId() {
    }

    public CohortFeaturesAnalysisRefEntityId(Long cohortDefinitionId, Integer analysisId, String domainId) {
        this.cohortDefinitionId = cohortDefinitionId;
        this.analysisId = analysisId;
        this.domainId = domainId;
    }

    public Long getCohortDefinitionId() {

        return cohortDefinitionId;
    }

    public Integer getAnalysisId() {
        return analysisId;
    }

    public String getDomainId() {
        return domainId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortFeaturesAnalysisRefEntityId that = (CohortFeaturesAnalysisRefEntityId) o;
        return Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(analysisId, that.analysisId) &&
                Objects.equals(domainId, that.domainId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, analysisId, domainId);
    }
}
