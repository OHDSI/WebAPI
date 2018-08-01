package org.ohdsi.webapi.service;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.collections.impl.block.factory.Comparators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ohdsi.webapi.userimport.entities.RoleGroupMappingEntity;
import org.ohdsi.webapi.userimport.model.*;
import org.ohdsi.webapi.userimport.services.UserImportService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  @Autowired
  private UserImportService userImportService;

  @Value("${security.ad.url}")
  private String adUrl;

  @Value("${security.ldap.url}")
  private String ldapUrl;

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
    public List<Permission> permissions;

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
  @Path("user/me")
  @Produces(MediaType.APPLICATION_JSON)
  public User getCurrentUser() throws Exception {

    UserEntity currentUser = this.authorizer.getCurrentUser();
    Iterable<PermissionEntity> permissions = this.authorizer.getUserPermissions(currentUser.getId());

    User user = new User();
    user.id = currentUser.getId();
    user.login = currentUser.getLogin();
    user.permissions = convertPermissions(permissions);

    return user;
  }


  @GET
  @Path("user/providers")
  @Produces(MediaType.APPLICATION_JSON)
  public AuthenticationProviders getAuthenticationProviders() {
    AuthenticationProviders providers = new AuthenticationProviders();
    providers.setAdUrl(adUrl);
    providers.setLdapUrl(ldapUrl);
    return providers;
  }

  @GET
  @Path("user/import/{type}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  public List<LdapGroup> findGroups(@PathParam("type") String type, @QueryParam("search") String searchStr) {
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return userImportService.findGroups(provider, searchStr);
  }

  @POST
  @Path("user/import/{type}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<AtlasUserRoles> findDirectoryUsers(@PathParam("type") String type, RoleGroupMapping mapping){
    LdapProviderType provider = LdapProviderType.fromValue(type);
    return userImportService.findUsers(provider, mapping);
  }

  @POST
  @Path("user/import")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response importUsers(List<AtlasUserRoles> users) {
    userImportService.importUsers(users);
    return Response.ok().build();
  }

  @POST
  @Path("user/import/{type}/mapping")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveMapping(@PathParam("type") String type, RoleGroupMapping mapping) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupMappingEntity> mappingEntities = convertRoleGroupMapping(mapping);
    userImportService.saveRoleGroupMapping(providerType, mappingEntities);
    return Response.ok().build();
  }

  @GET
  @Path("user/import/{type}/mapping")
  @Produces(MediaType.APPLICATION_JSON)
  public RoleGroupMapping getMapping(@PathParam("type") String type) {
    LdapProviderType providerType = LdapProviderType.fromValue(type);
    List<RoleGroupMappingEntity> mappingEntities = userImportService.getRoleGroupMapping(providerType);
    return convertRoleGroupMapping(type, mappingEntities);
  }

  private RoleGroupMapping convertRoleGroupMapping(String provider, List<RoleGroupMappingEntity> mappingEntities) {

    RoleGroupMapping roleGroupMapping = new RoleGroupMapping();
    roleGroupMapping.setProvider(provider);
    Map<Long, List<RoleGroupMappingEntity>> entityMap = mappingEntities.stream()
            .collect(Collectors.groupingBy(r -> r.getRole().getId()));
    Map<Long, RoleEntity> roleMap = entityMap.entrySet().stream().map(e -> new ImmutablePair<>(e.getKey(), e.getValue().iterator().next().getRole()))
            .collect(Collectors.toMap(ImmutablePair::getKey, ImmutablePair::getValue));

    List<RoleGroupsMap> roleGroups = entityMap
            .entrySet().stream().map(entry -> {
              RoleGroupsMap roleGroupsMap = new RoleGroupsMap();
              roleGroupsMap.setRole(new Role(roleMap.get(entry.getKey())));
              List<LdapGroup> groups = entry
                      .getValue()
                      .stream()
                      .map(role -> new LdapGroup(role.getGroupName(), role.getGroupDn()))
                      .collect(Collectors.toList());
              roleGroupsMap.setGroups(groups);
              return roleGroupsMap;
      }).collect(Collectors.toList());
    roleGroupMapping.setRoleGroups(roleGroups);
    return roleGroupMapping;
  }

  private List<RoleGroupMappingEntity> convertRoleGroupMapping(RoleGroupMapping mapping) {

    final String providerTypeName = mapping.getProvider();
    final LdapProviderType providerTyper = LdapProviderType.fromValue(providerTypeName);
    return mapping.getRoleGroups().stream().flatMap(m -> {
      RoleEntity roleEntity = convertRole(m.getRole());
      return m.getGroups().stream().map(g -> {
        RoleGroupMappingEntity entity = new RoleGroupMappingEntity();
        entity.setGroupDn(g.getDistinguishedName());
        entity.setGroupName(g.getDisplayName());
        entity.setRole(roleEntity);
        entity.setProvider(providerTyper);
        return entity;
      });
    }).collect(Collectors.toList());
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

  private RoleEntity convertRole(Role role) {
    RoleEntity roleEntity = new RoleEntity();
    roleEntity.setName(role.role);
    roleEntity.setId(role.id);
    return roleEntity;
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
