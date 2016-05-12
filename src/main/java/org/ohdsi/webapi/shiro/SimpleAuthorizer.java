package org.ohdsi.webapi.shiro;

import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
class SimpleAuthorizer {
  
  @Autowired
  private UserRepository userRepository;  
  
  public AuthorizationInfo getAuthorizationInfo(String login) {
    final UserEntity userEntity = userRepository.findByLogin(login);
    if(userEntity == null) {
      throw new UnknownAccountException("Account does not exist");
    }
    
    final int totalRoles = userEntity.getRoles().size();
    final Set<String> roleNames = new LinkedHashSet<>(totalRoles);
    final Set<String> permissionNames = new LinkedHashSet<>();
    
    if (totalRoles > 0) {
      for (RoleEntity roleEntity : userEntity.getRoles()){
        roleNames.add(roleEntity.getName());
        for (PermissionEntity permissionEntity : roleEntity.getPermissions()){
          permissionNames.add(permissionEntity.getValue());
        }
      }
    }
    
    final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
    info.setStringPermissions(permissionNames);
    return info;
  }
}
