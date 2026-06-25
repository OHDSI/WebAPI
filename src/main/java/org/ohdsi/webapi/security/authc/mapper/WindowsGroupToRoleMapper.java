package org.ohdsi.webapi.security.authc.mapper;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Maps Windows groups (from Kerberos/SPNEGO authentication) to WebAPI role names.
 *
 * PHASE 1 PLACEHOLDER: Currently returns empty set to support the unified login pipeline.
 * Phase 2 will implement actual group-to-role mapping using the sec_role_group table
 * and the Active Directory provider configuration.
 *
 * Windows groups typically come from Active Directory, but the mapping infrastructure
 * is currently entangled with the LDAP import job in the security.provisioning package.
 * Phase 2 will refactor this to provide a clean group-to-role mapping service.
 */
@Component
public class WindowsGroupToRoleMapper {

  private static final Logger log = LoggerFactory.getLogger(WindowsGroupToRoleMapper.class);


  /**
   * Maps Windows groups from the authentication authorities to WebAPI role names.
   *
   * PHASE 1: Returns empty set (no mapping). Phase 2 will implement actual mapping.
   *
   * @param authorities the Spring authorities from Windows authentication (group names)
   * @return Empty set of role names (placeholder for phase 1)
   */
  public Set<String> mapGroupsToRoles(java.util.Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null || authorities.isEmpty()) {
      return Collections.emptySet();
    }

    // PHASE 1: No group mapping - users will be assigned only default roles
    log.debug("Windows group mapping not yet implemented - phase 1 placeholder");
    return Collections.emptySet();
  }

  /**
   * Maps Windows group names to WebAPI role names.
   *
   * PHASE 1: Returns empty set (no mapping). Phase 2 will implement actual mapping.
   *
   * Windows groups are typically stored in Active Directory.
   *
   * @param groupNames the Windows group names to map
   * @return Empty set of role names (placeholder for phase 1)
   */
  public Set<String> mapGroupsToRoles(Set<String> groupNames) {
    if (groupNames == null || groupNames.isEmpty()) {
      return Collections.emptySet();
    }

    // PHASE 1: No group mapping - users will be assigned only default roles
    return Collections.emptySet();
  }
}
