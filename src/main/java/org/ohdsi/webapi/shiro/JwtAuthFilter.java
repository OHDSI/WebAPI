/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.shiro;

import java.util.Locale;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.util.WebUtils;

/**
 *
 * @author gennadiy.anisimov
 */
public final class JwtAuthFilter extends org.apache.shiro.web.filter.authc.AuthenticatingFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  
  @Override
  protected JwtAuthToken createToken(ServletRequest request, ServletResponse response) throws Exception {
    String authHeader = getAuthHeader(request);
    String jwt = authHeader.split(" ")[1];
    
    return new JwtAuthToken(jwt);
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
    String authHeader = getAuthHeader(request);    
    if (authHeader == null) 
      return false;
    
    return 
            authHeader.toLowerCase(Locale.ENGLISH).startsWith("Bearer".toLowerCase(Locale.ENGLISH)) 
            && 
            authHeader.split(" ").length == 2;
  }
  
  private String getAuthHeader(ServletRequest request) {
    HttpServletRequest httpRequest = WebUtils.toHttp(request);
    return httpRequest.getHeader(AUTHORIZATION_HEADER);    
  }
}
