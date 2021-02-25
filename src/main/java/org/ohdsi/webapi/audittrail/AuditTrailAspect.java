package org.ohdsi.webapi.audittrail;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
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

    @Autowired
    private PermissionManager permissionManager;

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

    @Around("(restGetPointcut() || restPostPointcut() || restPutPointcut() || restDeletePointcut() || irResource())" +
            " && " +
            // exclude system calls
            "!notificationsPointcut() && " +
            "!executionenginePointcut()")
    public Object auditLog(final ProceedingJoinPoint joinPoint) throws Throwable {
        final HttpServletRequest request = getHttpServletRequest();

        if (request == null) { // system call
            return joinPoint.proceed();
        }

        final AuditTrailEntry entry = new AuditTrailEntry();
        entry.setCurrentUser(getCurrentUser());
        entry.setActionLocation(request.getHeader("Action-Location"));
        entry.setRequestMethod(request.getMethod());
        entry.setRequestUri(request.getRequestURI());
        entry.setQueryString(request.getQueryString());

        final Object returnedObject = joinPoint.proceed();
        entry.setReturnedObject(returnedObject);

        auditTrailService.logRestCall(entry);

        return returnedObject;
    }

    private HttpServletRequest getHttpServletRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (final Exception e) {
            return null;
        }
    }

    private UserEntity getCurrentUser() {
        try {
            return permissionManager.getCurrentUser();
        } catch (final Exception e) {
            return null;
        }
    }
}