package org.ohdsi.webapi.security.authz.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.ohdsi.webapi.security.authc.UserOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

/**
 * Maps Windows groups (from Kerberos/SPNEGO authentication) to WebAPI role names.
 *
 * Queries the sec_external_role_map table for mappings between external Windows claims
 * (group names, etc.) and WebAPI role names. Returns the set of role names
 * that should be assigned to the authenticating user.
 *
 * Not a Spring bean - instantiated inline in LoginController.Windows to avoid bean namespace pollution.
 */
public class WindowsGroupToRoleMapper {

  private static final Logger log = LoggerFactory.getLogger(WindowsGroupToRoleMapper.class);
  private final ExternalRoleMapService externalRoleMapService;

  public WindowsGroupToRoleMapper(ExternalRoleMapService externalRoleMapService) {
    this.externalRoleMapService = externalRoleMapService;
  }

  /**
   * Maps Windows groups from the authentication authorities to WebAPI role names.
   *
   * Extracts group names from Spring authorities and queries the external role mapping
   * table to find matching WebAPI roles.
   *
   * @param authorities the Spring authorities from Windows authentication (group names)
   * @return set of WebAPI role names mapped for these Windows groups
   */
  public Set<String> mapGroupsToRoles(Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null || authorities.isEmpty()) {
      return Collections.emptySet();
    }

    // Extract group names from authorities
    Set<String> groupNames = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
    
    return mapGroupsToRoles(groupNames);
  }

  /**
   * Maps Windows group names to WebAPI role names.
   *
   * Queries the external role mapping table for Windows mappings and returns the
   * set of WebAPI role names that correspond to the provided group names.
   *
   * Windows groups are typically stored in Active Directory.
   *
   * @param groupNames the Windows group names to map
   * @return set of WebAPI role names mapped for these Windows group names
   */
  public Set<String> mapGroupsToRoles(Set<String> groupNames) {
    if (groupNames == null || groupNames.isEmpty()) {
      return Collections.emptySet();
    }

    // Query the mapping table for Windows-origin mappings
    return externalRoleMapService.resolveRoleNames(UserOrigin.WINDOWS, groupNames);
  }
}
