package org.ohdsi.webapi.statistic.dto;

import java.util.ArrayList;
import java.util.List;

public class SourceExecutionsDto {
    private List<SourceExecutionDto> executions = new ArrayList<>();

    public SourceExecutionsDto(List<SourceExecutionDto> executions) {
        this.executions = executions;
    }

    public List<SourceExecutionDto> getExecutions() {
        return executions;
    }
}
