package org.ohdsi.webapi.statistic.dto;

public class AccessTrendDto {
    private String endpointName;
    private String executionDate;
    private String userID;

    public AccessTrendDto(String endpointName, String  executionDate, String userID) {
        this.endpointName = endpointName;
        this.executionDate = executionDate;
        this.userID = userID;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public String  getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String  executionDate) {
        this.executionDate = executionDate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
