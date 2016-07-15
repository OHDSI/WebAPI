/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
