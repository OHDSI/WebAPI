package org.ohdsi.webapi.shiro;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
@Transactional
public class PermissionManager {
  
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


  public RoleEntity addRole(String roleName) throws Exception {
    Guard.checkNotEmpty(roleName);
    
    RoleEntity role = this.roleRepository.findByName(roleName);
    if (role != null) {
      throw new Exception("Can't create role - it already exists");
    }
    
    role = new RoleEntity();
    role.setName(roleName);
    role = this.roleRepository.save(role);
    
    return role;
  }

  public String addUserToRole(String roleName, String login) throws Exception {
    Guard.checkNotEmpty(roleName);
    Guard.checkNotEmpty(login);
    
    RoleEntity role = this.getRoleByName(roleName);
    UserEntity user = this.getUserByLogin(login);

    UserRoleEntity userRole = this.addUser(user, role, null);
    return userRole.getStatus();
  }

  public void removeUserFromRole(String roleName, String login) throws Exception {
    Guard.checkNotEmpty(roleName);
    Guard.checkNotEmpty(login);

    if (roleName.equalsIgnoreCase(login))
      throw new Exception("Can't remove user from personal role");

    RoleEntity role = this.getRoleByName(roleName);
    UserEntity user = this.getUserByLogin(login);

    UserRoleEntity userRole = this.userRoleRepository.findByUserAndRole(user, role);
    if (userRole != null)
      this.userRoleRepository.delete(userRole);
  }

  public Iterable<RoleEntity> getRoles(boolean includePersonalRoles) {
    Iterable<RoleEntity> roles = this.roleRepository.findAll();
    if (includePersonalRoles) {
      return roles;
    }

    Set<String> logins = this.userRepository.getUserLogins();
    HashSet<RoleEntity> filteredRoles = new HashSet<>();
    for (RoleEntity role : roles) {
      if (!logins.contains(role.getName())) {
        filteredRoles.add(role);
      }
    }

    return filteredRoles;
  }

  public AuthorizationInfo getAuthorizationInfo(final String login) {
    final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    
    final UserEntity userEntity = userRepository.findByLogin(login);
    if(userEntity == null) {
      throw new UnknownAccountException("Account does not exist");
    }
    
    final Set<String> permissionNames = new LinkedHashSet<>();
    final Set<PermissionEntity> permissions = this.getUserPermissions(userEntity);

    for (PermissionEntity permission : permissions) {
      permissionNames.add(permission.getValue());
    }

    info.setStringPermissions(permissionNames);
    return info;
  }

  @Transactional
  public UserEntity registerUser(final String login, final Set<String> defaultRoles) throws Exception {
    Guard.checkNotEmpty(login);
    
    UserEntity user = userRepository.findByLogin(login);
    if (user != null) {
      return user;
    }
    
    user = new UserEntity();
    user.setLogin(login);
    user = userRepository.save(user);

    RoleEntity personalRole = this.addRole(login);
    this.addUser(user, personalRole, null);

    if (defaultRoles != null) {
      for (String roleName: defaultRoles) {
        RoleEntity defaultRole = this.roleRepository.findByName(roleName);
        if (defaultRole != null) {
          this.addUser(user, defaultRole, null);
        }
      }
    }

    user = userRepository.findOne(user.getId());
    return user;
  }

  @Transactional
  public void removeUser(final String login) {
    UserEntity user = userRepository.findByLogin(login);

    if (user != null) {
      this.deleteRole(login);   // delete individual role
      userRepository.delete(user);
    }
  }
  
