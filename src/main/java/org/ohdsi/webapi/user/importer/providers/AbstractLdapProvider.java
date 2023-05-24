package org.ohdsi.webapi.user.importer.providers;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapObject;
import org.ohdsi.webapi.user.importer.model.LdapUser;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapUtils;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.getCriteria;
import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.valueAsString;

public abstract class AbstractLdapProvider implements LdapProvider {

  public static final String OBJECTCLASS_ATTR = "objectclass";
  public static final String CN_ATTR = "cn";

  @Override
  public List<LdapGroup> findGroups(String searchStr) {
    LdapTemplate ldapTemplate = getLdapTemplate();
    return ldapTemplate.search(LdapUtils.emptyLdapName(), getFilterString(searchStr), getAttributesMapper(LdapGroup::new));
  }

  private String getFilterString(String searchString) {
    StringBuffer buff = new StringBuffer();
    buff.append('(');
    buff.append(CN_ATTR).append("=").append(encodeSearchString(searchString));
    buff.append(')');
    return buff.toString();
  }

  private String encodeSearchString(String searchString) {
    // encode search string without encoding wildcards
    if (searchString == null) {
      return null;
    }
    char wildcard = '*';
    List<Integer> wildcardIndexes = IntStream.range(0, searchString.length())
            .filter(i -> searchString.charAt(i) == wildcard).boxed()
            .collect(Collectors.toList());
    StringBuffer buff = new StringBuffer();
    if (wildcardIndexes.isEmpty()) {
      buff.append(LdapEncoder.filterEncode(searchString));
    } else {
      int previousWildcardIndex = -1;
      for (int i = 0; i <= wildcardIndexes.size() - 1; i++) {
        int wildcardIndex = wildcardIndexes.get(i);
        if (wildcardIndex > previousWildcardIndex + 1 ) {
          String substring = searchString.substring(previousWildcardIndex + 1, wildcardIndex);
          buff.append(LdapEncoder.filterEncode(substring));
        }
        buff.append(wildcard);
        // last wildcard in the string but not trailing
        if (i == wildcardIndexes.size() - 1 && wildcardIndex < searchString.length() - 1) {
          String substring = searchString.substring(wildcardIndex + 1, searchString.length());
          buff.append(LdapEncoder.filterEncode(substring));
        }
        previousWildcardIndex = wildcardIndex;
      }
    }

    return buff.toString();
  }

  @Override
  public List<LdapUser> findUsers() {

    CollectingNameClassPairCallbackHandler<LdapUser> handler = getUserSearchCallbackHandler(getUserAttributesMapper());
    return search(getUserFilter(), handler);
  }

  abstract List<LdapUser> search(String filter, CollectingNameClassPairCallbackHandler<LdapUser> handler);

  private String getUserFilter () {

    if (StringUtils.isNotBlank(getSearchUserFilter())) {
        return getSearchUserFilter();
    }
    AndFilter filter = new AndFilter();
    filter.and(getCriteria(OBJECTCLASS_ATTR, getUserClass()));
    return filter.encode();
  }

  private AttributesMapper<LdapUser> getUserAttributesMapper() {

    return attributes -> {
      LdapUser user = getAttributesMapper(LdapUser::new).mapFromAttributes(attributes);
      user.setLogin(valueAsString(attributes.get(getLoginAttributeName())));
      List<LdapGroup> groups = getLdapGroups(attributes);
      user.setGroups(groups);
      return user;
    };
  }

  private <T extends LdapObject> AttributesMapper<T> getAttributesMapper(Supplier<T> supplier) {
    return attributes -> {
      String name = valueAsString(attributes.get(getDisplayNameAttributeName()));
      String dn = valueAsString(attributes.get(getDistinguishedAttributeName()));
      T object = supplier.get();
      object.setDisplayName(name);
      object.setDistinguishedName(dn);
      return object;
    };
  }

}
