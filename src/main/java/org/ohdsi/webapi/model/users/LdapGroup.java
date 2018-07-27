package org.ohdsi.webapi.model.users;

public class LdapGroup {
  private String displayName;
  private String distinguishedName;

  public LdapGroup() {
  }

  public LdapGroup(String displayName, String distinguishedName) {
    this.displayName = displayName;
    this.distinguishedName = distinguishedName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDistinguishedName() {
    return distinguishedName;
  }

  public void setDistinguishedName(String distinguishedName) {
    this.distinguishedName = distinguishedName;
  }
}
