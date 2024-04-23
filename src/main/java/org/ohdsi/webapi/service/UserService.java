package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.odysseusinc.logging.event.*;
import org.eclipse.collections.impl.block.factory.Comparators;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.user.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author gennadiy.anisimov
 */

@Path("/")
@Component
public class UserService {

  @Autowired
  private PermissionManager authorizer;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Value("${security.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
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

    public User() {}

    public User(UserEntity userEntity) {
      this.id = userEntity.getId();
      this.login = userEntity.getLogin();
      this.name = userEntity.getName();
    }

    @Override
    public int compareTo(User o) {
      Comparator c = Comparators.naturalOrder();
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
      Comparator c = Comparators.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.permission, o.permission);
      else
        return c.compare(this.id, o.id);
    }
  }

  @GET
  @Path("user")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<User> getUsers() {
    Iterable<UserEntity> userEntities = this.authorizer.getUsers();
    ArrayList<User> users = convertUsers(userEntities);
    return users;
  }

  @GET
  @Path("user/me")
  @Produces(MediaType.APPLICATION_JSON)
  public User getCurrentUser() throws Exception {

    UserEntity currentUser = this.authorizer.getCurrentUser();
    Iterable<PermissionEntity> permissions = this.authorizer.getUserPermissions(currentUser.getId());

    User user = new User();
    user.id = currentUser.getId();
    user.login = currentUser.getLogin();
    user.name = currentUser.getName();
    user.permissions = convertPermissions(permissions);
    user.permissionIdx = authorizer.queryUserPermissions(currentUser.getLogin()).permissions;
    

    return user;
  }

  @GET
  @Path("user/{userId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Permission> getUsersPermissions(@PathParam("userId") Long userId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(userId);
    List<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
    return permissions;
  }

  @GET
  @Path("user/{userId}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Role> getUserRoles(@PathParam("userId") Long userId) throws Exception {
    Set<RoleEntity> roleEntities = this.authorizer.getUserRoles(userId);
    ArrayList<Role> roles = convertRoles(roleEntities);
    Collections.sort(roles);
    return roles;
  }

  @POST
  @Path("role")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Role createRole(Role role) throws Exception {
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

  @PUT
  @Path("role/{roleId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Role updateRole(@PathParam("roleId") Long id, Role role) throws Exception {
    RoleEntity roleEntity = this.authorizer.getRole(id);
    if (roleEntity == null) {
      throw new Exception("Role doesn't exist");
    }
    roleEntity.setName(role.role);
    roleEntity = this.authorizer.updateRole(roleEntity);
    eventPublisher.publishEvent(new ChangeRoleEvent(this, id, role.role));
    return new Role(roleEntity);
  }

  @GET
  @Path("role")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Role> getRoles(
          @DefaultValue("false") @QueryParam("include_personal") boolean includePersonalRoles) {
    Iterable<RoleEntity> roleEntities = this.authorizer.getRoles(includePersonalRoles);
    ArrayList<Role> roles = convertRoles(roleEntities);
    return roles;
  }

  @GET
  @Path("role/{roleId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Role getRole(@PathParam("roleId") Long id) {
    RoleEntity roleEntity = this.authorizer.getRole(id);
    Role role = new Role(roleEntity);
    return role;
  }

  @DELETE
  @Path("role/{roleId}")
  public void removeRole(@PathParam("roleId") Long roleId) {
    this.authorizer.removeRole(roleId);
    this.authorizer.removePermissionsFromTemplate(this.roleCreatorPermissionsTemplate, String.valueOf(roleId));
  }

  @GET
  @Path("role/{roleId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Permission> getRolePermissions(@PathParam("roleId") Long roleId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getRolePermissions(roleId);
    List<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
    return permissions;
  }

  @PUT
  @Path("role/{roleId}/permissions/{permissionIdList}")
  public void addPermissionToRole(@PathParam("roleId") Long roleId, @PathParam("permissionIdList") String permissionIdList) throws Exception {
    String[] ids = permissionIdList.split("\\+");
    for (String permissionIdString : ids) {
      Long permissionId = Long.parseLong(permissionIdString);
      this.authorizer.addPermission(roleId, permissionId);
      eventPublisher.publishEvent(new AddPermissionEvent(this, permissionId, roleId));
    }
  }

  @DELETE
  @Path("role/{roleId}/permissions/{permissionIdList}")
  public void removePermissionFromRole(@PathParam("roleId") Long roleId, @PathParam("permissionIdList") String permissionIdList) {
    String[] ids = permissionIdList.split("\\+");
    for (String permissionIdString : ids) {
      Long permissionId = Long.parseLong(permissionIdString);
      this.authorizer.removePermission(permissionId, roleId);
      eventPublisher.publishEvent(new DeletePermissionEvent(this, permissionId, roleId));
    }
  }

  @GET
  @Path("role/{roleId}/users")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<User> getRoleUsers(@PathParam("roleId") Long roleId) throws Exception {
    Set<UserEntity> userEntities = this.authorizer.getRoleUsers(roleId);
    ArrayList<User> users = this.convertUsers(userEntities);
    Collections.sort(users);
    return users;
  }

  @PUT
  @Path("role/{roleId}/users/{userIdList}")
  public void addUserToRole(@PathParam("roleId") Long roleId, @PathParam("userIdList") String userIdList) throws Exception {
    String[] ids = userIdList.split("\\+");
    for (String userIdString : ids) {
      Long userId = Long.parseLong(userIdString);
      this.authorizer.addUser(userId, roleId);
      eventPublisher.publishEvent(new AssignRoleEvent(this, roleId, userId));
    }
  }

  @DELETE
  @Path("role/{roleId}/users/{userIdList}")
  public void removeUserFromRole(@PathParam("roleId") Long roleId, @PathParam("userIdList") String userIdList) {
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
