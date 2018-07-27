package org.ohdsi.webapi.service.providers;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("security.ldap.url")
public class DefaultLdapProvider implements LdapProvider {

  @Override
  public LdapTemplate getLdapTemplate() {
    return null;
  }
}
