package org.ohdsi.webapi.security.authz;

import org.ohdsi.webapi.arachne.logging.event.*;
import org.ohdsi.webapi.security.authz.access.UserAuthorizations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author gennadiy.anisimov
 */

@RestController
@RequestMapping("")
public class UserController {

  private record UserInfo (
    User user,
    UserAuthorizations authz
  ){};

  @Autowired
  private AuthorizationService authorizer;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Value("${security.auth.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
  private List<String> defaultRoles;


  @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
  public ArrayList<User> getUsers() {
    Iterable<User> userDtos = this.authorizer.getUsers();
    ArrayList<User> users = new ArrayList<>();
    for (User u : userDtos) {
      users.add(u);
    }
    return users;
  }

  @GetMapping(value = "/user/me", produces = MediaType.APPLICATION_JSON_VALUE)
  public UserInfo getCurrentUser() throws Exception {
    User currentUser = this.authorizer.getCurrentUser();
    UserAuthorizations authz = this.authorizer.getUserAuthorizations(currentUser.id());
    return new UserInfo(currentUser, authz);
  }

  @GetMapping(value = "/user/{userId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Permission> getUsersPermissions(@PathVariable("userId") Long userId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(userId);
    List<Permission> permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  @GetMapping(value = "/user/{userId}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
  public ArrayList<Role> getUserRoles(@PathVariable("userId") Long userId) throws Exception {
    Set<RoleEntity> roleEntities = this.authorizer.getUserRoles(userId);
    ArrayList<Role> roles = convertRoles(roleEntities);
    Collections.sort(roles);
    return roles;
  }

  @PostMapping(value = "/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Role createRole(@RequestBody Role role) throws Exception {
    RoleEntity roleEntity = this.authorizer.addRole(role.name(), true);
    Role newRole = Role.fromEntity(roleEntity);
    eventPublisher.publishEvent(new AddRoleEvent(this, newRole.id(), newRole.name()));
    return newRole;
  }

  @PutMapping(value = "/role/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Role updateRole(@PathVariable("roleId") Long id, @RequestBody Role role) throws Exception {
    RoleEntity roleEntity = this.authorizer.getRole(id);
    if (roleEntity == null) {
      throw new Exception("Role doesn't exist");
    }
    roleEntity.setName(role.name());
    roleEntity = this.authorizer.updateRole(roleEntity);
    eventPublisher.publishEvent(new ChangeRoleEvent(this, id, role.name()));
    return Role.fromEntity(roleEntity);
  }

  @GetMapping(value = "/role", produces = MediaType.APPLICATION_JSON_VALUE)
  public ArrayList<Role> getRoles(
          @RequestParam(value = "include_personal", defaultValue = "false") boolean includePersonalRoles) {
    Iterable<RoleEntity> roleEntities = this.authorizer.getRoles(includePersonalRoles);
    ArrayList<Role> roles = convertRoles(roleEntities);
    return roles;
  }

  @GetMapping(value = "/role/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Role getRole(@PathVariable("roleId") Long id) {
    RoleEntity roleEntity = this.authorizer.getRole(id);
    Role role = Role.fromEntity(roleEntity);
    return role;
  }

  @DeleteMapping(value = "/role/{roleId}")
  public void removeRole(@PathVariable("roleId") Long roleId) {
    this.authorizer.removeRole(roleId);
  }

  @GetMapping(value = "/role/{roleId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Permission> getRolePermissions(@PathVariable("roleId") Long roleId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getRolePermissions(roleId);
    List<Permission> permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  @PutMapping(value = "/role/{roleId}/permissions")
  public void addPermissionToRole(
          @PathVariable("roleId") Long roleId,
          @RequestParam List<Long> permissionIds) throws Exception {
    for (Long permissionId : permissionIds) {
      this.authorizer.addPermission(roleId, permissionId);
      eventPublisher.publishEvent(new AddPermissionEvent(this, permissionId, roleId));
    }
  }

  @DeleteMapping(value = "/role/{roleId}/permissions/{permissionIdList}")
  public void removePermissionFromRole(
          @PathVariable("roleId") Long roleId,
          @PathVariable("permissionIdList") String permissionIdList) {
    String[] ids = permissionIdList.split("\\+");
    for (String permissionIdString : ids) {
      Long permissionId = Long.parseLong(permissionIdString);
      this.authorizer.removePermission(permissionId, roleId);
      eventPublisher.publishEvent(new DeletePermissionEvent(this, permissionId, roleId));
    }
  }

  @GetMapping(value = "/role/{roleId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ArrayList<User> getRoleUsers(@PathVariable("roleId") Long roleId) throws Exception {
    Set<UserEntity> userEntities = this.authorizer.getRoleUsers(roleId);
    ArrayList<User> users = this.convertUsers(userEntities);
    return users;
  }

  @PutMapping(value = "/role/{roleId}/users/{userIdList}")
  public void addUserToRole(
          @PathVariable("roleId") Long roleId,
          @PathVariable("userIdList") String userIdList) throws Exception {
    String[] ids = userIdList.split("\\+");
    for (String userIdString : ids) {
      Long userId = Long.parseLong(userIdString);
      this.authorizer.addUser(userId, roleId);
      eventPublisher.publishEvent(new AssignRoleEvent(this, roleId, userId));
    }
  }

  @DeleteMapping(value = "/role/{roleId}/users/{userIdList}")
  public void removeUserFromRole(
          @PathVariable("roleId") Long roleId,
          @PathVariable("userIdList") String userIdList) {
    String[] ids = userIdList.split("\\+");
    for (String userIdString : ids) {
      Long userId = Long.parseLong(userIdString);
      this.authorizer.removeUser(userId, roleId);
      eventPublisher.publishEvent(new UnassignRoleEvent(this, roleId, userId));
    }
  }

  private List<Permission> convertPermissions(final Iterable<PermissionEntity> permissionEntities) {
    return StreamSupport.stream(permissionEntities.spliterator(), false)
            .map(Permission::fromEntity)
            .collect(Collectors.toList());
  }

  private ArrayList<Role> convertRoles(final Iterable<RoleEntity> roleEntities) {
    ArrayList<Role> roles = new ArrayList<>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = Role.fromEntity(roleEntity, defaultRoles.contains(roleEntity.getName()));
      roles.add(role);
    }

    return roles;
  }

  private ArrayList<User> convertUsers(final Iterable<UserEntity> userEntities) {
    ArrayList<User> users = new ArrayList<>();
    for (UserEntity userEntity : userEntities) {
      users.add(User.fromEntity(userEntity));
    }

    return users;
  }
}
