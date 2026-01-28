package org.ohdsi.webapi.service;

import org.ohdsi.webapi.arachne.logging.event.*;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.user.Role;
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
public class UserService {

  @Autowired
  private PermissionManager authorizer;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Value("${trexsql.enabled:false}")
  private boolean trexsqlCacheEnabled;

  @Value("${security.auth.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
  private List<String> defaultRoles;

  private Map<String, String> roleCreatorPermissionsTemplate = new LinkedHashMap<>();

  public UserService() {
    this.roleCreatorPermissionsTemplate.put("role:%s:permissions:*:put", "Add permissions to role with ID = %s");
    this.roleCreatorPermissionsTemplate.put("role:%s:permissions:*:delete", "Remove permissions from role with ID = %s");
    this.roleCreatorPermissionsTemplate.put("role:%s:put", "Update role with ID = %s");
    this.roleCreatorPermissionsTemplate.put("role:%s:delete", "Delete role with ID = %s");
  }

  public static class User implements Comparable<User> {
    public Long id;
    public String login;
    public String name;
    public List<Permission> permissions;
    public Map<String, List<String>> permissionIdx;
    public Boolean trexsqlCacheEnabled;

    public User() {}

    public User(UserEntity userEntity) {
      this.id = userEntity.getId();
      this.login = userEntity.getLogin();
      this.name = userEntity.getName();
    }

    @Override
    public int compareTo(User o) {
      Comparator c = Comparator.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.login, o.login);
      else
        return c.compare(this.id, o.id);
    }
  }

  public static class Permission implements Comparable<Permission> {
    public Long id;
    public String permission;
    public String description;

    public Permission() {}

    public Permission(PermissionEntity permissionEntity) {
      this.id = permissionEntity.getId();
      this.permission = permissionEntity.getValue();
      this.description = permissionEntity.getDescription();
    }

    @Override
    public int compareTo(Permission o) {
      Comparator c = Comparator.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.permission, o.permission);
      else
        return c.compare(this.id, o.id);
    }
  }

  @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
  public ArrayList<User> getUsers() {
    Iterable<UserEntity> userEntities = this.authorizer.getUsers();
    ArrayList<User> users = convertUsers(userEntities);
    return users;
  }

  @GetMapping(value = "/user/me", produces = MediaType.APPLICATION_JSON_VALUE)
  public User getCurrentUser() throws Exception {

    UserEntity currentUser = this.authorizer.getCurrentUser();
    Iterable<PermissionEntity> permissions = this.authorizer.getUserPermissions(currentUser.getId());

    User user = new User();
    user.id = currentUser.getId();
    user.login = currentUser.getLogin();
    user.name = currentUser.getName();
    user.permissions = convertPermissions(permissions);
    user.permissionIdx = authorizer.queryUserPermissions(currentUser.getLogin()).permissions;
    user.trexsqlCacheEnabled = trexsqlCacheEnabled;

    return user;
  }

  @GetMapping(value = "/user/{userId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Permission> getUsersPermissions(@PathVariable("userId") Long userId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(userId);
    List<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
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
    RoleEntity roleEntity = this.authorizer.addRole(role.role, true);
    RoleEntity personalRole = this.authorizer.getCurrentUserPersonalRole();
    this.authorizer.addPermissionsFromTemplate(
            personalRole,
            this.roleCreatorPermissionsTemplate,
            String.valueOf(roleEntity.getId()));
    Role newRole = new Role(roleEntity);
    eventPublisher.publishEvent(new AddRoleEvent(this, newRole.id, newRole.role));
    return newRole;
  }

  @PutMapping(value = "/role/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public Role updateRole(@PathVariable("roleId") Long id, @RequestBody Role role) throws Exception {
    RoleEntity roleEntity = this.authorizer.getRole(id);
    if (roleEntity == null) {
      throw new Exception("Role doesn't exist");
    }
    roleEntity.setName(role.role);
    roleEntity = this.authorizer.updateRole(roleEntity);
    eventPublisher.publishEvent(new ChangeRoleEvent(this, id, role.role));
    return new Role(roleEntity);
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
    Role role = new Role(roleEntity);
    return role;
  }

  @DeleteMapping(value = "/role/{roleId}")
  public void removeRole(@PathVariable("roleId") Long roleId) {
    this.authorizer.removeRole(roleId);
    this.authorizer.removePermissionsFromTemplate(this.roleCreatorPermissionsTemplate, String.valueOf(roleId));
  }

  @GetMapping(value = "/role/{roleId}/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Permission> getRolePermissions(@PathVariable("roleId") Long roleId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getRolePermissions(roleId);
    List<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
    return permissions;
  }

  @PutMapping(value = "/role/{roleId}/permissions/{permissionIdList}")
  public void addPermissionToRole(
          @PathVariable("roleId") Long roleId,
          @PathVariable("permissionIdList") String permissionIdList) throws Exception {
    String[] ids = permissionIdList.split("\\+");
    for (String permissionIdString : ids) {
      Long permissionId = Long.parseLong(permissionIdString);
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
    Collections.sort(users);
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
            .map(UserService.Permission::new)
            .collect(Collectors.toList());
  }

  private ArrayList<Role> convertRoles(final Iterable<RoleEntity> roleEntities) {
    ArrayList<Role> roles = new ArrayList<>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = new Role(roleEntity, defaultRoles.contains(roleEntity.getName()));
      roles.add(role);
    }

    return roles;
  }

  private ArrayList<User> convertUsers(final Iterable<UserEntity> userEntities) {
    ArrayList<User> users = new ArrayList<>();
    for (UserEntity userEntity : userEntities) {
      User user = new User(userEntity);
      users.add(user);
    }

    return users;
  }
}
