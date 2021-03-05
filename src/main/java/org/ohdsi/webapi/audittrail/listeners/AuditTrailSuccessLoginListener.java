package org.ohdsi.webapi.audittrail.listeners;

import com.odysseusinc.logging.event.SuccessLoginEvent;
import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.ohdsi.webapi.audittrail.events.AuditTrailSessionCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailSuccessLoginListener implements ApplicationListener<AuditTrailSessionCreatedEvent> {
    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void onApplicationEvent(final AuditTrailSessionCreatedEvent event) {
        auditTrailService.logSuccessfulLogin(event.getLogin(), event.getSessionId(), event.getRemoteHost());
    }
}
