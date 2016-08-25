package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.InvalidateAccessTokenFilter;
import org.ohdsi.webapi.shiro.JwtAuthRealm;
import org.ohdsi.webapi.shiro.JwtAuthenticatingFilter;
import org.ohdsi.webapi.shiro.SimpleAuthorizer;
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
  private SimpleAuthorizer authorizer;
  
  @Value("${security.token.expiration}")
  private int tokenExpirationIntervalInSeconds;

  private final Set<String> defaultRoles = new HashSet<>();

  @Override
  public void init() {
    this.defaultRoles.add("public");
    this.defaultRoles.add("concept set reader");

    try {
      RoleEntity publicRole = this.authorizer.addRole("public");
      this.authorizer.addPermission(publicRole, "user:roles:request:*:put", "Request role");
      this.authorizer.addPermission(publicRole, "user:permissions:request:*:*:put", "Request permission");
      this.authorizer.addPermission(publicRole, "user:permissions:request:*:*:*:put", "Request permission");
      this.authorizer.addPermission(publicRole, "user:permitted:post", "Check if user has permission");


      // for test purposes only
      //
      this.authorizer.addPermission(publicRole, "user:permissions:requested:get", "Get list of requested permissions");
      this.authorizer.addPermission(publicRole, "user:permissions:approve:*:post", "Approve request for permission");
      this.authorizer.addPermission(publicRole, "user:permissions:refuse:*:post", "Refuse request for permission");
      this.authorizer.addPermission(publicRole, "user:roles:get", "Get list of roles");
      this.authorizer.addPermission(publicRole, "user:roles:requested:get", "Get list of requested roles");
      this.authorizer.addPermission(publicRole, "user:roles:approve:*:post", "Approve request for role");
      this.authorizer.addPermission(publicRole, "user:roles:refuse:*:post", "Refuse request for role");
      this.authorizer.addPermission(publicRole, "user:roles:add:put", "Add user to role");
      this.authorizer.addPermission(publicRole, "user:roles:remove:delete", "Delete user from role");
      // for test purposes only - end



      RoleEntity adminRole = this.authorizer.addRole("admin");
      this.authorizer.addPermission(adminRole, "user:roles:get", "Get list of roles");
      this.authorizer.addPermission(adminRole, "user:roles:requested:get", "Get list of requested roles");
      this.authorizer.addPermission(adminRole, "user:roles:approve:*:post", "Approve request for role");
      this.authorizer.addPermission(adminRole, "user:roles:refuse:*:post", "Refuse request for role");
      this.authorizer.addPermission(adminRole, "user:roles:add:put", "Add user to role");
      this.authorizer.addPermission(adminRole, "user:roles:remove:delete", "Delete user from role");
      this.authorizer.addPermission(adminRole, "user:permissions:requested:get", "Get list of requested permissions");
      this.authorizer.addPermission(adminRole, "user:permissions:approve:*:post", "Approve request for permission");
      this.authorizer.addPermission(adminRole, "user:permissions:refuse:*:post", "Refuse request for permission");
      this.authorizer.addPermission(adminRole, "configuration:read,edit:ui", null);

      RoleEntity conceptSetEditorRole = this.authorizer.addRole("concept set editor");
      this.authorizer.addPermission(conceptSetEditorRole, "conceptset:*:*:exists:get", "Check if Concept Set exitsts");
      this.authorizer.addPermission(conceptSetEditorRole, "conceptset:post", "Create Concept Set");
      this.authorizer.addPermission(conceptSetEditorRole, "conceptset:*:items:post", "Save Concept Set items");
      this.authorizer.addPermission(conceptSetEditorRole, "conceptset:edit:ui", null);

      RoleEntity conceptSetReaderRole = this.authorizer.addRole("concept set reader");
      this.authorizer.addPermission(conceptSetReaderRole, "conceptset:get", "Get list of Concept Sets");
      this.authorizer.addPermission(conceptSetReaderRole, "conceptset:read:ui", null);

      RoleEntity cohortEditorRole = this.authorizer.addRole("cohort editor");
      this.authorizer.addPermission(cohortEditorRole, "cohortdefinition:put", "Save new Cohort");
      this.authorizer.addPermission(cohortEditorRole, "cohortdefinition:*:put", "Save changes in Cohort");
      this.authorizer.addPermission(cohortEditorRole, "job:execution:get", "Get list of jobs");
      this.authorizer.addPermission(cohortEditorRole, "cohort:edit:ui", null);

      RoleEntity cohortReaderRole = this.authorizer.addRole("cohort reader");
      this.authorizer.addPermission(cohortReaderRole, "cohortdefinition:get", "Get list of Cohorts");
      this.authorizer.addPermission(cohortReaderRole, "cohort:read:ui", null);
    }
    catch (Exception e) {
    }
  }

  @Override
  public Map<String, String> getFilterChain() {
    Map<String, String> filterChain = new HashMap<>();    
    
    filterChain.put("/user/login", "noSessionCreation, negotiateAuthcFilter, updateAccessTokenFilter");
    filterChain.put("/user/refresh", "noSessionCreation, jwtAuthcFilter, updateAccessTokenFilter");
    filterChain.put("/user/logout", "noSessionCreation, invalidateAccessTokenFilter");

    filterChain.put("/user/permissions/**", "noSessionCreation, jwtAuthcFilter, authzFilter");
    filterChain.put("/user/roles/**", "noSessionCreation, jwtAuthcFilter, authzFilter");
    filterChain.put("/user/permitted", "noSessionCreation, jwtAuthcFilter, authzFilter");

    filterChain.put("/conceptset", "noSessionCreation, jwtAuthcFilter, authzFilter");
    filterChain.put("/conceptset/**", "noSessionCreation, jwtAuthcFilter, authzFilter");
    filterChain.put("/cohortdefinition", "noSessionCreation, jwtAuthcFilter, authzFilter");
    filterChain.put("/cohortdefinition/**", "noSessionCreation, jwtAuthcFilter, authzFilter");
    filterChain.put("/job/execution", "noSessionCreation, jwtAuthcFilter, authzFilter");

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
