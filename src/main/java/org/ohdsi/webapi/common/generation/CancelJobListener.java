package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.Constants;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import java.util.Objects;

public class CancelJobListener extends JobExecutionListenerSupport {
  @Override
  public void afterJob(JobExecution jobExecution) {
    if (jobExecution.getStepExecutions().stream()
            .anyMatch(se -> Objects.equals(Constants.CANCELED, se.getExitStatus().getExitCode()))) {
      jobExecution.setExitStatus(new ExitStatus(Constants.CANCELED, "Canceled by user request"));
    }
  }
}
