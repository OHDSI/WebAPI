package org.ohdsi.webapi.security.provisioning.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.ohdsi.webapi.security.authz.Role;

public class AtlasUserRoles {

  @JsonProperty("login")
  private String login;
  @JsonProperty("displayName")
  private String displayName;
  @JsonProperty("roles")
  private List<Role> roles;
  @JsonProperty("status")
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
