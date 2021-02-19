package org.ohdsi.webapi.audittrail.listeners;

import com.odysseusinc.logging.event.FailedLogoutEvent;
import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailFailedLogoutListener implements ApplicationListener<FailedLogoutEvent> {

    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void onApplicationEvent(final FailedLogoutEvent event) {
        auditTrailService.logFailedLogout(event.getLogin());
    }
}
