package org.ohdsi.webapi.shiro;

import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.Subject;
import org.ohdsi.webapi.helper.Guard;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.PermissionRepository;
import org.ohdsi.webapi.shiro.Entities.PermissionRequest;
import org.ohdsi.webapi.shiro.Entities.RequestStatus;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.RolePermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RolePermissionRepository;
import org.ohdsi.webapi.shiro.Entities.RoleRepository;
import org.ohdsi.webapi.shiro.Entities.RoleRequest;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.Entities.UserRoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author gennadiy.anisimov
 */
public class SimpleAuthorizer {
  
  @Autowired
  private UserRepository userRepository;  
  
  @Autowired
  private RoleRepository roleRepository;  
  
  @Autowired
  private PermissionRepository permissionRepository;
  
  @Autowired
  private RolePermissionRepository rolePermissionRepository;
  
  @Autowired
  private UserRoleRepository userRoleRepository;

  public RoleEntity addRole(String roleName) {
    Guard.checkNotEmpty(roleName);
    
    RoleEntity role = this.roleRepository.findByName(roleName);
    if (role != null) {
      return role;
    }
    
    role = new RoleEntity();
    role.setName(roleName);
    role = this.roleRepository.save(role);
    
    return role;
  }

  public PermissionEntity addPermission(final RoleEntity role, final String permissionName, final String permissionDescription) {
    PermissionEntity permission = this.addPermission(permissionName, permissionDescription);
    this.addPermission(role, permission, null);
    return permission;
  }

  public AuthorizationInfo getAuthorizationInfo(final String login) {
    final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    
    final UserEntity userEntity = userRepository.findByLogin(login);
    if(userEntity == null) {
      throw new UnknownAccountException("Account does not exist");
    }
    
    final Set<String> permissionNames = new LinkedHashSet<>();
    
    for (UserRoleEntity userRole : userEntity.getUserRoles()) {
      if (isRelationAllowed(userRole.getStatus())) {
        RoleEntity roleEntity = userRole.getRole();
        for (RolePermissionEntity rolePermissionEntity : roleEntity.getRolePermissions()) {
          if (isRelationAllowed(rolePermissionEntity.getStatus()))
            permissionNames.add(rolePermissionEntity.getPermission().getValue());
        }
      }
    }

    info.setStringPermissions(permissionNames);
    return info;
  }

  @Transactional
  public UserEntity registerUser(final String login, final Set<String> defaultRoles) {
    Guard.checkNotEmpty(login);
    
    UserEntity user = userRepository.findByLogin(login);
    if (user != null) {
      return user;
    }
    
    user = new UserEntity();
    user.setLogin(login);
    user = userRepository.save(user);

    RoleEntity personalRole = this.addRole(login);
    this.addRole(user, personalRole, null);

    if (defaultRoles != null) {
      for (String roleName: defaultRoles) {
        RoleEntity defaultRole = this.roleRepository.findByName(roleName);
        if (defaultRole != null) {
          this.addRole(user, defaultRole, null);
        }
      }
    }

    user = userRepository.findOne(user.getId());
    return user;
  }

  @Transactional
  public void deleteUser(final String login) {
    UserEntity user = userRepository.findByLogin(login);

    if (user != null) {
      this.deleteRole(login);   // delete individual role
      userRepository.delete(user);
    }
  }
  
  @Transactional
  public String requestPermission(final String role, final String permission, final String description) {
    final RoleEntity roleEntity = this.getRoleByName(role);
    final PermissionEntity permissionEntity = this.addPermission(permission, description);
    final RolePermissionEntity request = this.addPermission(roleEntity, permissionEntity, RequestStatus.REQUESTED);
    final String status = request.getStatus();
    return status;
  }

  public void removePermission(final String role, final String permission) {
    Guard.checkNotEmpty(role);
    Guard.checkNotEmpty(permission);
    
    RoleEntity roleEntity = this.roleRepository.findByName(role);
    if (roleEntity == null)
      return;

    PermissionEntity permissionEntity = this.permissionRepository.findByValueIgnoreCase(permission);
    if (permissionEntity == null)
      return;

    RolePermissionEntity rolePermissionEntity = this.rolePermissionRepository.findByRoleAndPermission(roleEntity, permissionEntity);
    if (rolePermissionEntity == null)
      return;

    this.rolePermissionRepository.delete(rolePermissionEntity);
  }

  public Set<PermissionRequest> getRequestedPermissions() {
    List<RolePermissionEntity> requestedRolePermissions = this.rolePermissionRepository.findByStatusIgnoreCase(RequestStatus.REQUESTED);
    Set<PermissionRequest> requests = new HashSet<>();
    for (RolePermissionEntity rp: requestedRolePermissions) {
      PermissionRequest request = new PermissionRequest();
      request.setId(rp.getId());
      request.setRole(rp.getRole().getName());
      request.setPermission(rp.getPermission().getValue());
      request.setDescription(rp.getPermission().getDescription());

      requests.add(request);
    }

    return requests;
  }

  public String approvePermissionRequest(final Long requestId) {
    return this.changePermissionRequestStatus(requestId, RequestStatus.APPROVED);
  }

