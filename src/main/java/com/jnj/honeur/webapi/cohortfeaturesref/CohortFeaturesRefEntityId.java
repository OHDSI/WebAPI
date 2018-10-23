package com.jnj.honeur.webapi.cohortfeaturesref;

import java.io.Serializable;
import java.util.Objects;

public class CohortFeaturesRefEntityId implements Serializable {

    private Long cohortDefinitionId;
    private Long covariateId;
    private Integer analysisId;
    private Integer conceptId;

    public CohortFeaturesRefEntityId() {
    }

    public CohortFeaturesRefEntityId(Long cohortDefinitionId, Long covariateId, Integer analysisId,
                                     Integer conceptId) {

        this.cohortDefinitionId = cohortDefinitionId;
        this.covariateId = covariateId;
        this.analysisId = analysisId;
        this.conceptId = conceptId;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public Long getCovariateId() {
        return covariateId;
    }

    public Integer getAnalysisId() {
        return analysisId;
    }

    public Integer getConceptId() {
        return conceptId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortFeaturesRefEntityId that = (CohortFeaturesRefEntityId) o;
        return Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                Objects.equals(covariateId, that.covariateId) &&
                Objects.equals(analysisId, that.analysisId) &&
                Objects.equals(conceptId, that.conceptId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, covariateId, analysisId, conceptId);
    }
}
