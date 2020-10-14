package org.ohdsi.webapi.job;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class JobExecutionResource {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("startDate")
    private Date startDate;
    
    @JsonProperty("endDate")
    private Date endDate;
    
    @JsonProperty("exitStatus")
    private String exitStatus;
    
    @JsonProperty("executionId")
    private Long executionId;
    
    @JsonProperty("jobInstance")
    private JobInstanceResource jobInstanceResource;

    @JsonProperty("jobParameters")
    private Map<String, Object> jobParametersResource;

    @JsonProperty("ownerType")
    private JobOwnerType ownerType;
    
    public JobExecutionResource() {
        //needed for json deserialization
    }
    
    public JobExecutionResource(final JobInstanceResource jobInstanceResource, final Long executionId) {
        this.jobInstanceResource = jobInstanceResource;
        this.executionId = executionId;
    }
    
    /**
     * @return the status
     */
    public String getStatus() {
        return this.status;
    }
    
    /**
     * @param status the status to set
     */
    public void setStatus(final String status) {
        this.status = status;
    }
    
    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return this.startDate;
    }
    
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }
    
    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return this.endDate;
    }
    
    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }
    
    /**
     * @return the exitStatus
     */
    public String getExitStatus() {
        return this.exitStatus;
    }
    
    /**
     * @param exitStatus the exitStatus to set
     */
    public void setExitStatus(final String exitStatus) {
        this.exitStatus = exitStatus;
    }
    
    /**
     * @return the executionId
     */
    public Long getExecutionId() {
        return this.executionId;
    }
    
    /**
     * @return the jobInstanceResource
     */
    public JobInstanceResource getJobInstanceResource() {
        return this.jobInstanceResource;
    }
    
    /**
     * Auto generated method comment
     * 
     * @param map
     */
    public void setJobParametersResource(Map<String, Object> map) {
        this.jobParametersResource = map;
    }

    public JobOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(JobOwnerType ownerType) {
        this.ownerType = ownerType;
    }
}
