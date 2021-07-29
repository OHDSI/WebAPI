package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.ExecutionInfo;

import java.util.ArrayList;
import java.util.List;

public class AnalysisInfoDTO {

    private ExecutionInfo executionInfo;
    private List<AnalysisReport.Summary> summaryList = new ArrayList<>();

    public ExecutionInfo getExecutionInfo() {
        return executionInfo;
    }

    public void setExecutionInfo(ExecutionInfo executionInfo) {
        this.executionInfo = executionInfo;
    }

    public List<AnalysisReport.Summary> getSummaryList() {
        return summaryList;
    }

    public void setSummaryList(List<AnalysisReport.Summary> summaryList) {
        this.summaryList = summaryList;
    }
}
