package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.JwtException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public final class JwtAuthFilter extends org.apache.shiro.web.filter.authc.AuthenticatingFilter {

  @Override
  protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
    String jwt = TokenManager.extractToken(request);
    String subject;
    try {
      subject = TokenManager.getSubject(jwt);
    } catch (JwtException e) {
      throw new AuthenticationException(e);
    }

    return new JwtAuthToken(subject);
  }

  @Override
  protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
    boolean loggedIn = false;

    if (isLoginAttempt(request, response)) {
      try {
        loggedIn = executeLogin(request, response);
      }
      catch(AuthenticationException ae) {
        loggedIn = false;
      }
    }

    if (!loggedIn) {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    return loggedIn;
  }

  protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
    return TokenManager.extractToken(request) != null;
  }
}
