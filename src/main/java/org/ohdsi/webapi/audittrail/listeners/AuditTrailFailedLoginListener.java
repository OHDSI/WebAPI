package org.ohdsi.webapi.audittrail.listeners;

import com.odysseusinc.logging.event.FailedLoginEvent;
import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailFailedLoginListener implements ApplicationListener<FailedLoginEvent> {

    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void onApplicationEvent(final FailedLoginEvent event) {
        auditTrailService.logFailedLogin(event.getLogin());
    }
}
