package org.ohdsi.webapi.user.importer.providers;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.getCriteria;
import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.valueAsString;

@Component
@ConditionalOnProperty("security.ldap.url")
public class DefaultLdapProvider extends AbstractLdapProvider {

  private static final String DN = "DN";
  private static final String[] RETURNING_ATTRS = {DN, "cn", "ou"};
  private static final String[] USER_ATTRIBUTES = {DN, "uid", "cn"};
  @Value("${security.ldap.url}")
  private String ldapUrl;

  @Value("${security.ldap.baseDn}")
  private String baseDn;

  @Value("${security.ldap.system.username}")
  private String systemUsername;

  @Value("${security.ldap.referral:#{null}}")
  private String referral;

  @Value("${security.ldap.system.password}")
  private String systemPassword;

  @Value("${security.ldap.ignore.partial.result.exception:false}")
  private Boolean ldapIgnorePartialResultException;

  @Value("${security.ldap.userImport.loginAttr}")
  private String loginAttr;

  @Value("${security.ldap.userImport.usernameAttr}")
  private String usernameAttr;

  private String[] userAttributes;

  private static final Set<String> GROUP_CLASSES = ImmutableSet.of("groupOfUniqueNames", "groupOfNames", "posixGroup");

  private static final Set<String> USER_CLASSES = ImmutableSet.of("account", "person");

  @PostConstruct
  private void init() {
    List<String> attrs = Arrays.stream(USER_ATTRIBUTES)
            .collect(Collectors.toList());
    attrs.add(usernameAttr);
    attrs.add(loginAttr);
    userAttributes = attrs.stream()
            .distinct().toArray(String[]::new);
  }

  @Override
  public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(ldapUrl);
    contextSource.setBase(baseDn);
    contextSource.setUserDn(systemUsername);
    contextSource.setPassword(systemPassword);
    contextSource.setReferral(referral);
    contextSource.setCacheEnvironmentProperties(false);
    contextSource.setAuthenticationSource(new AuthenticationSource() {
      @Override
      public String getPrincipal() {
        return systemUsername;
      }

      @Override
      public String getCredentials() {
        return systemPassword;
      }
    });

    LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
    ldapTemplate.setIgnorePartialResultException(ldapIgnorePartialResultException);
    return ldapTemplate;
  }

  @Override
  public List<LdapGroup> getLdapGroups(Attributes attributes) throws NamingException {

    String dn = valueAsString(attributes.get(DN));
    if (StringUtils.isNotEmpty(dn)) {
      LdapTemplate template = getLdapTemplate();
      AndFilter filter = new AndFilter();
      filter.and(getCriteria("objectclass", getGroupClasses()));
      OrFilter memberFilter = new OrFilter();
      memberFilter.or(new EqualsFilter("uniqueMember", dn));
      memberFilter.or(new EqualsFilter("member", dn));
      filter.and(memberFilter);
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      searchControls.setReturningAttributes(RETURNING_ATTRS);
      return template.search(LdapUtils.emptyLdapName(), filter.encode(), searchControls,
              (AttributesMapper<LdapGroup>) attrs -> new LdapGroup(valueAsString(attrs.get("ou")), valueAsString(attrs.get(DN))));
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public SearchControls getUserSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setReturningAttributes(userAttributes);
    return searchControls;
  }

  @Override
  public String getSearchUserFilter() {

    return null;
  }

  @Override
  public List<LdapUser> search(String filter, CollectingNameClassPairCallbackHandler<LdapUser> handler) {

    getLdapTemplate().search(LdapUtils.emptyLdapName(), filter, getUserSearchControls(), handler);
    return handler.getList();
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
    return DN;
  }

  @Override
  public String getDisplayNameAttributeName() {
    return usernameAttr;
  }

  @Override
  public CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(AttributesMapper<LdapUser> attributesMapper) {

    return new OpenLdapAttributesMapper<>(attributesMapper);
  }

  @Override
  public String getPrincipal() {
    return systemUsername;
  }

  @Override
  public String getPassword() {
    return systemPassword;
  }

  public static class OpenLdapAttributesMapper<T> extends CollectingNameClassPairCallbackHandler<T> {

    private AttributesMapper<T> mapper;

    public OpenLdapAttributesMapper(AttributesMapper<T> mapper) {
      this.mapper = mapper;
    }

    @Override
    public T getObjectFromNameClassPair(NameClassPair nameClassPair) throws NamingException {

      if (!(nameClassPair instanceof SearchResult)) {
        throw new IllegalArgumentException("Parameter must be an instance of SearchResult");
      } else {
        SearchResult searchResult = (SearchResult)nameClassPair;
        Attributes attributes = searchResult.getAttributes();
        attributes.put(DN, searchResult.getNameInNamespace());

        try {
          return this.mapper.mapFromAttributes(attributes);
        } catch (NamingException var5) {
          throw LdapUtils.convertLdapException(var5);
        }
      }
    }
  }
}
