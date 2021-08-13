package org.ohdsi.webapi.audittrail.listeners;

import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailJobListener implements JobExecutionListener {

    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        auditTrailService.logJobStart(jobExecution);
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED ) {
            auditTrailService.logJobCompleted(jobExecution);
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            auditTrailService.logJobFailed(jobExecution);
        }
    }
}
