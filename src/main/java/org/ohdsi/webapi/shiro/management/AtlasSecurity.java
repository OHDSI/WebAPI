package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.InvalidateAccessTokenFilter;
import org.ohdsi.webapi.shiro.JwtAuthRealm;
import org.ohdsi.webapi.shiro.JwtAuthenticatingFilter;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.ProcessResponseContentFilter;
import org.ohdsi.webapi.shiro.UpdateAccessTokenFilter;
import org.ohdsi.webapi.shiro.UrlBasedAuthorizingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public class AtlasSecurity extends Security {

  @Autowired
  private PermissionManager authorizer;

  @Value("${security.token.expiration}")
  private int tokenExpirationIntervalInSeconds;

  private final Set<String> defaultRoles = new HashSet<>();

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates = new LinkedHashMap<>();

  public AtlasSecurity() {
    this.defaultRoles.add("public");
    this.defaultRoles.add("concept set reader");

    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");
  }

  @Override
  public Map<String, String> getFilterChain() {
    Map<String, String> filterChain = new LinkedHashMap<>();
    
    filterChain.put("/user/login", "noSessionCreation, negotiateAuthcFilter, updateAccessTokenFilter");
    filterChain.put("/user/refresh", "noSessionCreation, jwtAuthcFilter, updateAccessTokenFilter");
    filterChain.put("/user/logout", "noSessionCreation, invalidateAccessTokenFilter");

    filterChain.put("/cohortdefinition", "noSessionCreation, jwtAuthcFilter, authzFilter, createPermissionsOnCreateCohortDefinitionFilter");
    filterChain.put("/cohortdefinition/*", "noSessionCreation, jwtAuthcFilter, authzFilter, deletePermissionsOnDeleteCohortDefinitionFilter");

    filterChain.put("/**", "noSessionCreation, jwtAuthcFilter, authzFilter");

    return filterChain;
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, javax.servlet.Filter> filters = new HashMap<>();
    
    filters.put("jwtAuthcFilter", new JwtAuthenticatingFilter());
    filters.put("negotiateAuthcFilter", new NegotiateAuthenticationFilter());
    filters.put("updateAccessTokenFilter", new UpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds));
    filters.put("invalidateAccessTokenFilter", new InvalidateAccessTokenFilter());
    filters.put("authzFilter", new UrlBasedAuthorizingFilter(this.authorizer));
    filters.put("createPermissionsOnCreateCohortDefinitionFilter", new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        return  HttpMethod.PUT.equalsIgnoreCase(this.getHttpMethod(request));
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();

        for (Map.Entry<String, String> entry : cohortdefinitionCreatorPermissionTemplates.entrySet()) {
          String value = String.format(entry.getKey(), id);
          String description = String.format(entry.getValue(), id);
          PermissionEntity permission = authorizer.addPermission(value, description);
          authorizer.addPermission(currentUserPersonalRole, permission);
        }
      }
    });
    filters.put("deletePermissionsOnDeleteCohortDefinitionFilter", new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())) {
          return;
        }

        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];

        for (Map.Entry<String, String> entry : cohortdefinitionCreatorPermissionTemplates.entrySet()) {
          String value = String.format(entry.getKey(), id);
          authorizer.removePermission(value);
        }
      }
    });
    
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
}
