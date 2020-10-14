package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.cohortcharacterization.report.Report;

import java.util.List;

public class GenerationResults {
    private List<Report> reports;
    private Float prevalenceThreshold;
    private Boolean showEmptyResults;
    private int count;

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
