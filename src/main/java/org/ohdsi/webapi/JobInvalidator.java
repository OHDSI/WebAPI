package org.ohdsi.webapi;

import jakarta.annotation.PostConstruct;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@DependsOn("flyway")
public class JobInvalidator {

    private static final Logger log = LoggerFactory.getLogger(JobInvalidator.class);

    public static final String INVALIDATED_BY_SYSTEM_EXIT_MESSAGE = "Invalidated by system";

    private final JobRepository jobRepository;
    private final TransactionTemplate transactionTemplateRequiresNew;
    private final JobExplorer jobExplorer;

    public JobInvalidator(JobRepository jobRepository,
                          TransactionTemplate transactionTemplateRequiresNew,
                          JobExplorer jobExplorer) {
        this.jobRepository = jobRepository;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
        this.jobExplorer = jobExplorer;
    }

    /**
     * Invalidates all running job executions during initialization.
     */
    @PostConstruct
    private void invalidateGenerations() {
        transactionTemplateRequiresNew.execute(status -> {
            // Retrieve all running job executions for all job names
            jobExplorer.getJobNames().forEach(jobName -> {
                Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions(jobName);
                runningJobs.forEach(this::invalidateJobExecution);
            });
            return null;
        });
    }

    /**
     * Invalidates a specific job execution based on an analysis status.
     *
     * @param executionEngineAnalysisStatus The status containing the job execution ID.
     */
    @Transactional
    public void invalidateJobExecutionById(ExecutionEngineAnalysisStatus executionEngineAnalysisStatus) {
        Long executionId = executionEngineAnalysisStatus.getExecutionEngineGeneration().getId();
        JobExecution jobExecution = jobExplorer.getJobExecution(executionId);

        if (jobExecution == null || jobExecution.getJobId() == null) {
            log.error("Cannot invalidate job. No job found for execution-engine-analysis-status with id = {}",
                      executionEngineAnalysisStatus.getId());
            return;
        }

        invalidateJobExecution(jobExecution);
    }

    /**
     * Marks a job execution as failed and updates its status in the repository.
     *
     * @param jobExecution The job execution to invalidate.
     */
    public void invalidateJobExecution(JobExecution jobExecution) {
        jobExecution.setStatus(BatchStatus.FAILED);
        jobExecution.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), INVALIDATED_BY_SYSTEM_EXIT_MESSAGE));
        jobExecution.setEndTime(LocalDateTime.now());
        jobRepository.update(jobExecution);
        log.info("Job execution with ID {} has been invalidated.", jobExecution.getId());
    }
}