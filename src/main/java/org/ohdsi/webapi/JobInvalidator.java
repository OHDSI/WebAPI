package org.ohdsi.webapi;

import java.util.Calendar;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class JobInvalidator {

    private static final Logger log = LoggerFactory.getLogger(JobInvalidator.class);

    public static final String INVALIDATED_BY_SYSTEM_EXIT_MESSAGE = "Invalidated by system";

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final TransactionTemplate transactionTemplateRequiresNew;

    @Autowired
    public JobInvalidator(JobExplorer explorer, JobRepository repository, TransactionTemplate transactionTemplateRequiresNew) {

        this.jobExplorer = explorer;
        this.jobRepository = repository;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
    }

    @PostConstruct
    private void invalidateGenerations() {
        transactionTemplateRequiresNew.execute(s -> {
            jobExplorer.getJobNames().forEach(name ->
                    jobExplorer
                            .findRunningJobExecutions(name)
                            .forEach(this::invalidationJobExecution));
            return null;
        });
    }

    public void invalidateJobExecutionById(ExecutionEngineAnalysisStatus executionEngineAnalysisStatus) {
        JobExecution job = jobExplorer.getJobExecution(executionEngineAnalysisStatus.getExecutionEngineGeneration().getId());
        if (job == null || job.getJobId() == null) {
            log.error("Cannot validate job. There is no job for execution-engine-analysis-status with id = {}", executionEngineAnalysisStatus.getId());
            return;
        }
        invalidationJobExecution(job);

    }

    public void invalidationJobExecution(JobExecution job) {
        job.setStatus(BatchStatus.FAILED);
        job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), INVALIDATED_BY_SYSTEM_EXIT_MESSAGE));
        job.setEndTime(Calendar.getInstance().getTime());
        jobRepository.update(job);
    }
}
