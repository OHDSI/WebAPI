package org.ohdsi.webapi.shiro.filters.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.apache.shiro.authc.AuthenticationException;
import org.ohdsi.webapi.shiro.filters.AtlasAuthFilter;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.ohdsi.webapi.shiro.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AtlasJwtAuthFilter extends AtlasAuthFilter {

  private static final Logger logger = LoggerFactory.getLogger(AtlasJwtAuthFilter.class);

  @Override
  protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
    String jwt = TokenManager.extractToken(request);
    try {
      String subject = TokenManager.getSubject(jwt);
      return new JwtAuthToken(subject);
    } catch (JwtException e) {
      logger.warn("JWT validation failed: {}", e.getMessage());
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
        logger.debug("JWT authentication failed: {}", ae.getMessage());
        loggedIn = false;
      }
    }

    if (!loggedIn) {
        HttpServletResponse httpResponse = ServletBridge.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    return loggedIn;
  }

  protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
    return TokenManager.extractToken(request) != null;
  }
}
