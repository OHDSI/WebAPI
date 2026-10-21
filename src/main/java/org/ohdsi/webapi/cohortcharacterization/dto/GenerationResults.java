package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.cohortcharacterization.report.Report;

import java.util.List;
import java.util.Map;

public class GenerationResults {
    private List<Report> reports;
    private Float prevalenceThreshold;
    private Boolean showEmptyResults;
    private int count;
    // cohortId -> strataId -> number of people; strata "0" is the overall cohort. Missing entries are unknown.
    private Map<String, Map<String, Long>> strataCounts;

    public Map<String, Map<String, Long>> getStrataCounts() {
        return strataCounts;
    }

    public void setStrataCounts(Map<String, Map<String, Long>> strataCounts) {
        this.strataCounts = strataCounts;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public Float getPrevalenceThreshold() {

        return prevalenceThreshold;
    }

    public void setPrevalenceThreshold(Float prevalenceThreshold) {

        this.prevalenceThreshold = prevalenceThreshold;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public Boolean getShowEmptyResults() {
        return showEmptyResults;
    }

    public void setShowEmptyResults(Boolean showEmptyResults) {
        this.showEmptyResults = showEmptyResults;
    }
}
