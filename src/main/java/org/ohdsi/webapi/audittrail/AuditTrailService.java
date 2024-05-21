package org.ohdsi.webapi.audittrail;

import org.springframework.batch.core.JobExecution;

public interface AuditTrailService {

    void logSuccessfulLogin(String login, String sessionId, String remoteHost);
    void logFailedLogin(String login, String remoteHost);

    void logSuccessfulLogout(String login);
    void logFailedLogout(String login);

    void logRestCall(AuditTrailEntry entry, boolean success);

    void logJobStart(JobExecution jobExecution);
    void logJobCompleted(JobExecution jobExecution);
    void logJobFailed(JobExecution jobExecution);
}
