package org.ohdsi.webapi.job;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;

/**
 *
 */
public final class JobUtils {
    
    public static JobInstanceResource toJobInstanceResource(final JobInstance jobInstance) {
        final JobInstanceResource job = new JobInstanceResource(jobInstance.getId());
        job.setName(jobInstance.getJobName());
        return job;
    }
    
    public static JobExecutionResource toJobExecutionResource(final JobExecution jobExecution) {
        final JobExecutionResource execution = new JobExecutionResource(
                toJobInstanceResource(jobExecution.getJobInstance()), jobExecution.getId());
        execution.setStatus(jobExecution.getStatus().name());
        execution.setStartDate(jobExecution.getStartTime());
        execution.setEndDate(jobExecution.getEndTime());
        execution.setExitStatus(jobExecution.getExitStatus().getExitCode());
        return execution;
    }
}
