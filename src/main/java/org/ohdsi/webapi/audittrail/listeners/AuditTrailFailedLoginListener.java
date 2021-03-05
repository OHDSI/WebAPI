package org.ohdsi.webapi.audittrail.listeners;

import com.odysseusinc.logging.event.FailedLoginEvent;
import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.ohdsi.webapi.audittrail.events.AuditTrailLoginFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailFailedLoginListener implements ApplicationListener<AuditTrailLoginFailedEvent> {

    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void onApplicationEvent(final AuditTrailLoginFailedEvent event) {
        auditTrailService.logFailedLogin(event.getLogin(), event.getRemoteHost());
    }
}
