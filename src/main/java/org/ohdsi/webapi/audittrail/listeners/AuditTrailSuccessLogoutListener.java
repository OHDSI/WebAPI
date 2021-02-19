package org.ohdsi.webapi.audittrail.listeners;

import com.odysseusinc.logging.event.SuccessLogoutEvent;
import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailSuccessLogoutListener implements ApplicationListener<SuccessLogoutEvent> {

    @Autowired
    private AuditTrailService auditTrailService;

    @Override
    public void onApplicationEvent(final SuccessLogoutEvent event) {
        auditTrailService.logSuccessfulLogout(event.getLogin());
    }
}
