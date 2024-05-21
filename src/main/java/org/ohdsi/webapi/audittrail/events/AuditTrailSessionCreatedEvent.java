package org.ohdsi.webapi.audittrail.events;

import com.odysseusinc.logging.LogLevel;
import org.springframework.context.ApplicationEvent;

public class AuditTrailSessionCreatedEvent extends ApplicationEvent {
    private final String login;
    private final String sessionId;
    private final String remoteHost;

    public AuditTrailSessionCreatedEvent(final Object source,
                                         final String login,
                                         final String sessionId,
                                         final String remoteHost) {
        super(source);
        this.login = login;
        this.sessionId = sessionId;
        this.remoteHost = remoteHost;
    }

    public String getLogin() {
        return this.login;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteHost() {
        return remoteHost;
    }
}
