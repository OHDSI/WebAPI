package org.ohdsi.webapi.user.importer.model;

import java.util.List;

public class LdapUser extends LdapObject {

  private List<LdapGroup> groups;

  private String login;

  public LdapUser() {
  }

  public LdapUser(String displayName, String distinguishedName) {
    super(displayName, distinguishedName);
  }

  public List<LdapGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<LdapGroup> groups) {
    this.groups = groups;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }
}
