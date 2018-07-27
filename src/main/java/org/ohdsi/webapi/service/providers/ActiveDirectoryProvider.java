package org.ohdsi.webapi.service.providers;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("security.ad.url")
public class ActiveDirectoryProvider implements LdapProvider {

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

  @Value("${security.ad.ignore.partial.result.exception}")
  private Boolean adIgnorePartialResultException;

  @Override
  public LdapTemplate getLdapTemplate() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(adUrl);
    contextSource.setBase(adSearchBase);
    contextSource.setUserDn(adSystemUsername);
    contextSource.setPassword(adSystemPassword);
    contextSource.setCacheEnvironmentProperties(false);
    contextSource.setAuthenticationStrategy(new SimpleDirContextAuthenticationStrategy());
    contextSource.setAuthenticationSource(new AuthenticationSource() {
      @Override
      public String getPrincipal() {
        return StringUtils.isNotBlank(adPrincipalSuffix) ? adSystemUsername + adPrincipalSuffix : adSystemUsername;
      }

      @Override
      public String getCredentials() {
        return adSystemPassword;
      }
    });
    LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
    ldapTemplate.setIgnorePartialResultException(adIgnorePartialResultException);
    return ldapTemplate;
  }
}
