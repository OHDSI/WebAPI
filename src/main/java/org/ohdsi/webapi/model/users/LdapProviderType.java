package org.ohdsi.webapi.model.users;

import java.util.Objects;

public enum LdapProviderType {
  ACTIVE_DIRECTORY("ad"), LDAP("ldap");

  private String value;

  LdapProviderType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static LdapProviderType fromValue(String value) {
    for(LdapProviderType provider : values()) {
      if (Objects.equals(provider.value, value)) {
        return provider;
      }
    }
    return null;
  }
}
