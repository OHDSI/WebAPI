package org.ohdsi.webapi.statistic.dto;

public class SourceExecutionDto {
    private String sourceName;
    private String executionName;
    private String executionDate;
    private String userId;

    public SourceExecutionDto(String sourceName, String executionName, String executionDate, String userId) {
        this.sourceName = sourceName;
        this.executionName = executionName;
        this.executionDate = executionDate;
        this.userId = userId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getExecutionName() {
        return executionName;
    }

    public void setExecutionName(String executionName) {
        this.executionName = executionName;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
