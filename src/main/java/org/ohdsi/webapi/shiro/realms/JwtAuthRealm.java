package org.ohdsi.webapi.shiro.realms;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;

/**
 *
 * @author gennadiy.anisimov
 */
public class JwtAuthRealm extends AuthorizingRealm {
  
  private final PermissionManager authorizer;

  public JwtAuthRealm(PermissionManager authorizer) {
    this.authorizer = authorizer;
  }

  @Override
  public boolean supports(AuthenticationToken token) {

    return token != null && token.getClass() == JwtAuthToken.class;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    final String login = (String) principals.getPrimaryPrincipal();
    return authorizer.getAuthorizationInfo(login);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {
    return new SimpleAuthenticationInfo(at.getPrincipal(), "", getName());
  }  
}
