package org.ohdsi.webapi.security.authz.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.provisioning.model.LdapProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

/**
 * Maps LDAP groups (from authentication authorities) to WebAPI role names.
 *
 * Queries the sec_external_role_map table for mappings between external LDAP claims
 * (group DNs, scopes, etc.) and WebAPI role names. Returns the set of role names
 * that should be assigned to the authenticating user.
 *
 * Not a Spring bean - instantiated inline in LoginController.Ldap to avoid bean namespace pollution.
 */
public class LdapGroupToRoleMapper {

  private static final Logger log = LoggerFactory.getLogger(LdapGroupToRoleMapper.class);
  private final ExternalRoleMapService externalRoleMapService;

  public LdapGroupToRoleMapper(ExternalRoleMapService externalRoleMapService) {
    this.externalRoleMapService = externalRoleMapService;
  }

  /**
   * Maps LDAP groups from the authentication authorities to WebAPI role names.
   *
   * Extracts group names from Spring authorities and queries the external role mapping
   * table to find matching WebAPI roles.
   *
   * @param authorities the Spring authorities from LDAP authentication (usually group names)
   * @param providerType the type of LDAP provider (LDAP or ACTIVE_DIRECTORY)
   * @return set of WebAPI role names mapped for these LDAP groups
   */
  public Set<String> mapGroupsToRoles(Collection<? extends GrantedAuthority> authorities, LdapProviderType providerType) {
    if (authorities == null || authorities.isEmpty()) {
      return Collections.emptySet();
    }
    
    // Extract group names from authorities
    Set<String> groupNames = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
    
    return mapGroupsToRoles(groupNames, providerType);
  }

  /**
   * Maps LDAP group names to WebAPI role names.
   *
   * Queries the external role mapping table for LDAP mappings and returns the
   * set of WebAPI role names that correspond to the provided group names.
   *
   * @param groupNames the LDAP group names to map
   * @param providerType the type of LDAP provider (LDAP or ACTIVE_DIRECTORY)
   * @return set of WebAPI role names mapped for these LDAP group names
   */
  public Set<String> mapGroupsToRoles(Set<String> groupNames, LdapProviderType providerType) {
    if (groupNames == null || groupNames.isEmpty()) {
      return Collections.emptySet();
    }
    
    // Query the mapping table for LDAP-origin mappings
    return externalRoleMapService.resolveRoleNames(UserOrigin.LDAP, groupNames);
  }
}
