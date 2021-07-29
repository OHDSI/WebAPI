package org.ohdsi.webapi.shiro.filters.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.filters.AtlasAuthFilter;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.ohdsi.webapi.shiro.TokenManager;

public final class AtlasJwtAuthFilter extends AtlasAuthFilter {

  @Override
  protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
    String jwt = TokenManager.extractToken(request);
    try {
      String subject = TokenManager.getSubject(jwt);
      return new JwtAuthToken(subject);
    } catch (JwtException e) {
      throw new AuthenticationException(e);
    }
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
