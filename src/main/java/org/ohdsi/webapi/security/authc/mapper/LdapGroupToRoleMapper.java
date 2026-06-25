package org.ohdsi.webapi.security.authc.mapper;

import java.util.Collections;
import java.util.Set;

import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Maps LDAP groups (from authentication authorities) to WebAPI role names.
 *
 * PHASE 1 PLACEHOLDER: Currently returns empty set to support the unified login pipeline.
 * Phase 2 will implement actual group-to-role mapping using the sec_role_group table
 * and RoleGroupEntity infrastructure.
 *
 * The mapping infrastructure is currently entangled with the LDAP import job in the
 * security.provisioning package. Phase 2 will refactor this to provide a clean
 * group-to-role mapping service.
 */
@Component
public class LdapGroupToRoleMapper {

  private static final Logger log = LoggerFactory.getLogger(LdapGroupToRoleMapper.class);


  /**
   * Maps LDAP groups from the authentication authorities to WebAPI role names.
   *
   * PHASE 1: Returns empty set (no mapping). Phase 2 will implement actual mapping.
   *
   * @param authorities the Spring authorities from LDAP authentication (usually group names)
   * @param providerType the type of LDAP provider (LDAP or ACTIVE_DIRECTORY)
   * @return Empty set of role names (placeholder for phase 1)
   */
  public Set<String> mapGroupsToRoles(java.util.Collection<? extends GrantedAuthority> authorities, LdapProviderType providerType) {
    if (authorities == null || authorities.isEmpty()) {
      return Collections.emptySet();
    }
    
    // PHASE 1: No group mapping - users will be assigned only default roles
    log.debug("LDAP group mapping not yet implemented - phase 1 placeholder");
    return Collections.emptySet();
  }

  /**
   * Maps LDAP group names to WebAPI role names.
   *
   * PHASE 1: Returns empty set (no mapping). Phase 2 will implement actual mapping.
   *
   * @param groupNames the LDAP group names to map
   * @param providerType the type of LDAP provider (LDAP or ACTIVE_DIRECTORY)
   * @return Empty set of role names (placeholder for phase 1)
   */
  public Set<String> mapGroupsToRoles(Set<String> groupNames, LdapProviderType providerType) {
    if (groupNames == null || groupNames.isEmpty()) {
      return Collections.emptySet();
    }
    
    // PHASE 1: No group mapping - users will be assigned only default roles
    return Collections.emptySet();
  }
}
