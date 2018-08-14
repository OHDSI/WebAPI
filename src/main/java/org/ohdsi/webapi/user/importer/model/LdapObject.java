package org.ohdsi.webapi.user.importer.model;

public abstract class LdapObject {
  private String displayName;
  private String distinguishedName;

  public LdapObject() {
  }

  public LdapObject(String displayName, String distinguishedName) {
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
