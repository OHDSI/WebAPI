package org.ohdsi.webapi.shiro;

import org.apache.shiro.authz.AuthorizationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import waffle.shiro.AbstractWaffleRealm;
import waffle.shiro.WaffleFqnPrincipal;

/**
 *
 * @author gennadiy.anisimov based on sample realm created by GMalikov
 */
public class WindowsAuthRealm extends AbstractWaffleRealm {
    
  @Autowired
  private PermissionManager authorizer;
  
  @Override
  protected AuthorizationInfo buildAuthorizationInfo(WaffleFqnPrincipal waffleFqnPrincipal) {
    final String login = (String) waffleFqnPrincipal.getFqn();
    return authorizer.getAuthorizationInfo(login);
  }  
}
