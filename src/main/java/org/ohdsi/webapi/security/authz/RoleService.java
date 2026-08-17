package org.ohdsi.webapi.security.authz;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ohdsi.webapi.security.authc.UserOrigin;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * The RoleService is intentionaly left package-protected, as all interactions
 * with authz will be performed through AuthorizationService.
 * 
 * RoleService manages roll lifecycle operations (including role creation,
 * adding users to roles, adding permissions to roles) and lookup functions.
 * Making this service a package-protected class will let us return JPA Entities
 * freely without risking leaking entities to outer callers.
 */
@Service
class RoleService {

  private final UserService userService;
  private final PermissionService permissionService;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final RolePermissionRepository rolePermissionRepository;
  private final AuthorizationCacheService authCacheService;
  public RoleService(
      RoleRepository roleRepository,
      UserRoleRepository userRoleRepository,
      RolePermissionRepository rolePermissionRepository,
      UserService userService,
      PermissionService permissionService,
      AuthorizationCacheService authCacheService) {
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
    this.userService = userService;
    this.permissionService = permissionService;
    this.authCacheService = authCacheService;
  }

  // -------------------------
  // Role CRUD
  // -------------------------

  public RoleEntity addRole(String roleName, boolean isSystem) {

    Assert.hasLength(roleName, "roleName must not be null or empty");

    if(roleExists(roleName)) {
      throw new RuntimeException("Can't create role - it already exists");
    }

    RoleEntity role = new RoleEntity();
    role.setName(roleName);
    role.setSystemRole(isSystem);
    role = this.roleRepository.save(role);

    return role;
  }

  public RoleEntity updateRole(RoleEntity roleEntity) {
    return this.roleRepository.save(roleEntity);
  }

  public void removeRole(Long roleId) {
    this.roleRepository.deleteById(roleId);
  }

  // -------------------------
  // Role Lists and Lookup
  // -------------------------

  public Iterable<RoleEntity> getRoles(boolean includePersonalRoles) {

    if (includePersonalRoles) {
      return this.roleRepository.findAll();
    } else {
      return this.roleRepository.findAllBySystemRoleTrue();
    }
  }

  public Set<RoleEntity> getUserRoles(UserEntity user) {
    Set<UserRoleEntity> userRoles = user.getUserRoles();
    Set<RoleEntity> roles = new LinkedHashSet<>();
    for (UserRoleEntity userRole : userRoles) {
      roles.add(userRole.getRole());
    }

    return roles;
  }

  public RoleEntity getRole(Long id) {
    return this.roleRepository.findById(id).orElseThrow();
  }

  public boolean roleExists(String roleName) {
    return this.roleRepository.existsByName(roleName);
  }

  /**
   * Search for system roles whose name contains the search string (case-insensitive).
   * Returns all system roles if roleSearch is null or empty.
   *
   * @param roleSearch The partial role name to search for, or null/empty for all
   * @return List of matching RoleEntity instances
   */
  public List<RoleEntity> searchRoles(String roleSearch) {
    if (roleSearch == null || roleSearch.isBlank()) {
      List<RoleEntity> roles = new java.util.ArrayList<>();
      this.roleRepository.findAllBySystemRoleTrue().forEach(roles::add);
      return roles;
    }
    return this.roleRepository.findByNameIgnoreCaseContaining(roleSearch);
  }

  // -------------------------
  // Role Permissions
  // -------------------------

  public Set<PermissionEntity> getRolePermissions(Long roleId) {
    RoleEntity role = this.getRoleById(roleId);
    Set<PermissionEntity> permissions = this.getRolePermissions(role);
    return permissions;
  }

  public void addPermission(Long roleId, Long permissionId) {
    PermissionEntity permission = permissionService.getPermissionById(permissionId);
    RoleEntity role = this.getRoleById(roleId);

    this.addPermission(role, permission);
  }

  private RolePermissionEntity addPermission(final RoleEntity role, final PermissionEntity permission) {
    RolePermissionEntity relation = this.rolePermissionRepository.findByRoleAndPermission(role, permission);
    if (relation == null) {
      relation = new RolePermissionEntity();
      relation.setRole(role);
      relation.setPermission(permission);
      relation = this.rolePermissionRepository.save(relation);
      authCacheService.evictUsersWithRole(role.getId());
    }

    return relation;
  }

  public void removePermission(Long roleId, Long permissionId) {
    RolePermissionEntity rolePermission = this.rolePermissionRepository.findByRoleIdAndPermissionId(roleId,
        permissionId);
    if (rolePermission != null)
      this.rolePermissionRepository.delete(rolePermission);
      authCacheService.evictUsersWithRole(roleId);
  }

