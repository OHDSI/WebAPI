package org.ohdsi.webapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ohdsi.webapi.batch.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Component
public class JobInvalidator {

    private static final Logger log = LoggerFactory.getLogger(JobInvalidator.class);

    public static final String INVALIDATED_BY_SYSTEM_EXIT_MESSAGE = "Invalidated by system";

    private final JobRepository jobRepository;
    private final TransactionTemplate transactionTemplateRequiresNew;
    private final SearchableJobExecutionDao jobExecutionDao;

    @Autowired
    public JobInvalidator(JobRepository repository, 
                          @Qualifier("transactionTemplateRequiresNew") TransactionTemplate transactionTemplateRequiresNew,
                          SearchableJobExecutionDao jobExecutionDao) {
        this.jobRepository = repository;
        this.transactionTemplateRequiresNew = transactionTemplateRequiresNew;
        this.jobExecutionDao = jobExecutionDao;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void invalidateGenerations() {
        transactionTemplateRequiresNew.execute(s -> {
            jobExecutionDao.getRunningJobExecutions().forEach(this::invalidationJobExecution);
            return null;
        });
    }


    public void invalidationJobExecution(JobExecution job) {
        job.setStatus(BatchStatus.FAILED);
        job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), INVALIDATED_BY_SYSTEM_EXIT_MESSAGE));
        job.setEndTime(java.time.LocalDateTime.now());
        jobRepository.update(job);
    }
}
