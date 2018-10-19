package org.ohdsi.webapi;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;

@Component
public class JobInvalidator {

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
            jobExplorer.getJobNames().forEach(
                    name -> jobExplorer.findRunningJobExecutions(name)
                            .forEach(job -> {
                                job.setStatus(BatchStatus.FAILED);
                                job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), "Invalidated by system"));
                                job.setEndTime(Calendar.getInstance().getTime());
                                jobRepository.update(job);
                            }));
            return null;
        });
    }
}
