package org.ohdsi.webapi.security.provisioning.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.ohdsi.webapi.security.authz.Role;

public class RoleGroupsMap {
  @JsonProperty("role")
  private Role role;

  @JsonProperty("groups")
  private List<LdapGroup> groups;

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public List<LdapGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<LdapGroup> groups) {
    this.groups = groups;
  }
}
