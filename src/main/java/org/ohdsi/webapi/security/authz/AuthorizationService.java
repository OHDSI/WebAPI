package org.ohdsi.webapi.security.authz;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.security.authz.access.EntityAccessService;
import org.ohdsi.webapi.security.authz.access.EntityGrant;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.ohdsi.webapi.security.authz.access.UserAuthorizations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * The AuthorizatonService is part of security.authz which orchastrates the permission assignments for users, roles, and permisisons
 * AuthoriationService will serve as a fascade to the underlying package-local services that will freely exchange JPA Entities.
 */
@Service
public class AuthorizationService {

  private final AuthorizationCacheService authorizationCacheService;
  private final UserService userService;
  private final RoleService roleService;
  private final PermissionService permissionService;
  private final EntityAccessService entityAccessService;
  private final SourceRepository sourceRepository;

  public AuthorizationService(
      AuthorizationCacheService authorizationCacheService,
      UserService userService,
      RoleService roleService,
      PermissionService permissionService,
      EntityAccessService entityAccessService,
      JdbcTemplate jdbcTemplate,
      SourceRepository sourceRepository) {

    this.authorizationCacheService = authorizationCacheService;
    this.userService = userService;
    this.roleService = roleService;
    this.permissionService = permissionService;
    this.entityAccessService = entityAccessService;
    this.sourceRepository = sourceRepository;
  }

  @Value("${security.auth.ad.default.import.group}#{T(java.util.Collections).emptyList()}")
  private List<String> defaultRoles;

  // -------------------------
  // Compatibility / Facade Wrappers
  // These methods provide the higher-level operations previously exposed
  // by legacy PermissionManager so callers in the web layer can migrate
  // to the new authz package.

  public Iterable<User> getUsers() {
    Iterable<UserEntity> ents = this.userService.getAllUsers();
    java.util.ArrayList<User> users = new java.util.ArrayList<>();
    for (UserEntity e : ents) {
      users.add(User.fromEntity(e));
    }
    return users;
  }

  public User getCurrentUser() {
    WebApiPrincipal principal = getAuthenticatedPrincipal();
    UserEntity ue = this.userService.getUserById(principal.getUserId());
    return User.fromEntity(ue);
  }

  @Transactional(readOnly = true)
  public List<Permission> getUserPermissions(Long userId) {
    Set<RoleEntity> roles = this.roleService.getUserRoles(userId);
    Set<PermissionEntity> perms = new HashSet<>();
    for (RoleEntity r : roles) {
      perms.addAll(this.roleService.getRolePermissions(r.getId()));
    }
    return perms.stream().map(Permission::fromEntity).collect(Collectors.toList());
  }

  public Set<String> queryUserPermissions(Long userId) {
    return this.authorizationCacheService.getUserAuthorizations(userId).permissions;
  }

