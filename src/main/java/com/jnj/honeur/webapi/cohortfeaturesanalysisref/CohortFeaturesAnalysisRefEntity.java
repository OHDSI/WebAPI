package com.jnj.honeur.webapi.cohortfeaturesanalysisref;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "CohortFeaturesAnalysisRefEntity")
@Table(name = "cohort_features_analysis_ref")
@IdClass(CohortFeaturesAnalysisRefEntityId.class)
public class CohortFeaturesAnalysisRefEntity implements Serializable {

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "analysis_id")
    private Integer analysisId;

    @Column(name = "analysis_name")
    private String analysisName;

    @Id
    @Column(name = "domain_id")
    private String domainId;

    @Column(name = "start_day")
    private Integer startDay;

    @Column(name = "end_day")
    private Integer endDay;

    @Column(name = "is_binary")
    private Boolean isBinary;

    @Column(name = "missing_means_zero")
    private Boolean missingMeansZero;

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public Integer getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Integer analysisId) {
        this.analysisId = analysisId;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    public Integer getEndDay() {
        return endDay;
    }

    public void setEndDay(Integer endDay) {
        this.endDay = endDay;
    }

    public Boolean getBinary() {
        return isBinary;
    }

    public void setBinary(Boolean binary) {
        isBinary = binary;
    }

    public Boolean getMissingMeansZero() {
        return missingMeansZero;
    }

    public void setMissingMeansZero(Boolean missingMeansZero) {
        this.missingMeansZero = missingMeansZero;
    }
}