  private Set<PermissionEntity> getRolePermissions(RoleEntity role) {
    Set<PermissionEntity> permissions = new LinkedHashSet<>();

    Set<RolePermissionEntity> rolePermissions = role.getRolePermissions();
    for (RolePermissionEntity rolePermission : rolePermissions) {
      permissions.add(rolePermission.getPermission());
    }

    return permissions;
  }

  public Optional<RoleEntity> getRoleByName(String roleName, Boolean isSystemRole) {
    return this.roleRepository.findByNameAndSystemRole(roleName, isSystemRole);
  }

  public Optional<RoleEntity> getSystemRoleByName(String roleName) {
    return getRoleByName(roleName, true);
  }

  private RoleEntity getRoleById(Long roleId) {
    return this.roleRepository.findById(roleId).orElseThrow();
  }

  // -------------------------
  // Role Assignments
  // -------------------------

  public void addUserToRole(String login, String roleName) {
    addUserToRole(login, roleName, UserOrigin.SYSTEM);
  }

  public void addUserToRole(Long userId, Long roleId) {
    UserEntity user = userService.getUserById(userId);
    RoleEntity role = this.getRoleById(roleId);

    this.addUserToRole(user, role, UserOrigin.SYSTEM);
  }

  public void addUserToRole(String login, String roleName, UserOrigin userOrigin) {
    Assert.hasLength(roleName, "roleName can not be empty.");
    Assert.hasLength(login, "login can not be empty");

    RoleEntity role = this.getSystemRoleByName(roleName).orElseThrow(() -> new RuntimeException("Role not found."));
    UserEntity user = userService.getUserByLogin(login).orElseThrow(() -> new RuntimeException("Login not found."));

    this.addUserToRole(user, role, userOrigin);
  }

  public UserRoleEntity addUserToRole(final UserEntity user, final RoleEntity role,
      final UserOrigin userOrigin) {
    final UserOrigin origin = userOrigin != null ? userOrigin : UserOrigin.SYSTEM;

    UserRoleEntity relation = this.userRoleRepository.findFirstByUserAndRoleAndOrigin(user, role, origin)
        .orElseGet(() -> {
          UserRoleEntity newRelation = new UserRoleEntity();
          newRelation.setUser(user);
          newRelation.setRole(role);
          newRelation.setOrigin(origin);
          newRelation = this.userRoleRepository.save(newRelation);
          authCacheService.evictUser(user.getId());
          return newRelation;
        });

    return relation;
  }

  public void removeUserFromRole(String login, String roleName, UserOrigin origin) {
    Assert.hasLength(roleName, "roleName can not be empty.");
    Assert.hasLength(login, "login can not be empty");

    if (roleName.equalsIgnoreCase(login))
      throw new RuntimeException("Can't remove user from personal role");

    RoleEntity role = this.getSystemRoleByName(roleName).orElseThrow(() -> new RuntimeException("Role not found."));
    UserEntity user = userService.getUserByLogin(login).orElseThrow(() -> new RuntimeException("Login not found."));

    List<UserRoleEntity> assignments = this.userRoleRepository.findAllByUserAndRole(user, role).stream()
        .filter(userRole -> origin == null || origin.equals(userRole.getOrigin()))
        .toList();

    if (!assignments.isEmpty()) {
      this.userRoleRepository.deleteAll(assignments);
      authCacheService.evictUser(user.getId());
    }
  }

  public void removeUser(Long userId, Long roleId) {
    UserEntity user = userService.getUserById(userId);
    RoleEntity role = this.getRole(roleId);

    List<UserRoleEntity> assignments = this.userRoleRepository.findAllByUserAndRole(user, role);

    if (!assignments.isEmpty()) {
      this.userRoleRepository.deleteAll(assignments);
      authCacheService.evictUser(user.getId());
    }
  }

  public Set<RoleEntity> getUserRoles(Long userId) {
    UserEntity user = userService.getUserById(userId);
    Set<RoleEntity> roles = this.getUserRoles(user);
    return roles;
  }

  // -------------------------
  // User-Role Operations
  // -------------------------

  public Set<UserEntity> getRoleUsers(Long roleId) {
    RoleEntity role = this.getRoleById(roleId);
    Set<UserEntity> users = this.getRoleUsers(role);
    return users;
  }

  private Set<UserEntity> getRoleUsers(RoleEntity role) {
    Set<UserEntity> users = new LinkedHashSet<>();
    for (UserRoleEntity userRole : role.getUserRoles()) {
      users.add(userRole.getUser());
    }
    return users;
  }
}
