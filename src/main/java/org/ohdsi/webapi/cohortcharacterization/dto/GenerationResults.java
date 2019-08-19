package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.cohortcharacterization.CcServiceImpl;

import java.util.List;

public class GenerationResults {
    private List<CcServiceImpl.Report> reports;
    private Float prevalenceThreshold;
    private int count;

    public List<CcServiceImpl.Report> getReports() {
        return reports;
    }

    public void setReports(List<CcServiceImpl.Report> reports) {
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
}
