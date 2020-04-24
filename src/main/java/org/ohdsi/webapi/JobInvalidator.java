package org.ohdsi.webapi;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;

@Component
@DependsOn("flyway")
public class JobInvalidator {

    public static final String INVALIDATED_BY_SYSTEM_EXIT_MESSAGE = "Invalidated by system";

    private final JobRepository jobRepository;
    private final TransactionTemplate transactionTemplateRequiresNew;
    private final SearchableJobExecutionDao jobExecutionDao;

    @Autowired
    public JobInvalidator(JobRepository repository, TransactionTemplate transactionTemplateRequiresNew,
                          SearchableJobExecutionDao jobExecutionDao) {
        this.jobRepository = repository;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
        this.jobExecutionDao = jobExecutionDao;
    }

    @PostConstruct
    private void invalidateGenerations() {
        transactionTemplateRequiresNew.execute(s -> {
            jobExecutionDao.getRunningJobExecutions().forEach(this::invalidationJobExecution);
            return null;
        });
    }

    public void invalidateJobExecutionById(ExecutionEngineAnalysisStatus executionEngineAnalysisStatus) {
        JobExecution job = jobExecutionDao.getJobExecution(executionEngineAnalysisStatus.getExecutionEngineGeneration().getId());
        invalidationJobExecution(job);
    }

    public void invalidationJobExecution(JobExecution job) {
        job.setStatus(BatchStatus.FAILED);
        job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), INVALIDATED_BY_SYSTEM_EXIT_MESSAGE));
        job.setEndTime(Calendar.getInstance().getTime());
        jobRepository.update(job);
    }
}
