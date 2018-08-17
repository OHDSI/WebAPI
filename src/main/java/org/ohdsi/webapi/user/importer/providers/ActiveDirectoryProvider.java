package org.ohdsi.webapi.user.importer.providers;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.valueAsList;

@Component
@ConditionalOnProperty("security.ad.url")
public class ActiveDirectoryProvider implements LdapProvider {

  @Value("${security.ad.url}")
  private String adUrl;

  @Value("${security.ad.searchBase}")
  private String adSearchBase;

  @Value("${security.ad.principalSuffix}")
  private String adPrincipalSuffix;

  @Value("${security.ad.system.username}")
  private String adSystemUsername;

  @Value("${security.ad.system.password}")
  private String adSystemPassword;

  @Value("${security.ad.ignore.partial.result.exception}")
  private Boolean adIgnorePartialResultException;

  private static final Set<String> GROUP_CLASSES = ImmutableSet.of("group");

  private static final Set<String> USER_CLASSES = ImmutableSet.of("user");

  @Override
  public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(adUrl);
    contextSource.setBase(adSearchBase);
    contextSource.setUserDn(adSystemUsername);
    contextSource.setPassword(adSystemPassword);
    contextSource.setCacheEnvironmentProperties(false);
    contextSource.setAuthenticationStrategy(new SimpleDirContextAuthenticationStrategy());
    contextSource.setAuthenticationSource(new AuthenticationSource() {
      @Override
      public String getPrincipal() {
        return ActiveDirectoryProvider.this.getPrincipal();
      }

      @Override
      public String getCredentials() {
        return ActiveDirectoryProvider.this.getPassword();
      }
    });
    LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
    ldapTemplate.setIgnorePartialResultException(adIgnorePartialResultException);
    return ldapTemplate;
  }

  @Override
  public List<LdapGroup> getLdapGroups(Attributes attributes) throws NamingException {
    return valueAsList(attributes.get("memberOf")).stream()
            .map(v -> new LdapGroup("", v))
            .collect(Collectors.toList());
  }

  @Override
  public SearchControls getUserSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    return searchControls;
  }

  @Override
  public Set<String> getGroupClasses() {
    return GROUP_CLASSES;
  }

  @Override
  public Set<String> getUserClass() {
    return USER_CLASSES;
  }

  @Override
  public String getLoginAttributeName() {
    return "sAMAccountName";
  }

  @Override
  public String getDistinguishedAttributeName() {
    return "distinguishedName";
  }

  @Override
  public String getDisplayNameAttributeName() {
    return "cn";
  }

  @Override
  public CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(AttributesMapper<LdapUser> attributesMapper) {

    return new AttributesMapperCallbackHandler<>(attributesMapper);
  }

  @Override
  public String getPrincipal() {
    return StringUtils.isNotBlank(adPrincipalSuffix) ? adSystemUsername + adPrincipalSuffix : adSystemUsername;
  }

  @Override
  public String getPassword() {
    return adSystemPassword;
  }
}
