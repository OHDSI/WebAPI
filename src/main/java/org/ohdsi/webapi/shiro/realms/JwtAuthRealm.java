package org.ohdsi.webapi.shiro.realms;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.ohdsi.webapi.security.model.UserSimpleAuthorizationInfo;
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

  @Override
  protected boolean isPermitted(Permission permission, AuthorizationInfo info) {
    // for speed, we check the permission against the set of permissions found by the key '*' and the first element of the permission.
    String permPrefix =  StringUtils.split(permission.toString(), ":")[0];
    // we only need to check * perms and perms that begin with the same prefix (up to first :) as the requested permission
    // starting with perms that start with *
    List<Permission> permsToCheck = new ArrayList(((UserSimpleAuthorizationInfo)info).getPermissionIdx().getOrDefault("*", new ArrayList<>()));
    // adding those permissiosn that start with the permPrefix
    permsToCheck.addAll(((UserSimpleAuthorizationInfo)info).getPermissionIdx().getOrDefault(permPrefix, new ArrayList<>()));
    
    // do check
    return permsToCheck.stream().anyMatch(permToCheck -> permToCheck.implies(permission));
    
  }
}
