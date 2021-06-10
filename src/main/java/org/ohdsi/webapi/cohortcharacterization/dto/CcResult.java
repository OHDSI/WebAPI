package org.ohdsi.webapi.cohortcharacterization.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CcPrevalenceStat.class),
        @JsonSubTypes.Type(value = CcDistributionStat.class)
})
public abstract class CcResult {
    
    private Long id;
    private String faType;
    private String sourceKey;
    private Integer cohortId;
    private Integer analysisId;
    private String analysisName;
    private CcResultType resultType;
    private Long strataId;
    private String strataName;

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

    public Long getStrataId() {
        return strataId;
    }

    public void setStrataId(Long strataId) {
        this.strataId = strataId;
    }

    public String getStrataName() {
        return strataName;
    }

    public void setStrataName(String strataName) {
        this.strataName = strataName;
    }
}
