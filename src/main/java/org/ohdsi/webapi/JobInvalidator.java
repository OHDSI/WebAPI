package org.ohdsi.webapi;

import java.util.Calendar;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
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

    public static final String INVALIDATED_BY_SYSTEM_EXIT_CODE = "Invalidated by system";

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
        invalidationJobExecution(job);
    }

    private void invalidationJobExecution(JobExecution job) {
        job.setStatus(BatchStatus.FAILED);
        job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), INVALIDATED_BY_SYSTEM_EXIT_CODE));
        job.setEndTime(Calendar.getInstance().getTime());
        jobRepository.update(job);
    }
}
