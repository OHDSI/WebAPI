package org.ohdsi.webapi.service.auditlogger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.CodeSignature;
import org.ohdsi.webapi.shiro.management.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.validation.constraints.NotNull;
import org.ohdsi.webapi.shiro.PermissionManager;

@Component
@Aspect
public class AuditAspect {
    @Autowired
    private Security securityManager;

    @Autowired
    protected PermissionManager authorizer;

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Pointcut("within(org.ohdsi.webapi..*)")
    public void componentBean() {
        // Pointer for logging purposes
    }

    @Pointcut("@annotation(javax.ws.rs.Path)")
    public void pathMethods() {
        // Pointer for logging purposes
    }

    @Before("componentBean() && pathMethods()")
    public void logAuditedMethods(@NotNull JoinPoint jp) {
        CodeSignature methodSignature = (CodeSignature) jp.getSignature();
        AuditLogger.log(securityManager.getSubject(), jp.getSignature().toShortString(), jp.getArgs(), methodSignature.getParameterNames());
    }
}
