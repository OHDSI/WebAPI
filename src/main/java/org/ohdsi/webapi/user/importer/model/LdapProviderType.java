package org.ohdsi.webapi.user.importer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum LdapProviderType {
  ACTIVE_DIRECTORY("ad"), LDAP("ldap");

  private String value;

  LdapProviderType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static LdapProviderType fromValue(String value) {
    for(LdapProviderType provider : values()) {
      if (Objects.equals(provider.value, value)) {
        return provider;
      }
    }
    return null;
  }
}
