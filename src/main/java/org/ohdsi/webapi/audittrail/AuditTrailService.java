package org.ohdsi.webapi.audittrail;

public interface AuditTrailService {

    void log(AuditTrailEntry entry);
}
