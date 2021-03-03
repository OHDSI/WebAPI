package org.ohdsi.webapi.audittrail;

import org.springframework.batch.core.JobExecution;

public interface AuditTrailService {

    void logSuccessfulLogin(String login);
    void logFailedLogin(String login);

    void logSuccessfulLogout(String login);
    void logFailedLogout(String login);

    void logRestCall(AuditTrailEntry entry, boolean success);

    void logJobStart(JobExecution jobExecution);
    void logJobCompleted(JobExecution jobExecution);
    void logJobFailed(JobExecution jobExecution);
}
