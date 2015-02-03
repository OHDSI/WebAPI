package org.ohdsi.webapi.job;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class JobResource {
    
    @JsonProperty("jobInstanceId")
    private long id;
    
    @JsonProperty("jobExecutionId")
    private long executionId;
    
    public JobResource(long id, long executionId) {
        this.id = id;
        this.executionId = executionId;
    }
}