  @Transactional(readOnly = true)
  public List<Role> getUserRoles(Long userId) throws Exception {
    Set<RoleEntity> roleEntities = this.roleService.getUserRoles(userId);
    ArrayList<Role> roles = new ArrayList<>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = Role.fromEntity(roleEntity, defaultRoles.contains(roleEntity.getName()));
      roles.add(role);
    }
    return roles;
  }

  @Transactional(readOnly = true)
  public List<Role> getUserRoles(String login) throws Exception {
    UserEntity user = this.userService.getUserByLogin(login).orElseThrow();
    Set<RoleEntity> roleEntities = this.roleService.getUserRoles(user);
    ArrayList<Role> roles = new ArrayList<>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = Role.fromEntity(roleEntity, defaultRoles.contains(roleEntity.getName()));
      roles.add(role);
    }
    return roles;
  }

  public RoleEntity addRole(String roleName, boolean isSystem) {
    return this.roleService.addRole(roleName, isSystem);
  }

  public RoleEntity getCurrentUserPersonalRole() {
    WebApiPrincipal principal = getAuthenticatedPrincipal();
    return this.roleService.getRoleByName(principal.getName(), false).orElseThrow();
  }

  public RoleEntity getRole(Long id) {
    return this.roleService.getRole(id);
  }

  public RoleEntity updateRole(RoleEntity roleEntity) {
    return this.roleService.updateRole(roleEntity);
  }

  public List<Role> getRoles(boolean includePersonalRoles) {
    Iterable<RoleEntity> roleEntities = this.roleService.getRoles(includePersonalRoles);
    ArrayList<Role> roles = new ArrayList<>();
    for (RoleEntity roleEntity : roleEntities) {
      Role role = Role.fromEntity(roleEntity, defaultRoles.contains(roleEntity.getName()));
      roles.add(role);
    }
    return roles;
  }

  public void removeRole(Long roleId) {
    this.roleService.removeRole(roleId);
  }

  @Transactional(readOnly = true)
  public List<Permission> getRolePermissions(Long roleId) {
    Set<PermissionEntity> permissionEntities = this.roleService.getRolePermissions(roleId);
    return permissionEntities.stream().map(Permission::fromEntity).collect(Collectors.toList());
  }

  public void addPermission(Long roleId, Long permissionId) {
    this.roleService.addPermission(roleId, permissionId);
  }

  public void removePermission(Long roleId, Long permissionId) {
    this.roleService.removePermission(roleId, permissionId);
  }

  @Transactional(readOnly = true)
  public List<User> getRoleUsers(Long roleId) {
    Set<UserEntity> userEntities = this.roleService.getRoleUsers(roleId);
    return userEntities.stream().map(User::fromEntity).collect(Collectors.toList());
  }

  public void addUser(Long userId, Long roleId) {
    this.roleService.addUserToRole(userId, roleId);
  }

  public void removeUser(Long userId, Long roleId) {
    this.roleService.removeUser(userId, roleId);
  }

  public void removeUserFromRole(String roleName, String login, UserOrigin origin) {
    this.roleService.removeUserFromRole(login, roleName, origin);
  }

  public void addUserToRole(String roleName, String login, UserOrigin origin) {
    this.roleService.addUserToRole(login, roleName, origin);
  }

  // -------------------------
  // Permission & Entity Access Facade
  // -------------------------

  /**
   * Get all global permissions defined in the system.
   *
   * @return List of Permission domain objects
   */
  @Transactional(readOnly = true)
  public List<Permission> getPermissions() {
    Iterable<PermissionEntity> entities = this.permissionService.getPermissions();
    ArrayList<Permission> permissions = new ArrayList<>();
    for (PermissionEntity e : entities) {
      permissions.add(Permission.fromEntity(e));
    }
    return permissions;
  }

  /**
   * Search for system roles by partial name match (case-insensitive).
   * Returns all system roles if roleSearch is null or empty.
   *
   * @param roleSearch The partial role name to search for
   * @return List of matching Role domain objects
   */
  @Transactional(readOnly = true)
  public List<Role> searchRoles(String roleSearch) {
    List<RoleEntity> roleEntities = this.roleService.searchRoles(roleSearch);
    return roleEntities.stream()
        .map(re -> Role.fromEntity(re, defaultRoles.contains(re.getName())))
        .collect(Collectors.toList());
  }

  /**
   * Find all roles that have a specific access type to an entity.
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @param accessType The access type to filter by (READ or WRITE)
   * @return List of Role domain objects with the specified access
   */
  @Transactional(readOnly = true)
  public List<Role> getRolesForEntity(EntityType entityType, Long entityId, AccessType accessType) {
    List<Long> roleIds = this.entityAccessService.getRoleIdsForEntity(entityType, entityId, accessType);
    return roleIds.stream()
        .map(id -> this.roleService.getRole(id))
        .map(re -> Role.fromEntity(re, defaultRoles.contains(re.getName())))
        .collect(Collectors.toList());
  }

  /**
   * Grant access to an entity for a specific role.
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @param roleId     The role ID to grant access to
   * @param accessType The access type to grant (READ or WRITE)
   */
  @Transactional
  public void grantEntityAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    this.entityAccessService.grantAccess(entityType, entityId, roleId, accessType);
    this.authorizationCacheService.evictUsersWithRole(roleId);
  }

  /**
   * Revoke a specific access type from a role for an entity.
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @param roleId     The role ID to revoke access from
   * @param accessType The access type to revoke (READ or WRITE)
   */
  @Transactional
  public void revokeEntityAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    this.entityAccessService.revokeAccess(entityType, entityId, roleId, accessType);
    this.authorizationCacheService.evictUsersWithRole(roleId);
  }

  // -------------------------
  // Lifecycle / Registration
  // -------------------------

  /**
   * During login, we ensure the user exists in the system.  This was formerly known as 'registerUser'.
   * @param login the login to ensure exists, will create a new user if not
   * @param name the friendly name of this User, will use login if null
   * @param origin Indicates origin of the User
   * @param defaultRoles
   * @return
   */
  @Transactional
  public User ensureUserExists(String login, String name, UserOrigin origin, List<String> defaultRoles) {
    return userService.getUserByLogin(login)
        .map(entity -> updateIfNeeded(entity, name, origin))
        .orElseGet(() -> registerUser(login, name, origin, new HashSet<>(defaultRoles == null ? List.of() : defaultRoles)));
  }

  /**
   * Registers a user by creating the suer (and personal role) and assinging any default roles
   * Will result in an exception of personal role already exists (because that indicates some data issue)
   * @param login the login of the user
   * @param name The name of the user, will use login if null.
   * @param origin Records where this user was authenticated from initialy.
   * @param defaultRoles Sets up default roles to assign to this user.
   * @return
   */
  @Transactional
  public User registerUser(String login, String name, UserOrigin origin, Set<String> defaultRoles) {

    this.roleService.getRoleByName(login, false)
      .ifPresent((role) -> {throw new RuntimeException("Can't create user when role for user %s already exists".formatted(login));});

    UserEntity userEntity = this.createUser(login, name, origin);

    // assign users to the specified default roles
    for (String roleName : defaultRoles) {
      RoleEntity publicRole = roleService.getRoleByName(roleName, true).orElseThrow();
      roleService.addUserToRole(userEntity, publicRole, UserOrigin.SYSTEM);
    }

    return User.fromEntity(userEntity);
  }

  private User updateIfNeeded(UserEntity entity, String name, UserOrigin origin) {
    boolean updated = false;
    if (name != null && !name.equals(entity.getName())) {
      entity.setName(name);
      updated = true;
    }
    if (origin != null && !origin.equals(entity.getOrigin())) {
      entity.setOrigin(origin);
      updated = true;
    }
    if (updated) {
      entity = userService.save(entity);
    }
    return User.fromEntity(entity);
  }  

