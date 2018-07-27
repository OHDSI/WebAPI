package org.ohdsi.webapi.service.userimport;

import org.ohdsi.webapi.model.users.LdapGroup;
import org.ohdsi.webapi.model.users.LdapProviderType;
import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

public interface UserImportService {

  LdapTemplate getLdapTemplate(LdapProviderType providerType);

  List<LdapGroup> findGroups(LdapProviderType providerType, String searchStr);
}
