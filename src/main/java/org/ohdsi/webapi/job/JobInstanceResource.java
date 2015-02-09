package org.ohdsi.webapi.job;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class JobInstanceResource {
    
    @JsonProperty("instanceId")
    private Long instanceId;
    
    @JsonProperty("name")
    private String name;
    
    public JobInstanceResource() {
        //needed for json deserialization
    }
    
    public JobInstanceResource(final Long instanceId) {
        this.instanceId = instanceId;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    
    /**
     * @return the id
     */
    public Long getInstanceId() {
        return this.instanceId;
    }
}
