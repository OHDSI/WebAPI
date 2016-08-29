package org.ohdsi.webapi.service;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

  public static class User {
    public Long id;
    public String login;

    public User() {}

    public User(UserEntity userEntity) {
      this.id = userEntity.getId();
      this.login = userEntity.getLogin();
    }
  }

  public static class Permission {
    public Long id;
    public String permission;
    public String description;

    public Permission() {}

    public Permission(PermissionEntity permissionEntity) {
      this.id = permissionEntity.getId();
      this.permission = permissionEntity.getValue();
      this.description = permissionEntity.getDescription();
    }
  }

  public static class Role {
    public Long id;
    public String role;

    public Role() {}

    public Role (RoleEntity roleEntity) {
      this.id = roleEntity.getId();
      this.role = roleEntity.getName();
    }

  }

  @GET
  @Path("user")
  @Produces(MediaType.APPLICATION_JSON)
  public User[] getUsers() {
    Set<UserEntity> userEntities = this.authorizer.getUsers();
    User[] users = new User[userEntities.size()];
    int i = 0;
    for (UserEntity userEntity : userEntities) {
      User user = new User();
      user.id = userEntity.getId();
      user.login = userEntity.getLogin();
      users[i] = user;
      i++;
    }

    return users;
  }

  @POST
  @Path("user/permitted")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public boolean isPermitted (String permission) {
    boolean isPermitted = this.authorizer.isPermitted(permission);
    return isPermitted;
  }

  @GET
  @Path("user/{userId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public Permission[] getUsersPermissions(@PathParam("userId") Long userId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getUserPermissions(userId);
    Permission[] permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  @GET
  @Path("user/{userId}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  public Role[] getUserRoles(@PathParam("userId") Long userId) throws Exception {
    Set<RoleEntity> roleEntities = this.authorizer.getUserRoles(userId);
    Role[] roles = convertRoles(roleEntities);
    return roles;
  }

  @PUT
  @Path("role")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Role createRole(Role role) throws Exception {
    RoleEntity roleEntity = this.authorizer.addRole(role.role);
    Role newRole = new Role(roleEntity);
    return newRole;
  }

  @GET
  @Path("role")
  @Produces(MediaType.APPLICATION_JSON)
  public Role[] getRoles() {
    Set<RoleEntity> roleEntities = this.authorizer.getRoles();
    Role[] roles = convertRoles(roleEntities);
    return roles;
  }

  @DELETE
  @Path("role/{roleId}")
  public void removeRole(@PathParam("roleId") Long roleId) {
    this.authorizer.removeRole(roleId);
  }

  @GET
  @Path("role/{roleId}/permissions")
  @Produces(MediaType.APPLICATION_JSON)
  public Permission[] getRolePermissions(@PathParam("roleId") Long roleId) throws Exception {
    Set<PermissionEntity> permissionEntities = this.authorizer.getRolePermissions(roleId);
    Permission[] permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  @PUT
  @Path("role/{roleId}/permissions/{permissionId}")
  public void addPermissionToRole(@PathParam("roleId") Long roleId, @PathParam("permissionId") Long permissionId) throws Exception {
    this.authorizer.addPermission(roleId, permissionId);
  }

  @DELETE
  @Path("role/{roleId}/permissions/{permissionId}")
  public void removePermissionFromRole(@PathParam("roleId") Long roleId, @PathParam("permissionId") Long permissionId) {
    this.authorizer.removePermission(permissionId, roleId);
  }

  @GET
  @Path("role/{roleId}/users")
  @Produces(MediaType.APPLICATION_JSON)
  public User[] getRoleUsers(@PathParam("roleId") Long roleId) throws Exception {
    Set<UserEntity> userEntities = this.authorizer.getRoleUsers(roleId);
    User[] users = this.convertUsers(userEntities);
    return users;
  }

  @PUT
  @Path("role/{roleId}/users/{userId}")
  public void addUserToRole(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) throws Exception {
    this.authorizer.addUser(userId, roleId);
  }

  @DELETE
  @Path("role/{roleId}/users/{userId}")
  public void removeUserFromRole(@PathParam("roleId") Long roleId, @PathParam("userId") Long userId) {
    this.authorizer.removeUser(userId, roleId);
  }

  @GET
  @Path("permission")
  @Produces(MediaType.APPLICATION_JSON)
  public Permission[] getPermissions() {
    Set<PermissionEntity> permissionEntities = this.authorizer.getPermissions();
    Permission[] permissions = convertPermissions(permissionEntities);
    return permissions;
  }

  @PUT
  @Path("permission")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Permission addPermissions(Permission permission) throws Exception {
    PermissionEntity permissionEntity = this.authorizer.addPermission(permission.permission, permission.description);
    Permission newPermission = new Permission(permissionEntity);
    return newPermission;
  }

  @DELETE
  @Path("permission/{permissionId}")
  public void deletePermission(@PathParam("permissionId") Long permissionId) {
    this.authorizer.removePermission(permissionId);
  }



  private Permission[] convertPermissions(final Set<PermissionEntity> permissionEntities) {
    Permission[] permissions = new Permission[permissionEntities.size()];
    int i = 0;
    for (PermissionEntity permissionEntity : permissionEntities) {
      Permission permission = new Permission(permissionEntity);
      permissions[i] = permission;
      i++;
    }

    return permissions;
  }

  private Role[] convertRoles(Set<RoleEntity> roleEntities) {
    Role[] roles = new Role[roleEntities.size()];
    int i = 0;
    for (RoleEntity roleEntity : roleEntities) {
      Role role = new Role(roleEntity);
      roles[i] = role;
      i++;
    }

    return roles;
  }

  private User[] convertUsers(Set<UserEntity> userEntities) {
    User[] users = new User[userEntities.size()];
    int i = 0;
    for (UserEntity userEntity : userEntities) {
      User user = new User(userEntity);
      users[i] = user;
      i++;
    }

    return users;
  }
}
