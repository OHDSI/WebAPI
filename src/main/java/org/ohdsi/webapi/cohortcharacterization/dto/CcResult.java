package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.cohortcharacterization.CcResultType;

public class CcResult {
    
    private Long id;
    private String analysisName;
    private CcResultType resultType;


    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public CcResultType getResultType() {

        return resultType;
    }

    public void setResultType(final CcResultType resultType) {

        this.resultType = resultType;
    }

    public String getAnalysisName() {

        return analysisName;
    }

    public void setAnalysisName(final String analysisName) {

        this.analysisName = analysisName;
    }
}