  public String refusePermissionRequest(final Long requestId) {
    return this.changePermissionRequestStatus(requestId, RequestStatus.REFUSED);
  }

  public boolean isPermitted(final String permission) {
    return SecurityUtils.getSubject().isPermitted(permission);
  }

  public String requestRole(final String role) {
    final RoleEntity roleEntity = this.getRoleByName(role);    
    final UserEntity userEntity = this.getCurrentUser();
    final UserRoleEntity request = this.addRole(userEntity, roleEntity, RequestStatus.REQUESTED);
    final String status = request.getStatus();
    return status;
  }

  public Set<RoleRequest> getRequestedRoles() {
    List<UserRoleEntity> requestedUserRoles = this.userRoleRepository.findByStatusIgnoreCase(RequestStatus.REQUESTED);
    Set<RoleRequest> requests = new HashSet<>();
    for (UserRoleEntity userRole: requestedUserRoles) {
      RoleRequest request = new RoleRequest();
      request.setId(userRole.getId());
      request.setRole(userRole.getRole().getName());
      request.setUser(userRole.getUser().getLogin());

      requests.add(request);
    }

    return requests;
  }

  public String approveRoleRequest(final Long requestId) {
    return this.changeRoleRequestStatus(requestId, RequestStatus.APPROVED);
  }

  public String refuseRoleRequest(final Long requestId) {
    return this.changeRoleRequestStatus(requestId, RequestStatus.REFUSED);
  }

  private UserEntity getCurrentUser() {
    final String login = this.getSubjectName();
    final UserEntity currentUser = this.getUserByLogin(login);
    return currentUser;
  }

  private UserEntity getUserByLogin(final String login) {
    final UserEntity user = this.userRepository.findByLogin(login);
    if (user == null)
      throw new IllegalArgumentException("User doesn't exist");

    return user;
  }
  private RoleEntity getRoleByName(String roleName) {
    final RoleEntity roleEntity = this.roleRepository.findByName(roleName);
    if (roleEntity == null)
      throw new IllegalArgumentException("Role doesn't exist");

    return roleEntity;
  }

  private String changePermissionRequestStatus(Long requestId, String status) {
    RolePermissionEntity rolePermission = this.rolePermissionRepository.findById(requestId);
    if (rolePermission == null)
      throw new IllegalArgumentException("Request doesn't exist");

    if (RequestStatus.REQUESTED.equals(rolePermission.getStatus())) {
      rolePermission.setStatus(status);
      rolePermission = this.rolePermissionRepository.save(rolePermission);
    }

    return rolePermission.getStatus();
  }

  private RolePermissionEntity addPermission(final RoleEntity role, final PermissionEntity permission, final String status) {
    RolePermissionEntity relation = this.rolePermissionRepository.findByRoleAndPermission(role, permission);
    if (relation == null) {
      relation = new RolePermissionEntity();
      relation.setRole(role);
      relation.setPermission(permission);
      relation.setStatus(status);
      relation = this.rolePermissionRepository.save(relation);
    }

    return relation;
  }

  private boolean isRelationAllowed(final String relationStatus) {
    return relationStatus == null || relationStatus == RequestStatus.APPROVED;
  }

  private UserRoleEntity addRole(final UserEntity user, final RoleEntity role, final String status) {
    UserRoleEntity relation = this.userRoleRepository.findByUserAndRole(user, role);
    if (relation == null) {
      relation = new UserRoleEntity();
      relation.setUser(user);
      relation.setRole(role);
      relation.setStatus(status);
      relation = this.userRoleRepository.save(relation);
    }
    
    return relation;
  }

  private PermissionEntity addPermission(final String permissionName, final String permissionDescription) {
    Guard.checkNotEmpty(permissionName);

    PermissionEntity permission = this.permissionRepository.findByValueIgnoreCase(permissionName);
    if (permission != null) {
      return permission;
    }

    permission = new PermissionEntity();
    permission.setValue(permissionName);
    permission.setDescription(permissionDescription);
    permission = this.permissionRepository.save(permission);
    return permission;
  }

  private String getSubjectName() {
    Subject subject = SecurityUtils.getSubject();
    Object principalObject = subject.getPrincipals().getPrimaryPrincipal();

    if (principalObject instanceof String)
      return (String)principalObject;

    if (principalObject instanceof Principal) {
      Principal principal = (Principal)principalObject;
      return principal.getName();
    }

    throw new UnsupportedOperationException();
  }

  private void deleteRole(final String role) {
    RoleEntity roleEntity = this.roleRepository.findByName(role);

    if (roleEntity != null) {
      this.roleRepository.delete(roleEntity);
    }
  }

  private String changeRoleRequestStatus(Long requestId, String status) {
    UserRoleEntity userRole = this.userRoleRepository.findOne(requestId);
    if (userRole == null)
      throw new IllegalArgumentException("Request doesn't exist");

    if (RequestStatus.REQUESTED.equals(userRole.getStatus())) {
      userRole.setStatus(status);
      userRole = this.userRoleRepository.save(userRole);
    }

    return userRole.getStatus();
  }
}
