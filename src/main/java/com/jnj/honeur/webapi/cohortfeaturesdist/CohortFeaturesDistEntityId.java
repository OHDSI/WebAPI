package com.jnj.honeur.webapi.cohortfeaturesdist;

import java.io.Serializable;
import java.util.Objects;

public class CohortFeaturesDistEntityId implements Serializable {

    private Long cohortDefinitionId;
    private Long covariateId;

    public CohortFeaturesDistEntityId() {
    }

    public CohortFeaturesDistEntityId(Long cohortDefinitionId, Long covariateId) {
        this.cohortDefinitionId = cohortDefinitionId;
        this.covariateId = covariateId;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public Long getCovariateId() {
        return covariateId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortFeaturesDistEntityId that = (CohortFeaturesDistEntityId) o;
        return Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(covariateId, that.covariateId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, covariateId);
    }
}
