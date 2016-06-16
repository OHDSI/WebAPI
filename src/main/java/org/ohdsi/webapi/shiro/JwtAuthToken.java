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
  
  private String jwt;
  
  public JwtAuthToken(String jwt) {    
    this.jwt = jwt;
  }
  
  public String getJwt() {
    return jwt;
  }

  @Override
  public Object getPrincipal() {
    return jwt == null ? null : TokenManager.getSubject(jwt);
  }

  @Override
  public Object getCredentials() {
    return "";
  }  
}
