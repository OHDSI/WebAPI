package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.shiro.InvalidateAccessTokenFilter;
import org.ohdsi.webapi.shiro.JwtAuthFilter;
import org.ohdsi.webapi.shiro.JwtAuthRealm;
import org.ohdsi.webapi.shiro.SimpleAuthorizer;
import org.ohdsi.webapi.shiro.UpdateAccessTokenFilter;
import org.ohdsi.webapi.shiro.UrlBasedAuthorizingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public class DefaultSecurity extends Security {

  @Autowired
  private SimpleAuthorizer authorizer;
  
  @Override
  public Map<String, String> getFilterChain() {
    Map<String, String> filterChain = new HashMap<>();    
    
    filterChain.put("/user/test/*", "noSessionCreation, bearerTokenAuthFilter, authorizingFilter");
    filterChain.put("/user/login", "noSessionCreation, negotiateAuthFilter, updateAccessTokenFilter");
    filterChain.put("/user/logout", "noSessionCreation, invalidateAccessTokenFilter");
    
    return filterChain;
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, javax.servlet.Filter> filters = new HashMap<>();
    
    filters.put("bearerTokenAuthFilter", new JwtAuthFilter());
    filters.put("negotiateAuthFilter", new NegotiateAuthenticationFilter());
    filters.put("updateAccessTokenFilter", new UpdateAccessTokenFilter(this.authorizer));
    filters.put("invalidateAccessTokenFilter", new InvalidateAccessTokenFilter());
    filters.put("authorizingFilter", new UrlBasedAuthorizingFilter(this));
    
    return filters;
  }

  @Override
  public Set<Realm> getRealms() {
    Set<Realm> realms = new HashSet<>();

    realms.add(new JwtAuthRealm(this.authorizer));
    realms.add(new NegotiateAuthenticationRealm());
    
    return realms;
  }

  @Override
  public Authenticator getAuthenticator() {
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new NegotiateAuthenticationStrategy());
    
    return authenticator;
  }

  @Override
  public boolean isPermitted(String permission) {
    return SecurityUtils.getSubject().isPermitted(permission);
  }
}
