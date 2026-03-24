package org.ohdsi.webapi.security.authz;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;

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
  private final SourceRepository sourceRepository;
  private final boolean securityDisabled;

  public AuthorizationService(
      AuthorizationCacheService authorizationCacheService,
      UserService userService,
      RoleService roleService,
      PermissionService permissionService,
      JdbcTemplate jdbcTemplate,
      SourceRepository sourceRepository,
      @Value("${security.provider:DisabledSecurity}") String securityProvider) {

    this.authorizationCacheService = authorizationCacheService;
    this.userService = userService;
    this.roleService = roleService;
    this.permissionService = permissionService;
    this.sourceRepository = sourceRepository;
    this.securityDisabled = "DisabledSecurity".equals(securityProvider);
  }

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

  public Set<PermissionEntity> getUserPermissions(Long userId) {
    Set<RoleEntity> roles = this.roleService.getUserRoles(userId);
    Set<PermissionEntity> perms = new HashSet<>();
    for (RoleEntity r : roles) {
      perms.addAll(this.roleService.getRolePermissions(r.getId()));
    }
    return perms;
  }

  public Set<String> queryUserPermissions(Long userId) {
    return this.authorizationCacheService.getUserAuthorizations(userId).permissions;
  }

  public Set<RoleEntity> getUserRoles(Long userId) throws Exception {
    return this.roleService.getUserRoles(userId);
  }

  public Set<RoleEntity> getUserRoles(String login) throws Exception {
    UserEntity user = this.userService.getUserByLogin(login).orElseThrow();
    return this.roleService.getUserRoles(user);
  }

  public RoleEntity addRole(String roleName, boolean isSystem) {
    return this.roleService.addRole(roleName, isSystem);
  }

  public RoleEntity getCurrentUserPersonalRole() {
    WebApiPrincipal principal = getAuthenticatedPrincipal();
    return this.roleService.getRoleByName(principal.getName(), false).orElseThrow();
  }

  public void addPermissionsFromTemplate(RoleEntity role, Map<String, String> template, String roleIdStr) {
    Long targetRoleId = Long.parseLong(roleIdStr);
    for (Map.Entry<String, String> e : template.entrySet()) {
      String permission = String.format(e.getKey(), roleIdStr);
      String description = String.format(e.getValue(), roleIdStr);
      PermissionEntity p = this.permissionService.getOrAddPermission(permission, description);
      this.roleService.addPermission(targetRoleId, p.getId());
    }
  }

  public RoleEntity getRole(Long id) {
    return this.roleService.getRole(id);
  }

  public RoleEntity updateRole(RoleEntity roleEntity) {
    return this.roleService.updateRole(roleEntity);
  }

  public Iterable<RoleEntity> getRoles(boolean includePersonalRoles) {
    return this.roleService.getRoles(includePersonalRoles);
  }

  public void removeRole(Long roleId) {
    this.roleService.removeRole(roleId);
  }

  public Set<PermissionEntity> getRolePermissions(Long roleId) {
    return this.roleService.getRolePermissions(roleId);
  }

  public void addPermission(Long roleId, Long permissionId) {
    this.roleService.addPermission(roleId, permissionId);
  }

  public void removePermission(Long permissionId, Long roleId) {
    this.roleService.removePermission(permissionId, roleId);
  }

  public Set<UserEntity> getRoleUsers(Long roleId) {
    return this.roleService.getRoleUsers(roleId);
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
    if (securityDisabled) {
      return true;
    }
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
    if (securityDisabled) {
      return true;
    }
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
    if (securityDisabled) {
      return true;
    }
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
    if (securityDisabled) {
      return true;
    }
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
