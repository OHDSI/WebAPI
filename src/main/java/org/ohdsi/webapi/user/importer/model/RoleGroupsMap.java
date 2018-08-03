package org.ohdsi.webapi.user.importer.model;

import org.ohdsi.webapi.service.UserService;

import java.util.List;

public class RoleGroupsMap {
  private UserService.Role role;

  private List<LdapGroup> groups;

  public UserService.Role getRole() {
    return role;
  }

  public void setRole(UserService.Role role) {
    this.role = role;
  }

  public List<LdapGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<LdapGroup> groups) {
    this.groups = groups;
  }
}
