package org.ohdsi.webapi.audittrail;

import java.util.Locale;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.security.authc.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Component
@Aspect
@ConditionalOnProperty(value = "audit.trail.enabled", havingValue = "true")
public class AuditTrailAspect {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  @Autowired
  private AuditTrailService auditTrailService;

  @Autowired
  private JwtDecoder decoder;

  @Pointcut("@annotation(jakarta.ws.rs.GET)")
  public void restGetPointcut() {
  }

  @Pointcut("@annotation(jakarta.ws.rs.POST)")
  public void restPostPointcut() {
  }

  @Pointcut("@annotation(jakarta.ws.rs.PUT)")
  public void restPutPointcut() {
  }

  @Pointcut("@annotation(jakarta.ws.rs.DELETE)")
  public void restDeletePointcut() {
  }

  @Pointcut("execution(public * org.ohdsi.webapi.service.IRAnalysisResource+.*(..))")
  public void irResource() {
  }

  @Pointcut("execution(public * org.ohdsi.webapi.job.NotificationController.*(..))")
  public void notificationsPointcut() {
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
      "!vocabularyServiceGetInfoPointcut() && " +
      "!webapiGetInfoPointcut()")
  public Object auditLog(final ProceedingJoinPoint joinPoint) throws Throwable {
    final HttpServletRequest request = getHttpServletRequest();

    if (request == null) { // system call
      return joinPoint.proceed();
    }

    final AuditTrailEntry entry = new AuditTrailEntry();
    entry.setRemoteHost(request.getRemoteHost());

    final String token = extractToken(request);
    if (token != null) {
      try {
        Jwt jwt = decoder.decode(token);
        final String user = jwt.getSubject();
        final String sessionId = jwt.getClaim(Constants.SESSION_ID).toString();

        entry.setCurrentUser(user);
        entry.setSessionId(sessionId);
      } catch (final JwtException e) {
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

  private static String extractToken(HttpServletRequest request) {

    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (header == null || header.isEmpty()) {
      return null;
    }

    if (!header.toLowerCase(Locale.ENGLISH).startsWith("bearer")) {
      return null;
    }

    String[] headerParts = header.split(" ");
    if (headerParts.length != 2) {
      return null;
    }

    return headerParts[1];
  }
}