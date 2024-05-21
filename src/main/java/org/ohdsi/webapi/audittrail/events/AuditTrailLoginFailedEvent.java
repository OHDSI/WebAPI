package org.ohdsi.webapi.audittrail.events;

import org.springframework.context.ApplicationEvent;

public class AuditTrailLoginFailedEvent extends ApplicationEvent {
    private final String login;
    private final String remoteHost;

    public AuditTrailLoginFailedEvent(final Object source,
                                      final String login,
                                      final String remoteHost) {
        super(source);
        this.login = login;
        this.remoteHost = remoteHost;
    }

    public String getLogin() {
        return this.login;
    }

    public String getRemoteHost() {
        return remoteHost;
    }
}
