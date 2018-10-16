package org.ohdsi.webapi.user.importer.model;

import org.ohdsi.webapi.model.Role;

import java.util.List;

public class RoleGroupsMap {
  private Role role;

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
