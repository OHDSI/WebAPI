package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.webapi.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExecutionResultRequest {

    @JsonProperty("cohortIds")
    private List<Integer> cohortIds;

    @JsonProperty("analysisIds")
    private List<Integer> analysisIds;

    @JsonProperty("domainIds")
    private List<String> domainIds;

    @JsonProperty("thresholdValuePct")
    private Float thresholdValuePct;

    @JsonProperty("isSummary")
    private Boolean isSummary;

    @JsonProperty("showEmptyResults")
    private Boolean isShowEmptyResults = false;

    public List<Integer> getCohortIds() {
        if(cohortIds == null) {
            return Collections.emptyList();
        }
        return cohortIds;
    }

    public void setCohortIds(List<Integer> cohortIds) {
        this.cohortIds = cohortIds;
    }

    public List<Integer> getAnalysisIds() {
        if(analysisIds == null) {
            return Collections.emptyList();
        }
        return analysisIds;
    }

    public void setAnalysisIds(List<Integer> analysisIds) {
        this.analysisIds = analysisIds;
    }

    public List<String> getDomainIds() {
        if(domainIds == null) {
            return Collections.emptyList();
        }
        return domainIds;
    }

    public void setDomainIds(List<String> domainIds) {
        this.domainIds = domainIds;
    }

    public boolean isFilterUsed() {
        return !(getAnalysisIds().isEmpty() && getDomainIds().isEmpty() && getCohortIds().isEmpty());
    }

    public Float getThresholdValuePct() {
        return thresholdValuePct != null ? thresholdValuePct : Constants.DEFAULT_THRESHOLD;
    }

    public void setThresholdValuePct(Float thresholdValuePct) {
        this.thresholdValuePct = thresholdValuePct;
    }

    public Boolean isSummary() {
        return isSummary;
    }

    public void setSummary(Boolean summary) {
        isSummary = summary;
    }

    public Boolean getShowEmptyResults() {
        return Boolean.TRUE.equals(isShowEmptyResults);
    }

    public void setShowEmptyResults(Boolean showEmptyResults) {
        isShowEmptyResults = showEmptyResults;
    }
}
