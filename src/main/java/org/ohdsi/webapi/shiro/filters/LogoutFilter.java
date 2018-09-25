package org.ohdsi.webapi.shiro.filters;

import com.odysseusinc.logging.event.FailedLogoffEvent;
import com.odysseusinc.logging.event.SuccessLogoffEvent;
import io.jsonwebtoken.JwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.TokenManager;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gennadiy.anisimov
 */
public class LogoutFilter extends AdviceFilter {

  private ApplicationEventPublisher eventPublisher;

  private final Log log = LogFactory.getLog(getClass());

  public LogoutFilter(ApplicationEventPublisher eventPublisher){
    this.eventPublisher = eventPublisher;
  }

  @Override
  protected boolean preHandle(ServletRequest request, ServletResponse response) {

    HttpServletResponse httpResponse = WebUtils.toHttp(response);
    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String jwt = TokenManager.extractToken(request);
    String principal;
    try {
        principal = TokenManager.getSubject(jwt);
    } catch (JwtException e) {
        throw new AuthenticationException(e);
    }
    if (TokenManager.invalidate(jwt)) {
        httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    Subject subject = SecurityUtils.getSubject();
    try {
        subject.logout();
        eventPublisher.publishEvent(new SuccessLogoffEvent(this, principal));
    } catch (SessionException ise) {
        log.debug("Encountered session exception during logout. This can be generally safely ignored.", ise);
        eventPublisher.publishEvent(new FailedLogoffEvent(this, principal));
    }
    return false;
  }
}
