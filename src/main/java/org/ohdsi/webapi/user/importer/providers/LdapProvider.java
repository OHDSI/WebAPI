package org.ohdsi.webapi.user.importer.providers;

import org.ohdsi.webapi.user.importer.model.LdapGroup;
import org.ohdsi.webapi.user.importer.model.LdapUser;
import org.springframework.ldap.core.*;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.List;
import java.util.Set;

public interface LdapProvider {

  LdapTemplate getLdapTemplate();

  List<LdapGroup> getLdapGroups(Attributes attributes) throws NamingException;

  SearchControls getUserSearchControls();

  Set<String> getGroupClasses();

  Set<String> getUserClass();

  String getSearchUserFilter();

  List<LdapUser> findUsers();

  List<LdapGroup> findGroups(String searchStr);

  String getLoginAttributeName();

  String getDistinguishedAttributeName();

  String getDisplayNameAttributeName();

  CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(AttributesMapper<LdapUser> attributesMapper);

  String getPrincipal();

  String getPassword();
}
