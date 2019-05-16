package org.ohdsi.webapi.shiro.management;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.filters.CorsFilter;
import org.ohdsi.webapi.shiro.filters.HideResourceFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.CORS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.HIDE_RESOURCE;
/**
 *
 * @author gennadiy.anisimov
 */
@Component
@Primary
@ConditionalOnProperty(name = "security.provider", havingValue = Constants.SecurityProviders.DISABLED)
@DependsOn("flyway")
public class DisabledSecurity extends Security {

  @Override
  public Map<String, String> getFilterChain() {
    Map<String, String> filterChain = new HashMap<>();
    filterChain.put("/user/**", HIDE_RESOURCE.getTemplateName());
    filterChain.put("/role/**", HIDE_RESOURCE.getTemplateName());
    filterChain.put("/permission/**", HIDE_RESOURCE.getTemplateName());
    filterChain.put("/**", CORS.getTemplateName());
    return filterChain;
  }

  @Override
  public Map<FilterTemplates, Filter> getFilters() {
    Map<FilterTemplates, Filter> filters = new HashMap<>();
    filters.put(HIDE_RESOURCE, new HideResourceFilter());
    filters.put(CORS, new CorsFilter());
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
  public void addSourceRole(String sourceKey) throws Exception {
    //Do nothing
  }

  @Override
  public void removeSourceRole(String sourceKey) throws Exception {
    //Do nothing
  }

  @Override
  public String getSubject() {
    return "anonymous";
  }

}
