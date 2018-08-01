package org.ohdsi.webapi.userimport.services.providers;


import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.userimport.model.LdapGroup;
import org.ohdsi.webapi.userimport.model.LdapUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ohdsi.webapi.userimport.services.providers.OhdsiLdapUtils.getCriteria;
import static org.ohdsi.webapi.userimport.services.providers.OhdsiLdapUtils.valueAsString;

@Component
@ConditionalOnProperty("security.ldap.url")
public class DefaultLdapProvider implements LdapProvider {

  private static final String DN = "DN";
  private static final String[] RETURNING_ATTRS = {DN, "cn", "ou"};
  private static final String[] USER_ATTRIBUTES = {DN, "uid", "cn"};
  @Value("${security.ldap.url}")
  private String ldapUrl;

  @Value("${security.ldap.baseDn}")
  private String baseDn;

  @Value("${security.ldap.system.username}")
  private String systemUsername;

  @Value("${security.ldap.system.password}")
  private String systemPassword;

  private static final Set<String> GROUP_CLASSES = ImmutableSet.of("groupOfUniqueNames", "groupOfNames");

  private static final Set<String> USER_CLASSES = ImmutableSet.of("account", "person");

  @Override
  public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(ldapUrl);
    contextSource.setBase(baseDn);
    contextSource.setUserDn(systemUsername);
    contextSource.setPassword(systemPassword);
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

    return new LdapTemplate(contextSource);
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
    searchControls.setReturningAttributes(USER_ATTRIBUTES);
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
    return "uid";
  }

  @Override
  public String getDistinguishedAttributeName() {
    return DN;
  }

  @Override
  public String getDisplayNameAttributeName() {
    return "cn";
  }

  @Override
  public CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(AttributesMapper<LdapUser> attributesMapper) {

    return new OpenLdapAttributesMapper<>(attributesMapper);
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
