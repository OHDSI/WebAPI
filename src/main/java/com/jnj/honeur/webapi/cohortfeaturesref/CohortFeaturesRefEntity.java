package com.jnj.honeur.webapi.cohortfeaturesref;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "CohortFeaturesRefEntity")
@Table(name = "cohort_features_ref")
@IdClass(CohortFeaturesRefEntityId.class)
public class CohortFeaturesRefEntity implements Serializable {

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "covariate_id")
    private Long covariateId;

    @Column(name = "covariate_name")
    private String covariateName;

    @Id
    @Column(name = "analysis_id")
    private Integer analysisId;

    @Id
    @Column(name = "concept_id")
    private Integer conceptId;

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public Long getCovariateId() {
        return covariateId;
    }

    public void setCovariateId(Long covariateId) {
        this.covariateId = covariateId;
    }

    public String getCovariateName() {
        return covariateName;
    }

    public void setCovariateName(String covariateName) {
        this.covariateName = covariateName;
    }

    public Integer getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Integer analysisId) {
        this.analysisId = analysisId;
    }

    public Integer getConceptId() {
        return conceptId;
    }

    public void setConceptId(Integer conceptId) {
        this.conceptId = conceptId;
    }
}
