package org.ohdsi.webapi.audittrail;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
@ConditionalOnProperty(value = "audit.trail.enabled", havingValue = "true")
public class AuditTrailAspect {

    @Autowired
    private AuditTrailService auditTrailService;

    @Pointcut("@annotation(javax.ws.rs.GET)")
    public void restGetPointcut() {
    }
    @Pointcut("@annotation(javax.ws.rs.POST)")
    public void restPostPointcut() {
    }
    @Pointcut("@annotation(javax.ws.rs.PUT)")
    public void restPutPointcut() {
    }
    @Pointcut("@annotation(javax.ws.rs.DELETE)")
    public void restDeletePointcut() {
    }
    @Pointcut("execution(public * org.ohdsi.webapi.service.IRAnalysisResource+.*(..))")
    public void irResource() {
    }

    @Pointcut("execution(public * org.ohdsi.webapi.job.NotificationController.*(..))")
    public void notificationsPointcut() {
    }
    @Pointcut("execution(public * org.ohdsi.webapi.executionengine.controller.ScriptExecutionController.*(..))")
    public void executionenginePointcut() {
    }
    @Pointcut("execution(public * org.ohdsi.webapi.service.VocabularyService.getInfo(..))")
    public void vocabularyServiceGetInfoPointcut() {
    }
    @Pointcut("execution(public * org.ohdsi.webapi.info.InfoService.getInfo(..))")
    public void webapiGetInfoPointcut() {
    }

    @Around("(restGetPointcut() || restPostPointcut() || restPutPointcut() || restDeletePointcut() || irResource())" +
            " && " +
            // exclude system calls
            "!notificationsPointcut() && " +
            "!executionenginePointcut() && " +
            "!vocabularyServiceGetInfoPointcut() && " +
            "!webapiGetInfoPointcut()")
    public Object auditLog(final ProceedingJoinPoint joinPoint) throws Throwable {
        final HttpServletRequest request = getHttpServletRequest();

        if (request == null) { // system call
            return joinPoint.proceed();
        }

        final AuditTrailEntry entry = new AuditTrailEntry();
        entry.setRemoteHost(request.getRemoteHost());

        final String token = TokenManager.extractToken(request);
        if (token != null) {
            try {
                final String user = TokenManager.getSubject(token);
                final String sessionId = (String) TokenManager.getBody(token).get(Constants.SESSION_ID);

                entry.setCurrentUser(user);
                entry.setSessionId(sessionId);
            } catch (final ExpiredJwtException | SignatureException e ) {
                // ignore expired or invalid token. let the application create a new one
            }
        }

        entry.setActionLocation(request.getHeader(Constants.Headers.ACTION_LOCATION));
        entry.setRequestMethod(request.getMethod());
        entry.setRequestUri(request.getRequestURI());
        entry.setQueryString(request.getQueryString());

        try {
            final Object returnedObject = joinPoint.proceed();
            entry.setReturnedObject(returnedObject);

            auditTrailService.logRestCall(entry, true);

            return returnedObject;
        } catch (final Throwable t) {
            auditTrailService.logRestCall(entry, false);
            throw t;
        }

    }

    private HttpServletRequest getHttpServletRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (final Exception e) {
            return null;
        }
    }
}