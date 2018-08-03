package org.ohdsi.webapi.user.importer.model;

import java.util.List;

public class RoleGroupMapping {

  private String provider;

  private List<RoleGroupsMap> roleGroups;

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public List<RoleGroupsMap> getRoleGroups() {
    return roleGroups;
  }

  public void setRoleGroups(List<RoleGroupsMap> roleGroups) {
    this.roleGroups = roleGroups;
  }
}
