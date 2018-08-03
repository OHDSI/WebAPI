package org.ohdsi.webapi.user.importer.model;

public class AuthenticationProviders {

  private String adUrl;
  private String ldapUrl;

  public String getAdUrl() {
    return adUrl;
  }

  public void setAdUrl(String adUrl) {
    this.adUrl = adUrl;
  }

  public String getLdapUrl() {
    return ldapUrl;
  }

  public void setLdapUrl(String ldapUrl) {
    this.ldapUrl = ldapUrl;
  }
}
