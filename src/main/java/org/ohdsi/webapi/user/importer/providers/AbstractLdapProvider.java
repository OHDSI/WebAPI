package org.ohdsi.webapi.user.importer.providers;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapObject;
import org.ohdsi.webapi.user.importer.model.LdapUser;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.CollectingNameClassPairCallbackHandler;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.WhitespaceWildcardsFilter;
import org.springframework.ldap.support.LdapUtils;

import java.util.List;
import java.util.function.Supplier;

import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.getCriteria;
import static org.ohdsi.webapi.user.importer.providers.OhdsiLdapUtils.valueAsString;

public abstract class AbstractLdapProvider implements LdapProvider {

  public static final String OBJECTCLASS_ATTR = "objectclass";
  public static final String CN_ATTR = "cn";

  @Override
  public List<LdapGroup> findGroups(String searchStr) {

    LdapTemplate ldapTemplate = getLdapTemplate();
    AndFilter filter = new AndFilter();
//    filter.and(getCriteria(OBJECTCLASS_ATTR, getGroupClasses()));
    filter.and(new WhitespaceWildcardsFilter(CN_ATTR, searchStr));
    return ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), getAttributesMapper(LdapGroup::new));
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
