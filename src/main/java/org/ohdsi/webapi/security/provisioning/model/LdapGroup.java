package org.ohdsi.webapi.security.provisioning.model;

public class LdapGroup extends LdapObject {

  public LdapGroup() {
  }

  public LdapGroup(String displayName, String distinguishedName) {
    super(displayName, distinguishedName);
  }

}
