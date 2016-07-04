package org.ohdsi.webapi.shiro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.ohdsi.webapi.helper.Guard;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public class SimpleAuthorizer {
  
  @Autowired
  private UserRepository userRepository;  
  
  @Autowired
  private RoleRepository roleRepository;  
  
  public AuthorizationInfo getAuthorizationInfo(String login) {
    final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    
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
    
    info.setRoles(roleNames);
    info.setStringPermissions(permissionNames);
    return info;
  }
  
  public void registerUser(String login) {
    Guard.checkNotEmpty(login);
    
    UserEntity user = userRepository.findByLogin(login);
    if (user != null) {
      return;
    }
    
    user = new UserEntity();
    user.setLogin(login);
    user = userRepository.save(user);
    
    RoleEntity role = roleRepository.getDefaultRole();
    Set<RoleEntity> roles = user.getRoles();
    roles.add(role);
    user.setRoles(roles);

    userRepository.save(user);
  }  

  public void deleteUser(String login) {
    UserEntity user = userRepository.findByLogin(login);
    if (user != null)
      userRepository.delete(user);
  }
}
