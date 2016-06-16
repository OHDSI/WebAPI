/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.shiro;

import io.jsonwebtoken.JwtException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author gennadiy.anisimov
 */
public class JwtAuthRealm extends AuthorizingRealm {
  
  public JwtAuthRealm() {
    setAuthenticationTokenClass(org.ohdsi.webapi.shiro.JwtAuthToken.class);
  }

  @Autowired
  private SimpleAuthorizer authorizer;
    
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    final String login = (String) principals.getPrimaryPrincipal();
    return authorizer.getAuthorizationInfo(login);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {
    final JwtAuthToken token = (JwtAuthToken)at;
    final String login;
    
    try {
      login = (String)token.getPrincipal();
    } 
    catch (JwtException e) {
      throw new CredentialsException("Invalid JWT token");
    }
    
    return new SimpleAuthenticationInfo(login, "", getName());
  }  
}
