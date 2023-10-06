package org.ohdsi.webapi.statistic.dto;

import java.time.LocalDate;

public class AccessTrendDto {
    private String endpointName;
    private LocalDate executionDate;

    public AccessTrendDto(String endpointName, LocalDate executionDate) {
        this.endpointName = endpointName;
        this.executionDate = executionDate;
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
}
