package org.ohdsi.webapi.job;

import org.springframework.batch.core.JobExecution;

public class JobExecutionInfo {
    private JobExecution jobExecution;
    private JobOwnerType ownerType;

    public JobExecutionInfo(JobExecution jobExecution, JobOwnerType ownerType) {
        this.jobExecution = jobExecution;
        this.ownerType = ownerType;
    }

    public JobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public JobOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(JobOwnerType ownerType) {
        this.ownerType = ownerType;
    }
}