/**
   * Creates a new user, assigning a personal role to the new user to hold personal permissions.
   * Will result in an exception of personal role already exists (because that indicates some data issue)
   * @param login the login of the user
   * @param name The name of the user, will use login if null.
   * @param origin Records where this user was authenticated from initialy.
   * @param defaultRoles Sets up default roles to assign to this user.
   * @return
   */
  @Transactional
  private UserEntity createUser(String login, String name, UserOrigin origin) {

    // personal role should not exist
    this.roleService.getRoleByName(login, false)
      .ifPresent((role) -> {throw new RuntimeException("Can't create user when role for user %s already exists".formatted(login));});

    UserEntity userEntity = new UserEntity();
    userEntity.setLogin(login);
    userEntity.setName(name != null ? name : login);
    userEntity.setOrigin(origin != null ? origin : UserOrigin.SYSTEM);
    userEntity = userService.save(userEntity);

    // Assign personal role
    RoleEntity roleEntity = roleService.addRole(login, false);

    roleService.addUserToRole(userEntity, roleEntity, origin);

    return userEntity;
  }

  // -------------------------
  // User Authorization Operations
  // -------------------------

 /**
   * Gets the active authentication prinicipal.
   * @return
   */
  public WebApiPrincipal getAuthenticatedPrincipal() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal() == null) {
      return WebApiPrincipal.ANONYMOUS;
    }
    Object principal = auth.getPrincipal();
    if (principal instanceof WebApiPrincipal wap) {
      return wap;
    }
    return WebApiPrincipal.ANONYMOUS;
  }  

  // -------------------------
  // Entity-Level Authorization (for @PreAuthorize SpEL expressions)
  // -------------------------

  /**
   * Check if the current principal is the owner of an entity
   * @param entityId The entity ID
   * @param entityType The type of entity
   * @return true if the principal created the entity
   */
  public boolean isOwner(Long entityId, EntityType entityType) {
    WebApiPrincipal principal = getCurrentPrincipal();
    if (principal == null) {
      return false;
    }

    UserAuthorizations authz = authorizationCacheService.getUserAuthorizations(principal.getUserId());

    return switch (entityType) {
      case COHORT_DEFINITION -> {
        EntityGrant grant = authz.cohortDefinitionAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case CONCEPT_SET -> {
        EntityGrant grant = authz.conceptSetAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case COHORT_CHARACTERIZATION -> {
        EntityGrant grant = authz.cohortCharacterizationAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case FE_ANALYSIS -> {
        EntityGrant grant = authz.feAnalysisAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case INCIDENCE_RATE -> {
        EntityGrant grant = authz.incidenceRateAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case PATHWAY_ANALYSIS -> {
        EntityGrant grant = authz.pathwayAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case REUSABLE -> {
        EntityGrant grant = authz.reusableAccess.get(entityId);
        yield grant != null && grant.isOwner();
      }
      case SOURCE -> false;
    };
  }

  /**
   * Check if the current principal has specific access to an entity.
   * Uses the cached UserAuthorizations per-entity access maps.
   * Does NOT check global permissions (use isPermitted for that).
   * WRITE implies READ: if the user has WRITE, a READ check also passes.
   *
   * @param entityId The entity ID
   * @param entityType The type of entity
   * @param accessType The type of access (READ, WRITE)
   * @return true if the principal has the specified access
   */
  public boolean hasEntityAccess(Long entityId, EntityType entityType, AccessType accessType) {
    WebApiPrincipal principal = getCurrentPrincipal();
    if (principal == null) {
      return false;
    }

    UserAuthorizations authz = authorizationCacheService.getUserAuthorizations(principal.getUserId());

    return switch (entityType) {
      case COHORT_DEFINITION -> {
        EntityGrant grant = authz.cohortDefinitionAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }
      case CONCEPT_SET -> {
        EntityGrant grant = authz.conceptSetAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }
      case COHORT_CHARACTERIZATION -> {
        EntityGrant grant = authz.cohortCharacterizationAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }
      case FE_ANALYSIS -> {
        EntityGrant grant = authz.feAnalysisAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }
      case INCIDENCE_RATE -> {
        EntityGrant grant = authz.incidenceRateAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }
      case PATHWAY_ANALYSIS -> {
        EntityGrant grant = authz.pathwayAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }
      case REUSABLE -> {
        EntityGrant grant = authz.reusableAccess.get(entityId);
        yield grant != null && grant.hasAccess(accessType);
      }

      // infrastructure types that don't have ownership (ie: sources, tools, etc)
      case SOURCE -> {
        Set<AccessType> granted = authz.sourceAccess.get(entityId);
        yield granted != null && EntityGrant.hasAccess(accessType, granted);
      }
    };
  }

  /**
   * Check if the current principal has specific access to a source.
   * This special implemnetation exists because requests can be made by a source key
   * and this method handles resolving it back to an ID and then calling hasEntityAccess.
   *
   * @param sourceKey The source key
   * @param accessType The type of access (READ, WRITE)
   * @return true if the principal has the specified access
   */
  public boolean hasSourceAccess(String sourceKey, AccessType accessType) {
    WebApiPrincipal principal = getCurrentPrincipal();
    if (principal == null) {
      return false;
    }
    Source source = sourceRepository.findBySourceKey(sourceKey);
    if (source == null) {
      return false;
    }
    Long sourceId = source.getId().longValue();
    return hasEntityAccess(sourceId, EntityType.SOURCE, accessType);
  }


  /**
   * Check if the current principal has a wildcard permission (global entitlement)
   * @param permission The permission string (e.g., "read:cohort", "write", "*")
   * @return true if the principal has the permission
   */
  public boolean isPermitted(String permission) {
    WebApiPrincipal principal = getCurrentPrincipal();
    if (principal == null) {
      return false;
    }

    UserAuthorizations authz = authorizationCacheService.getUserAuthorizations(principal.getUserId());
    return WildcardPermission.impliesAny(authz.permissions, permission);
  }

  /**
   * Returns the full cached UserAuthorizations for the current principal.
   * Useful for list endpoints that need to check per-entity access in bulk
   * without repeated cache lookups.
   *
   * @return UserAuthorizations for the current user, or an empty instance if not authenticated
   */
  public UserAuthorizations getCurrentUserAuthorizations() {
    WebApiPrincipal principal = getCurrentPrincipal();
    if (principal == null) {
      return new UserAuthorizations();
    }
    return getUserAuthorizations(principal.getUserId());
  }

  /**
   * Returns the full cached UserAuthorizations for the current principal.
   * Useful for list endpoints that need to check per-entity access in bulk
   * without repeated cache lookups.
   *
   * @return UserAuthorizations for the current user, or an empty instance if not authenticated
   */
  public UserAuthorizations getUserAuthorizations(Long userId) {
    return authorizationCacheService.getUserAuthorizations(userId);
  }

  /**
   * Tells cache service to clear
   */
  public void clearCache() {
    authorizationCacheService.clearCache();
  }

  /**
   * Get the current principal from the security context
   */
  private WebApiPrincipal getCurrentPrincipal() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof WebApiPrincipal principal) {
      return principal;
    }
    return null;
  }

}
