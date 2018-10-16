package org.ohdsi.webapi.user.importer.model;

import org.ohdsi.webapi.model.Role;

import java.util.List;

public class AtlasUserRoles {

  private String login;
  private String displayName;
  private List<Role> roles;
  private LdapUserImportStatus status;

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

  public LdapUserImportStatus getStatus() {
    return status;
  }

  public void setStatus(LdapUserImportStatus status) {
    this.status = status;
  }
}
