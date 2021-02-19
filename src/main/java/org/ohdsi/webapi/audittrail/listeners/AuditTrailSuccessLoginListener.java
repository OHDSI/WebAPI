package org.ohdsi.webapi.audittrail.listeners;

import com.odysseusinc.logging.event.SuccessLoginEvent;
import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailSuccessLoginListener implements ApplicationListener<SuccessLoginEvent> {
    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void onApplicationEvent(final SuccessLoginEvent event) {
        auditTrailService.logSuccessfulLogin(event.getLogin());
    }
}