  @Transactional
  public String requestPermission(final String role, final String permission, final String description) throws Exception {
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
    Set<PermissionRequest> requests = new LinkedHashSet<>();
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

  public String approvePermissionRequest(final Long requestId) throws Exception {
    return this.changePermissionRequestStatus(requestId, RequestStatus.APPROVED);
  }

  public String refusePermissionRequest(final Long requestId) throws Exception {
    return this.changePermissionRequestStatus(requestId, RequestStatus.REFUSED);
  }

  public String requestRole(final String role) throws Exception {
    final RoleEntity roleEntity = this.getRoleByName(role);    
    final UserEntity userEntity = this.getCurrentUser();
    final UserRoleEntity request = this.addUser(userEntity, roleEntity, RequestStatus.REQUESTED);
    final String status = request.getStatus();
    return status;
  }

  public Set<RoleRequest> getRequestedRoles() {
    List<UserRoleEntity> requestedUserRoles = this.userRoleRepository.findByStatusIgnoreCase(RequestStatus.REQUESTED);
    Set<RoleRequest> requests = new LinkedHashSet<>();
    for (UserRoleEntity userRole: requestedUserRoles) {
      RoleRequest request = new RoleRequest();
      request.setId(userRole.getId());
      request.setRole(userRole.getRole().getName());
      request.setUser(userRole.getUser().getLogin());

      requests.add(request);
    }

    return requests;
  }

  public String approveRoleRequest(final Long requestId) throws Exception {
    return this.changeRoleRequestStatus(requestId, RequestStatus.APPROVED);
  }

  public String refuseRoleRequest(final Long requestId) throws Exception {
    return this.changeRoleRequestStatus(requestId, RequestStatus.REFUSED);
  }

  public Iterable<UserEntity> getUsers() {
    return this.userRepository.findAll();
  }

  public PermissionEntity addPermission(final String permissionName, final String permissionDescription) throws Exception {
    Guard.checkNotEmpty(permissionName);

    PermissionEntity permission = this.permissionRepository.findByValueIgnoreCase(permissionName);
    if (permission != null) {
      throw new Exception("Can't create permission - it already exists");
    }

    permission = new PermissionEntity();
    permission.setValue(permissionName);
    permission.setDescription(permissionDescription);
    permission = this.permissionRepository.save(permission);
    return permission;
  }

  public Set<RoleEntity> getUserRoles(Long userId) throws Exception {
    UserEntity user = this.getUserById(userId);
    Set<RoleEntity> roles = this.getUserRoles(user);
    return roles;
  }

  public Iterable<PermissionEntity> getPermissions() {
    return this.permissionRepository.findAll();
  }

  public Set<PermissionEntity> getUserPermissions(Long userId) throws Exception {
    UserEntity user = this.getUserById(userId);
    Set<PermissionEntity> permissions = this.getUserPermissions(user);
    return permissions;
  }

  public void removeRole(Long roleId) {
    this.roleRepository.delete(roleId);
  }

  public Set<PermissionEntity> getRolePermissions(Long roleId) throws Exception {
    RoleEntity role = this.getRoleById(roleId);
    Set<PermissionEntity> permissions = this.getRolePermissions(role);
    return permissions;
  }

  public void addPermission(Long roleId, Long permissionId) throws Exception {
    PermissionEntity permission = this.getPermissionById(permissionId);
    RoleEntity role = this.getRoleById(roleId);

    this.addPermission(role, permission, null);
  }

  public void addPermission(RoleEntity role, PermissionEntity permission) {
    this.addPermission(role, permission, null);
  }

  public void removePermission(Long permissionId, Long roleId) {
    RolePermissionEntity rolePermission = this.rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
    if (rolePermission != null)
      this.rolePermissionRepository.delete(rolePermission);
  }

  public Set<UserEntity> getRoleUsers(Long roleId) throws Exception {
    RoleEntity role = this.getRoleById(roleId);
    Set<UserEntity> users = this.getRoleUsers(role);
    return users;
  }

  public void addUser(Long userId, Long roleId) throws Exception {
    UserEntity user = this.getUserById(userId);
    RoleEntity role = this.getRoleById(roleId);

    this.addUser(user, role, null);
  }

  public void removeUser(Long userId, Long roleId) {
    UserRoleEntity userRole = this.userRoleRepository.findByUserIdAndRoleId(userId, roleId);
    if (userRole != null)
      this.userRoleRepository.delete(userRole);
  }

  public void removePermission(Long permissionId) {
    this.permissionRepository.delete(permissionId);
  }

  public void removePermission(String value) {
    PermissionEntity permission = this.permissionRepository.findByValueIgnoreCase(value);
    if (permission != null)
      this.permissionRepository.delete(permission);
  }

  public RoleEntity getCurrentUserPersonalRole() throws Exception {
    String roleName = this.getSubjectName();
    RoleEntity role = this.roleRepository.findByName(roleName);
    if (role == null)
      throw new Exception(String.format("There is no personal role for user %s", roleName));

    return role;
  }


  private Set<PermissionEntity> getUserPermissions(UserEntity user) {
    Set<RoleEntity> roles = this.getUserRoles(user);
    Set<PermissionEntity> permissions = new LinkedHashSet<>();

    for (RoleEntity role : roles) {
      permissions.addAll(this.getRolePermissions(role));
    }

    return permissions;
  }

  private Set<PermissionEntity> getRolePermissions(RoleEntity role) {
    Set<PermissionEntity> permissions = new LinkedHashSet<>();

    Set<RolePermissionEntity> rolePermissions = role.getRolePermissions();
    for (RolePermissionEntity rolePermission : rolePermissions) {
      if (isRelationAllowed(rolePermission.getStatus())) {
        PermissionEntity permission = rolePermission.getPermission();
        permissions.add(permission);
      }
    }

    return permissions;
  }

  private Set<RoleEntity> getUserRoles(UserEntity user) {
    Set<UserRoleEntity> userRoles = user.getUserRoles();
    Set<RoleEntity> roles = new LinkedHashSet<>();
    for (UserRoleEntity userRole : userRoles) {
      if (isRelationAllowed(userRole.getStatus())) {
        RoleEntity role = userRole.getRole();
        roles.add(role);
      }
    }

    return roles;
  }

  private Set<UserEntity> getRoleUsers(RoleEntity role) {
    Set<UserEntity> users = new LinkedHashSet<>();
    for (UserRoleEntity userRole : role.getUserRoles()) {
      if (isRelationAllowed(userRole.getStatus())) {
        users.add(userRole.getUser());
      }
    }
    return users;
  }

  private UserEntity getCurrentUser() throws Exception {
    final String login = this.getSubjectName();
    final UserEntity currentUser = this.getUserByLogin(login);
    return currentUser;
  }

  private UserEntity getUserById(Long userId) throws Exception {
    UserEntity user = this.userRepository.findOne(userId);
    if (user == null)
      throw new Exception("User doesn't exist");

    return user;
  }

  private UserEntity getUserByLogin(final String login) throws Exception {
    final UserEntity user = this.userRepository.findByLogin(login);
    if (user == null)
      throw new Exception("User doesn't exist");

    return user;
  }

  private RoleEntity getRoleByName(String roleName) throws Exception {
    final RoleEntity roleEntity = this.roleRepository.findByName(roleName);
    if (roleEntity == null)
      throw new Exception("Role doesn't exist");

    return roleEntity;
  }

  private RoleEntity getRoleById(Long roleId) throws Exception {
    final RoleEntity roleEntity = this.roleRepository.findById(roleId);
    if (roleEntity == null)
      throw new Exception("Role doesn't exist");

    return roleEntity;
  }

  private PermissionEntity getPermissionById(Long permissionId) throws Exception {
    final PermissionEntity permission = this.permissionRepository.findById(permissionId);
    if (permission == null )
      throw new Exception("Permission doesn't exist");

    return permission;
  }

  private String changePermissionRequestStatus(Long requestId, String status) throws Exception {
    RolePermissionEntity rolePermission = this.rolePermissionRepository.findById(requestId);
    if (rolePermission == null)
      throw new Exception("Request doesn't exist");

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

  private UserRoleEntity addUser(final UserEntity user, final RoleEntity role, final String status) {
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

  public String getSubjectName() {
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

  private String changeRoleRequestStatus(Long requestId, String status) throws Exception {
    UserRoleEntity userRole = this.userRoleRepository.findOne(requestId);
    if (userRole == null)
      throw new Exception("Request doesn't exist");

    if (RequestStatus.REQUESTED.equals(userRole.getStatus())) {
      userRole.setStatus(status);
      userRole = this.userRoleRepository.save(userRole);
    }

    return userRole.getStatus();
  }

  public RoleEntity getRole(Long id) {
    return this.roleRepository.findById(id);
  }

  public RoleEntity updateRole(RoleEntity roleEntity) {
    return this.roleRepository.save(roleEntity);
  }

  public void addPermissionsFromTemplate(RoleEntity roleEntity, Map<String, String> template, String value) throws Exception {
    for (Map.Entry<String, String> entry : template.entrySet()) {
      String permission = String.format(entry.getKey(), value);
      String description = String.format(entry.getValue(), value);
      PermissionEntity permissionEntity = this.addPermission(permission, description);
      this.addPermission(roleEntity, permissionEntity);
    }
  }

  public void removePermissionsFromTemplate(Map<String, String> template, String value) {
    for (Map.Entry<String, String> entry : template.entrySet()) {
      String permission = String.format(entry.getKey(), value);
      this.removePermission(permission);
    }
  }

  public boolean roleExists(String roleName) {
    return this.roleRepository.existsByName(roleName);
  }
}
