package org.ohdsi.webapi.security.authc.mapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.Role;
import org.ohdsi.webapi.security.authz.RoleEntity;
import org.ohdsi.webapi.security.authz.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing external role mappings.
 * Handles both administrative operations (CRUD) and runtime role resolution during login.
 */
@Service
@Transactional
public class ExternalRoleMapService {

  private final ExternalRoleMapRepository externalRoleMapRepository;
  private final RoleRepository roleRepository;
  private final AuthorizationService authorizationService;

  public ExternalRoleMapService(ExternalRoleMapRepository externalRoleMapRepository,
                                RoleRepository roleRepository,
                                AuthorizationService authorizationService) {
    this.externalRoleMapRepository = externalRoleMapRepository;
    this.roleRepository = roleRepository;
    this.authorizationService = authorizationService;
  }

  /**
   * Create a new external role mapping.
   *
   * @param origin the authentication origin (LDAP, OIDC, WINDOWS, etc.)
   * @param externalClaim the claim/group/identifier value from the external source
   * @param roleId the WebAPI role ID to map to
   * @param description optional description of the mapping
   * @return the created mapping
   * @throws IllegalArgumentException if the role doesn't exist or mapping already exists
   */
  public ExternalRoleMap createMapping(UserOrigin origin, String externalClaim, Long roleId, String description) {
    RoleEntity roleEntity = roleRepository.findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

    ExternalRoleMapEntity entity = new ExternalRoleMapEntity(origin, externalClaim, roleEntity, description);
    ExternalRoleMapEntity saved = externalRoleMapRepository.save(entity);
    return ExternalRoleMap.fromEntity(saved);
  }

  /**
   * Create a new external role mapping without description.
   *
   * @param origin the authentication origin
   * @param externalClaim the claim/group/identifier value
   * @param roleId the WebAPI role ID
   * @return the created mapping
   */
  public ExternalRoleMap createMapping(UserOrigin origin, String externalClaim, Long roleId) {
    return createMapping(origin, externalClaim, roleId, null);
  }

  /**
   * Remove an external role mapping.
   *
   * @param mappingId the mapping ID to remove
   */
  public void removeMapping(Integer mappingId) {
    externalRoleMapRepository.deleteById(mappingId);
  }

  /**
   * Get all mappings for a specific authentication origin.
   *
   * @param origin the authentication origin
   * @return list of all mappings for that origin
   */
  public List<ExternalRoleMap> getMappingsForOrigin(UserOrigin origin) {
    return externalRoleMapRepository.findByOrigin(origin).stream()
        .map(ExternalRoleMap::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get all mappings for a specific authentication origin.
   *
   * @param origin the authentication origin as string
   * @return list of all mappings for that origin
   */
  public List<ExternalRoleMap> getMappingsForOrigin(String origin) {
    return getMappingsForOrigin(UserOrigin.valueOf(origin));
  }

  /**
   * Resolve external claims to WebAPI role names.
   * Used during login to map external identities (groups, claims) to roles.
   *
   * @param origin the authentication origin
   * @param externalClaims collection of claim values to resolve (group DNs, claim values, etc.)
   * @return set of WebAPI role names that the claims map to
   */
  @Transactional(readOnly = true)
  public Set<String> resolveRoleNames(UserOrigin origin, Collection<String> externalClaims) {
    if (externalClaims == null || externalClaims.isEmpty()) {
      return new HashSet<>();
    }

    return externalRoleMapRepository.findByOriginAndExternalClaimIn(origin, externalClaims)
        .stream()
        .map(mapping -> mapping.getRole().getName())
        .collect(Collectors.toSet());
  }

  /**
   * Sync user roles from a specific authentication origin.
   * Removes roles from that origin that user no longer has, adds new ones they do have.
   * Never modifies roles from other origins (e.g., SYSTEM origin remains untouched).
   *
   * @param login the WebAPI user login name
   * @param origin the authentication origin to sync
   * @param mappedRoleNames set of role names user should have from this origin
   */
  public void syncUserRoles(String login, UserOrigin origin, Set<String> mappedRoleNames) {
    if (mappedRoleNames == null) {
      mappedRoleNames = new HashSet<>();
    }

    // Get all current roles for this user from this specific origin
    List<String> currentRoleNames = authorizationService.getRolesByOrigin(login, origin);
    Set<String> currentRoles = new HashSet<>(currentRoleNames);

    // Determine which roles to remove (in current but not in mapped)
    Set<String> toRemove = new HashSet<>(currentRoles);
    toRemove.removeAll(mappedRoleNames);

    // Determine which roles to add (in mapped but not in current)
    Set<String> toAdd = new HashSet<>(mappedRoleNames);
    toAdd.removeAll(currentRoles);

    // Remove roles no longer in external source
    for (String roleName : toRemove) {
      authorizationService.removeUserFromRole(roleName, login, origin);
    }

    // Add new roles
    for (String roleName : toAdd) {
      authorizationService.addUserToRole(roleName, login, origin);
    }
  }


}
