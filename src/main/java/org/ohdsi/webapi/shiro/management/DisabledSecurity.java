package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.shiro.CorsFilter;
import org.ohdsi.webapi.shiro.HideResourceFilter;
import org.springframework.stereotype.Component;

/**
 *
 * @author gennadiy.anisimov
 */
public class DisabledSecurity extends Security {

  @Override
  public Map<String, String> getFilterChain() {
    Map<String, String> filterChain = new HashMap<>();
    filterChain.put("/user/**", "hideResource");
    filterChain.put("/role/**", "hideResource");
    filterChain.put("/permission/**", "hideResource");
    filterChain.put("/**", "cors");
    return filterChain;
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, javax.servlet.Filter> filters = new HashMap<>();
    filters.put("hideResource", new HideResourceFilter());
    filters.put("cors", new CorsFilter());
    return filters;
  }

  @Override
  public Set<Realm> getRealms() {
    return new HashSet<>();
  }

  @Override
  public Authenticator getAuthenticator() {
    return new ModularRealmAuthenticator();
  }

  @Override
  public String getSubject() {
    return "anonymous";
  }
}
