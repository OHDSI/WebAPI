package org.ohdsi.webapi.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.ohdsi.webapi.arachne.logging.event.FailedLoginEvent;
import java.io.IOException;
import org.ohdsi.webapi.arachne.logging.event.SuccessLoginEvent;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.audittrail.events.AuditTrailLoginFailedEvent;
import org.ohdsi.webapi.audittrail.events.AuditTrailSessionCreatedEvent;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

public abstract class AuthenticatingPropagationFilter extends AuthenticatingFilter {

    public static final String HEADER_AUTH_ERROR = "x-auth-error";
    protected ApplicationEventPublisher eventPublisher;

    protected AuthenticatingPropagationFilter(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {

        request.setAttribute(AtlasSecurity.AUTH_FILTER_ATTRIBUTE, this.getClass().getName());
        String username = ((UsernamePasswordToken) token).getUsername();
        eventPublisher.publishEvent(new SuccessLoginEvent(this, username));

        final String sessionId = UUID.randomUUID().toString();
        request.setAttribute(Constants.SESSION_ID, sessionId);
        eventPublisher.publishEvent(new AuditTrailSessionCreatedEvent(this, username, sessionId, request.getRemoteHost()));

        return true;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        HttpServletResponse httpResponse = ServletBridge.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (e instanceof LockedAccountException) {
            httpResponse.setHeader(HEADER_AUTH_ERROR, e.getMessage());
        }
        String username = ((UsernamePasswordToken) token).getUsername();
        boolean result = super.onLoginFailure(token, e, request, response);
        eventPublisher.publishEvent(new FailedLoginEvent(this, username));
        eventPublisher.publishEvent(new AuditTrailLoginFailedEvent(this, username, request.getRemoteHost()));

        // Write response body and commit to prevent Spring MVC from processing
        try {
            httpResponse.setContentType("application/json");
            String errorMessage = e instanceof LockedAccountException ? e.getMessage() : "Invalid credentials";
            httpResponse.getWriter().write("{\"error\":\"" + errorMessage + "\"}");
            httpResponse.flushBuffer(); // Commit the response
        } catch (IOException ioe) {
            // Response may already be committed, ignore
        }

        return result;
    }
}
