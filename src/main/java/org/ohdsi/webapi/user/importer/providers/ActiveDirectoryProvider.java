package org.ohdsi.webapi.user.importer.providers;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.odysseusinc.arachne.commons.utils.QuoteUtils.dequote;
import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.valueAsList;

@Component
@ConditionalOnProperty("security.ad.url")
public class ActiveDirectoryProvider extends AbstractLdapProvider {

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

  @Value("${security.ad.referral:#{null}}")
  private String referral;

  @Value("${security.ad.ignore.partial.result.exception:false}")
  private Boolean adIgnorePartialResultException;

  @Value("${security.ad.result.count.limit:30000}")
  private Long countLimit;

  @Value("${security.ad.searchFilter}")
  private String adSearchFilter;

  @Value("${security.ad.userImport.loginAttr}")
  private String loginAttr;

  @Value("${security.ad.userImport.usernameAttr}")
  private String usernameAttr;

  private String[] userAttributes;

  private static final Set<String> GROUP_CLASSES = ImmutableSet.of("group");

  private static final Set<String> USER_CLASSES = ImmutableSet.of("user");

  private static final int PAGE_SIZE = 500;

  @Override
  public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(dequote(adUrl));
    contextSource.setBase(dequote(adSearchBase));
    contextSource.setUserDn(dequote(adSystemUsername));
    contextSource.setPassword(dequote(adSystemPassword));
    contextSource.setCacheEnvironmentProperties(false);
    contextSource.setReferral(dequote(referral));
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
    searchControls.setCountLimit(countLimit);
    return searchControls;
  }

  @Override
  public String getSearchUserFilter() {

    return adSearchFilter;
  }

  @Override
  public List<LdapUser> search(String filter, CollectingNameClassPairCallbackHandler<LdapUser> handler) {

    PagedResultsDirContextProcessor pager = new PagedResultsDirContextProcessor(PAGE_SIZE);
    do {
        getLdapTemplate().search(LdapUtils.emptyLdapName(), filter, getUserSearchControls(), handler, pager);
        pager = new PagedResultsDirContextProcessor(PAGE_SIZE, pager.getCookie());

    } while (pager.getCookie() != null && pager.getCookie().getCookie() != null
            && (countLimit == 0 || handler.getList().size() < countLimit));

    return  handler.getList();
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
    return loginAttr;
  }

  @Override
  public String getDistinguishedAttributeName() {
    return "distinguishedName";
  }

  @Override
  public String getDisplayNameAttributeName() {
    return usernameAttr;
  }

  @Override
  public CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(AttributesMapper<LdapUser> attributesMapper) {

    return new AttributesMapperCallbackHandler<>(attributesMapper);
  }

  @Override
  public String getPrincipal() {
      return StringUtils.isNotBlank(adPrincipalSuffix) ? dequote(adSystemUsername) + dequote(adPrincipalSuffix) : dequote(adSystemUsername);
  }

  @Override
  public String getPassword() {
      return dequote(adSystemPassword);
  }
}
