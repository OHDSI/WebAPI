package org.ohdsi.webapi.service;

import org.eclipse.collections.impl.block.factory.Comparators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */

@Path("/")
@Component
public class UserService {

  @Autowired
  private PermissionManager authorizer;

  private Map<String, String> roleCreatorPermissionsTemplate = new LinkedHashMap<>();

  public UserService() {
    this.roleCreatorPermissionsTemplate.put("role:%s:permissions:*:put", "Add permissions to role with ID = %s");
    this.roleCreatorPermissionsTemplate.put("role:%s:permissions:*:delete", "Remove permissions from role with ID = %s");
    this.roleCreatorPermissionsTemplate.put("role:%s:post", "Update role with ID = %s");
    this.roleCreatorPermissionsTemplate.put("role:%s:delete", "Delete role with ID = %s");
  }

  public static class User implements Comparable<User> {
    public Long id;
    public String login;

    public User() {}

    public User(UserEntity userEntity) {
      this.id = userEntity.getId();
      this.login = userEntity.getLogin();
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

  public static class Role implements Comparable<Role> {
    public Long id;
    public String role;

    public Role() {}

    public Role (RoleEntity roleEntity) {
      this.id = roleEntity.getId();
      this.role = roleEntity.getName();
    }

    @Override
    public int compareTo(Role o) {
      Comparator c = Comparators.naturalOrder();
      if (this.id == null && o.id == null)
        return c.compare(this.role, o.role);
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
  @Path("user/{userId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Permission> getUsersPermissions(@PathParam("userId") Long userId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(userId);
    ArrayList<Permission> permissions = convertPermissions(permissionEntities);
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
    RoleEntity roleEntity = this.authorizer.addRole(role.role);
    RoleEntity personalRole = this.authorizer.getCurrentUserPersonalRole();
    this.authorizer.addPermissionsFromTemplate(
            personalRole,
            this.roleCreatorPermissionsTemplate,
            String.valueOf(roleEntity.getId()));
    Role newRole = new Role(roleEntity);
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
  public ArrayList<Permission> getRolePermissions(@PathParam("roleId") Long roleId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getRolePermissions(roleId);
    ArrayList<Permission> permissions = convertPermissions(permissionEntities);
    Collections.sort(permissions);
    return permissions;
  }

  @PUT
  @Path("role/{roleId}/permissions/{permissionIdList}")
  public void addPermissionToRole(@PathParam("roleId") Long roleId, @PathParam("permissionIdList") String permissionIdList) throws Exception {
    for (String permissionIdString: permissionIdList.split("\\+")) {
      Long permissionId = Long.parseLong(permissionIdString);
      this.authorizer.addPermission(roleId, permissionId);
    }
  }

  @DELETE
  @Path("role/{roleId}/permissions/{permissionIdList}")
  public void removePermissionFromRole(@PathParam("roleId") Long roleId, @PathParam("permissionIdList") String permissionIdList) {
    for (String permissionIdString: permissionIdList.split("\\+")) {
      Long permissionId = Long.parseLong(permissionIdString);
      this.authorizer.removePermission(permissionId, roleId);
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
    for (String userIdString: userIdList.split("\\+")) {
      Long userId = Long.parseLong(userIdString);
      this.authorizer.addUser(userId, roleId);
    }
  }

  @DELETE
  @Path("role/{roleId}/users/{userIdList}")
  public void removeUserFromRole(@PathParam("roleId") Long roleId, @PathParam("userIdList") String userIdList) {
    for (String userIdString: userIdList.split("\\+")) {
      Long userId = Long.parseLong(userIdString);
      this.authorizer.removeUser(userId, roleId);
    }
  }

  @GET
  @Path("permission")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Permission> getPermissions() {
    Iterable<PermissionEntity> permissionEntities = this.authorizer.getPermissions();
    ArrayList<Permission> permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  
  private ArrayList<Permission> convertPermissions(final Iterable<PermissionEntity> permissionEntities) {
    ArrayList<Permission> permissions = new ArrayList<Permission>();
    for (PermissionEntity permissionEntity : permissionEntities) {
      Permission permission = new Permission(permissionEntity);
      permissions.add(permission);
    }

    return permissions;
  }

  private ArrayList<Role> convertRoles(final Iterable<RoleEntity> roleEntities) {
    ArrayList<Role> roles = new ArrayList<Role>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = new Role(roleEntity);
      roles.add(role);
    }

    return roles;
  }

  private ArrayList<User> convertUsers(final Iterable<UserEntity> userEntities) {
    ArrayList<User> users = new ArrayList<User>();
    for (UserEntity userEntity : userEntities) {
      User user = new User(userEntity);
      users.add(user);
    }

    return users;
  }
}
