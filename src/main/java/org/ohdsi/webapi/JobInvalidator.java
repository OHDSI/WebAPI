package org.ohdsi.webapi;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;

@Component
public class JobInvalidator {

    @Autowired
    private JobExplorer jobExplorer;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @PostConstruct
    private void invalidateGenerations() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jobExplorer.getJobNames().forEach(
                        name -> jobExplorer.findRunningJobExecutions(name)
                                .forEach(job -> {
                                    job.setStatus(BatchStatus.FAILED);
                                    job.setExitStatus(new ExitStatus(ExitStatus.FAILED.getExitCode(), "Invalidated by system"));
                                    job.setEndTime(Calendar.getInstance().getTime());
                                    jobRepository.update(job);
                                }));
            }
        });
    }
}
