package org.ohdsi.webapi.shiro.realms;

import io.buji.pac4j.subject.Pac4jPrincipal;
import java.security.Principal;
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
    setAuthenticationTokenClass(JwtAuthToken.class);
    this.authorizer = authorizer;
  }
   
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    try {
      String login;
      Object principal = principals.getPrimaryPrincipal();

      if (principal instanceof Principal) {
        login = ((Principal) principal).getName();
      } else if (principal instanceof Pac4jPrincipal) {
        login = ((Pac4jPrincipal) principal).getProfile().getEmail();
      } else {
        login = (String) principal;
      }
      return authorizer.getAuthorizationInfo(login);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {
    return new SimpleAuthenticationInfo(at.getPrincipal(), "", getName());
  }  
}
