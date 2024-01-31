package org.ohdsi.webapi.statistic.dto;

import java.time.LocalDate;

public class AccessTrendDto {
    private String endpointName;
    private LocalDate executionDate;
    private String userID;

    public AccessTrendDto(String endpointName, LocalDate executionDate, String userID) {
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

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
