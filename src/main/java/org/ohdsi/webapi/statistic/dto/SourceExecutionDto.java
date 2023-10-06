package org.ohdsi.webapi.statistic.dto;

import java.time.Instant;
import java.time.LocalDate;

public class SourceExecutionDto {
    private String sourceName;
    private String executionName;
    private LocalDate executionDate;

    public SourceExecutionDto(String sourceName, String executionName, LocalDate executionDate) {
        this.sourceName = sourceName;
        this.executionName = executionName;
        this.executionDate = executionDate;
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

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }
}
