package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.cohortcharacterization.CcResultType;

public class CcResult {
    
    private Long id;
    private String faType;
    private String sourceKey;
    private Integer cohortId;
    private Integer analysisId;
    private String analysisName;
    private CcResultType resultType;


    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public String getFaType() {

        return faType;
    }

    public void setFaType(String faType) {

        this.faType = faType;
    }

    public String getSourceKey() {

        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {

        this.sourceKey = sourceKey;
    }

    public CcResultType getResultType() {

        return resultType;
    }

    public void setResultType(final CcResultType resultType) {

        this.resultType = resultType;
    }

    public Integer getCohortId() {

        return cohortId;
    }

    public void setCohortId(Integer cohortId) {

        this.cohortId = cohortId;
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

    public void setAnalysisName(final String analysisName) {

        this.analysisName = analysisName;
    }
}
