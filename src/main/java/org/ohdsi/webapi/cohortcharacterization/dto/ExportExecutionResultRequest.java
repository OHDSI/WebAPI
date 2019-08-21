package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExportExecutionResultRequest extends ExecutionResultRequest{
    @JsonProperty("isComparative")
    private Boolean isComparative;

    public Boolean isComparative() {
        return isComparative;
    }

    public void setComparative(Boolean comparative) {
        isComparative = comparative;
    }
}
