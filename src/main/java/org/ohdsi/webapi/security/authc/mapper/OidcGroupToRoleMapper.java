package org.ohdsi.webapi.security.authc.mapper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Extracts and maps OIDC token claims to WebAPI role names.
 *
 * PHASE 1 PLACEHOLDER: Currently returns empty set to support the unified login pipeline.
 * Phase 2 will implement actual role claim extraction and filtering.
 *
 * The mapping infrastructure needs further clarification about which token formats
 * are supported and how to safely extract roles from various OIDC providers.
 */
@Component
public class OidcGroupToRoleMapper {

  private static final Logger log = LoggerFactory.getLogger(OidcGroupToRoleMapper.class);

  /**
   * Extracts and maps OIDC roles from JWT claims to WebAPI role names.
   *
   * PHASE 1: Returns empty set (no mapping). Phase 2 will implement actual extraction.
   *
   * @param claims the JWT claims map from the OIDC token
   * @param roleClaimPath the dot-separated path to the roles claim (e.g., "realm_access.roles")
   * @param toUpperCase whether to uppercase role names before filtering
   * @return Empty set of role names (placeholder for phase 1)
   */
  public Set<String> extractAndMapRoles(Map<String, Object> claims, String roleClaimPath, boolean toUpperCase) {
    if (claims == null || claims.isEmpty()) {
      return Collections.emptySet();
    }
    
    // PHASE 1: No role claim extraction - users will be assigned only default roles
    log.debug("OIDC role claim extraction not yet implemented - phase 1 placeholder");
    return Collections.emptySet();
  }
}
