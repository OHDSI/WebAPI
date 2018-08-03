package org.ohdsi.webapi.user.importer.model;

public class LdapGroup extends LdapObject {

  public LdapGroup() {
  }

  public LdapGroup(String displayName, String distinguishedName) {
    super(displayName, distinguishedName);
  }

}
