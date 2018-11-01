package org.ohdsi.webapi.user.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class LdapObject {
  @JsonProperty("displayName")
  private String displayName;
  @JsonProperty("distinguishedName")
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
