package org.ohdsi.webapi.user.importer.repository;

import org.ohdsi.webapi.user.importer.model.LdapProviderType;

import javax.persistence.AttributeConverter;
import java.util.Objects;

public class LdapProviderTypeConverter implements AttributeConverter<LdapProviderType, String> {
  @Override
  public String convertToDatabaseColumn(LdapProviderType providerType) {
    return Objects.nonNull(providerType) ? providerType.getValue() : null;
  }

  @Override
  public LdapProviderType convertToEntityAttribute(String value) {
    return Objects.nonNull(value) ? LdapProviderType.fromValue(value) : null;
  }
}
