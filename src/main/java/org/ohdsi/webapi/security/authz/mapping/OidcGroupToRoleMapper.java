package org.ohdsi.webapi.security.authz.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohdsi.webapi.security.authc.UserOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts and maps OIDC token claims to WebAPI role names.
 *
 * Extracts roles from JWT claims using a configurable claim path (e.g., "realm_access.roles")
 * and queries the sec_external_role_map table for mappings between those roles and
 * WebAPI role names. Returns the set of role names that should be assigned to the user.
 */
public class OidcGroupToRoleMapper {

  private static final Logger log = LoggerFactory.getLogger(OidcGroupToRoleMapper.class);
  private final ExternalRoleMapService externalRoleMapService;

  public OidcGroupToRoleMapper(ExternalRoleMapService externalRoleMapService) {
    this.externalRoleMapService = externalRoleMapService;
  }

  /**
   * Extracts and maps OIDC roles from JWT claims to WebAPI role names.
   *
   * Extracts role names from the JWT claims using the provided claim path, then
   * queries the external role mapping table for OIDC mappings and returns the
   * set of WebAPI role names that should be assigned.
   *
   * @param claims the JWT claims map from the OIDC token
   * @param roleClaimPath the dot-separated path to the roles claim (e.g., "realm_access.roles")
   * @param toUpperCase whether to uppercase role names before filtering
   * @return set of WebAPI role names mapped for the extracted OIDC roles
   */
  public Set<String> extractAndMapRoles(Map<String, Object> claims, String roleClaimPath, boolean toUpperCase) {
    if (claims == null || claims.isEmpty()) {
      return Collections.emptySet();
    }
    
    // Extract roles from JWT claims using the configured claim path
    List<String> extractedRoles = extractRoles(claims, roleClaimPath, toUpperCase);
    
    if (extractedRoles.isEmpty()) {
      return Collections.emptySet();
    }
    
    // Query the mapping table for OIDC-origin mappings
    Set<String> roleSet = new HashSet<>(extractedRoles);
    return externalRoleMapService.resolveRoleNames(UserOrigin.OIDC, roleSet);
  }

  /**
   * Extracts roles from JWT claims using a dot-separated claim path.
   *
   * Traverses nested claims (e.g., "realm_access.roles" -> claims["realm_access"]["roles"])
   * and returns a list of role strings. Supports both list and single string values.
   *
   * @param claims the JWT claims map
   * @param claimPath the dot-separated path (e.g., "realm_access.roles")
   * @param toUpperCase whether to uppercase extracted role names
   * @return list of extracted role names, or empty list if not found
   */
  private static List<String> extractRoles(Map<String, Object> claims, String claimPath, boolean toUpperCase) {
    if (claimPath == null || claimPath.isBlank() || claims == null) {
      return List.of();
    }
    
    String[] parts = claimPath.split("\\.");
    Object current = claims;
    
    // Traverse the nested claim path
    for (String part : parts) {
      if (current instanceof Map<?, ?> map) {
        current = map.get(part);
      } else {
        log.debug("OIDC: Cannot traverse claim path '{}' - intermediate value is not a map", claimPath);
        return List.of();
      }
      if (current == null) {
        log.debug("OIDC: Claim '{}' not found in ID token", claimPath);
        return List.of();
      }
    }

    // Extract roles from the final value (can be list or single string)
    if (current instanceof List<?> list) {
      List<String> roles = new java.util.ArrayList<>();
      for (Object item : list) {
        if (item instanceof String s) {
          roles.add(toUpperCase ? s.toUpperCase() : s);
        }
      }
      return roles;
    }
    if (current instanceof String s) {
      return List.of(toUpperCase ? s.toUpperCase() : s);
    }
    
    log.debug("OIDC: Claim '{}' is not a list or string: {}", claimPath, current.getClass().getName());
    return List.of();
  }
}
