package org.ohdsi.webapi.service.providers;

import org.springframework.ldap.core.LdapTemplate;

public interface LdapProvider {

  LdapTemplate getLdapTemplate();
}
