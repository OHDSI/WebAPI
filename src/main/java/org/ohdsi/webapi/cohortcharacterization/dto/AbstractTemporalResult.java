package org.ohdsi.webapi.cohortcharacterization.dto;

public abstract class AbstractTemporalResult {
    protected Integer analysisId;
    protected String analysisName;
    protected Long covariateId;
    protected String covariateName;
    protected Integer strataId;
    protected String strataName;
    protected Integer conceptId;
    protected Integer cohortId;
    protected Long count;
    protected Double avg;

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

    public Integer getStrataId() {
        return strataId;
    }

    public void setStrataId(Integer strataId) {
        this.strataId = strataId;
    }

    public String getStrataName() {
        return strataName;
    }

    public void setStrataName(String strataName) {
        this.strataName = strataName;
    }

    public Integer getConceptId() {
        return conceptId;
    }

    public void setConceptId(Integer conceptId) {
        this.conceptId = conceptId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Integer getCohortId() {
        return cohortId;
    }

    public void setCohortId(Integer cohortId) {
        this.cohortId = cohortId;
    }
}
