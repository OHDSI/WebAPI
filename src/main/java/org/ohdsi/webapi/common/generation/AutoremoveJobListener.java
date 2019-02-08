package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.service.JobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

public class AutoremoveJobListener extends JobExecutionListenerSupport {

  private final JobService jobService;

  public AutoremoveJobListener(JobService jobService) {
    this.jobService = jobService;
  }

  @Override
  public void afterJob(JobExecution jobExecution) {

    jobService.removeJob(jobExecution.getJobId());
  }
}
