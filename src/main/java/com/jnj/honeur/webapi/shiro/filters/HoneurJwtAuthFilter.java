package com.jnj.honeur.webapi.shiro.filters;

import com.jnj.honeur.webapi.shiro.HoneurTokenManager;
import io.jsonwebtoken.JwtException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.filters.AtlasAuthFilter;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author gennadiy.anisimov
 */
public final class HoneurJwtAuthFilter extends AtlasAuthFilter {

  @Override
  protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
    String jwt = HoneurTokenManager.extractToken(request);
    String subject;
    try {
      subject = HoneurTokenManager.getSubject(jwt);
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
    return HoneurTokenManager.extractToken(request) != null;
  }
}
