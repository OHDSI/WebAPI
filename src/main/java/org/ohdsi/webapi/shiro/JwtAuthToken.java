package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 *
 * @author gennadiy.anisimov
 */
public class JwtAuthToken implements AuthenticationToken {
  
  private String subject;
  
  public JwtAuthToken(String subject) {    
    this.subject = subject;
  }
  
  @Override
  public Object getPrincipal() {
    return this.subject;
  }

  @Override
  public Object getCredentials() {
    return "";
  }  
}
