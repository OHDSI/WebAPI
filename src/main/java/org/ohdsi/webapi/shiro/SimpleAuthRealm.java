package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author gennadiy.anisimov based on sample realm created by GMalikov
 */
public class SimpleAuthRealm extends AuthorizingRealm {
  
  public SimpleAuthRealm() {
    setAuthenticationTokenClass(SimpleAuthToken.class);
  }
  
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PermissionManager authorizer;
  
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
      final String login = (String) principalCollection.getPrimaryPrincipal();
      return authorizer.getAuthorizationInfo(login);
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
      final SimpleAuthToken credentials = (SimpleAuthToken) authenticationToken;
      final String login = credentials.getUsername();
      if (login == null) {
        throw new UnknownAccountException("Login not provided");
      }

      final UserEntity userEntity = userRepository.findByLogin(login);
      if (userEntity == null) {
        throw new UnknownAccountException("Account does not exist");
      }
      
      return new SimpleAuthenticationInfo(login, "", getName());
    }
}
