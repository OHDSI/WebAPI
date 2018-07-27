package org.ohdsi.webapi.service.userimport;

import org.ohdsi.webapi.model.users.LdapGroup;
import org.ohdsi.webapi.model.users.LdapProviderType;
import org.ohdsi.webapi.service.providers.ActiveDirectoryProvider;
import org.ohdsi.webapi.service.providers.DefaultLdapProvider;
import org.ohdsi.webapi.service.providers.LdapProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.*;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Component
public class DefaultUserImportService implements UserImportService {

  private final Map<LdapProviderType, LdapProvider> providersMap = new HashMap<>();

  public DefaultUserImportService(@Autowired(required = false) ActiveDirectoryProvider activeDirectoryProvider,
                                  @Autowired(required = false) DefaultLdapProvider ldapProvider) {

    Optional.ofNullable(activeDirectoryProvider).ifPresent(provider -> providersMap.put(LdapProviderType.ACTIVE_DIRECTORY, provider));
    Optional.ofNullable(ldapProvider).ifPresent(provider -> providersMap.put(LdapProviderType.LDAP, provider));
  }

  protected Optional<LdapProvider> getProvider(LdapProviderType type) {

    return Optional.ofNullable(providersMap.get(type));
  }

  @Override
  public LdapTemplate getLdapTemplate(LdapProviderType providerType) {

    return getProvider(providerType).orElseThrow(IllegalArgumentException::new).getLdapTemplate();
  }

  @Override
  public List<LdapGroup> findGroups(LdapProviderType type, String searchStr) {

    LdapTemplate ldapTemplate = getLdapTemplate(type);
    return ldapTemplate.search(query().where("objectclass").is("group")
            .and("cn").whitespaceWildcardsLike(searchStr), (AttributesMapper<LdapGroup>)  attributes -> {
      String name = valueAsString(attributes.get("cn"));
      String dn = valueAsString(attributes.get("distinguishedName"));
      return new LdapGroup(name, dn);
    });
  }

  private String valueAsString(Attribute attribute) throws NamingException {
    return Objects.nonNull(attribute) ? attribute.get().toString() : "";
  }
}
