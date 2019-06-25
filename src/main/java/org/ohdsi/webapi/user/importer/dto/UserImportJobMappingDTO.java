package org.ohdsi.webapi.user.importer.dto;

import org.ohdsi.webapi.user.importer.model.RoleGroupMapping;

public class UserImportJobMappingDTO extends UserImportJobDTO {

  private RoleGroupMapping roleGroupMapping;

  public RoleGroupMapping getRoleGroupMapping() {
    return roleGroupMapping;
  }

  public void setRoleGroupMapping(RoleGroupMapping roleGroupMapping) {
    this.roleGroupMapping = roleGroupMapping;
  }
}
