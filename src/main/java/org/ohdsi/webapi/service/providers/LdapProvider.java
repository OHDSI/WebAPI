package org.ohdsi.webapi.service.providers;

import org.ohdsi.webapi.model.users.LdapGroup;
import org.ohdsi.webapi.model.users.LdapUser;
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

  String getLoginAttributeName();

  String getDistinguishedAttributeName();

  String getDisplayNameAttributeName();

  CollectingNameClassPairCallbackHandler<LdapUser> getUserSearchCallbackHandler(AttributesMapper<LdapUser> attributesMapper);
}
