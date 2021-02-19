package org.ohdsi.webapi.audittrail;

public interface AuditTrailService {


    void logSuccessfulLogin(String login);
    void logFailedLogin(String login);

    void logSuccessfulLogout(String login);
    void logFailedLogout(String login);

    void log(AuditTrailEntry entry);
}
