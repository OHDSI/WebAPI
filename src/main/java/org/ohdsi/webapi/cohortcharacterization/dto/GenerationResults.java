package org.ohdsi.webapi.cohortcharacterization.dto;

import java.util.List;

public class GenerationResults {

    private List<CcResult> results;
    private Long totalCount;
    private Float prevalenceThreshold;

    public List<CcResult> getResults() {

        return results;
    }

    public void setResults(List<CcResult> results) {

        this.results = results;
    }

    public Long getTotalCount() {

        return totalCount;
    }

    public void setTotalCount(Long totalCount) {

        this.totalCount = totalCount;
    }

    public Float getPrevalenceThreshold() {

        return prevalenceThreshold;
    }

    public void setPrevalenceThreshold(Float prevalenceThreshold) {

        this.prevalenceThreshold = prevalenceThreshold;
    }
}
