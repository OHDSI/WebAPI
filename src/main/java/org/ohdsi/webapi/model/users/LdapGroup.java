package org.ohdsi.webapi.model.users;

public class LdapGroup extends LdapObject {

  public LdapGroup() {
  }

  public LdapGroup(String displayName, String distinguishedName) {
    super(displayName, distinguishedName);
  }

}
