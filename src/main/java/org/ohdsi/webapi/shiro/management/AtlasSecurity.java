package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.Filter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.ohdsi.webapi.OidcConfCreator;
import org.ohdsi.webapi.cohortcharacterization.CcImportEvent;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.filters.CorsFilter;
import org.ohdsi.webapi.shiro.filters.ForceSessionCreationFilter;
import org.ohdsi.webapi.shiro.filters.ResponseNoCacheFilter;
import org.ohdsi.webapi.shiro.filters.UrlBasedAuthorizingFilter;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.AUTHZ;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CORS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.FORCE_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.JWT_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_CACHE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SSL;

/**
 *
 * @author gennadiy.anisimov
 */
public abstract class AtlasSecurity extends Security {
  public static final String TOKEN_ATTRIBUTE = "TOKEN";
  public static final String AUTH_FILTER_ATTRIBUTE = "AuthenticatingFilter";
  public static final String PERMISSIONS_ATTRIBUTE = "PERMISSIONS";
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected PermissionManager authorizer;

  @Autowired
  SourceRepository sourceRepository;

  @Autowired
  OidcConfCreator oidcConfCreator;

  @Value("${server.port}")
  private int sslPort;

  @Value("${security.ssl.enabled}")
  private boolean sslEnabled;

  private final EntityPermissionSchemaResolver permissionSchemaResolver;

  protected final Set<String> defaultRoles = new LinkedHashSet<>();

  private final Map<String, String> featureAnalysisPermissionTemplates;
  private final Map<FilterTemplates, Filter> filters = new HashMap<>();

  public AtlasSecurity(EntityPermissionSchemaResolver permissionSchemaResolver) {
    this.defaultRoles.add("public");
    this.permissionSchemaResolver = permissionSchemaResolver;
    featureAnalysisPermissionTemplates = permissionSchemaResolver.getForType(EntityType.FE_ANALYSIS).getAllPermissions();
  }

  @PostConstruct
  private void init() {
    fillFilters();
    initRolesForSources();
  }

  private void initRolesForSources() {
    try {
      for (Source source : sourceRepository.findAll()) {
        this.addSourceRole(source.getSourceKey());
      }
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, String> getFilterChain() {

      return getFilterChainBuilder().build();
  }

  protected abstract FilterChainBuilder getFilterChainBuilder();

  protected FilterChainBuilder setupProtectedPaths(FilterChainBuilder filterChainBuilder) {

    return filterChainBuilder
            // version info
            .addRestPath("/info")
            // DDL service
            .addRestPath("/ddl/results")
            .addRestPath("/ddl/cemresults")

            //executionservice callbacks
            .addRestPath("/executionservice/callbacks/**")

            .addRestPath("/permission/access/**/*", JWT_AUTHC) // Authorization check is done inside controller

            .addProtectedRestPath("/**/*");
  }

  @Override
  public Map<FilterTemplates, Filter> getFilters(){
    return new HashMap<>(filters);
  }

  private void fillFilters() {

    filters.put(NO_SESSION_CREATION, new NoSessionCreationFilter());
    filters.put(FORCE_SESSION_CREATION, new ForceSessionCreationFilter());
    filters.put(AUTHZ, new UrlBasedAuthorizingFilter());
    filters.put(CORS, new CorsFilter());
    filters.put(SSL, this.getSslFilter());
    filters.put(NO_CACHE, this.getNoCacheFilter());
  }

  @Override
  public Set<Realm> getRealms() {

    return new LinkedHashSet<>();
  }

  @Override
  public Authenticator getAuthenticator() {
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new NegotiateAuthenticationStrategy());

    return authenticator;
  }

  private String getSourceRoleName(String sourceKey) {
    return String.format("Source user (%s)", sourceKey);
  }

  @Override
  public void addSourceRole(String sourceKey) throws Exception {
    final String roleName = getSourceRoleName(sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      return;
    }

    RoleEntity role = this.authorizer.addRole(roleName, true);
    Map<String, String> sourceWritePermissionTemplates = permissionSchemaResolver.getForType(EntityType.SOURCE).getAllPermissions();
    this.authorizer.addPermissionsFromTemplate(role, sourceWritePermissionTemplates, sourceKey);
  }

  @Override
  public void removeSourceRole(String sourceKey) throws Exception {
    final String roleName = getSourceRoleName(sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      RoleEntity role = this.authorizer.getRoleByName(roleName);
      Map<String, String> sourcePermissionTemplates = permissionSchemaResolver.getForType(EntityType.SOURCE).getAllPermissions();
      this.authorizer.removePermissionsFromTemplate(sourcePermissionTemplates, sourceKey);
      this.authorizer.removeRole(role.getId());
    }
  }

  private Filter getSslFilter() {
    SslFilter sslFilter = new SslFilter();
    sslFilter.setPort(sslPort);
    sslFilter.setEnabled(sslEnabled);
    return sslFilter;
  }

  private Filter getNoCacheFilter() {
    return new ResponseNoCacheFilter();
  }

  @Override
  public String getSubject() {
    if (SecurityUtils.getSubject().isAuthenticated())
      return authorizer.getSubjectName();
    else
      return "anonymous";
  }

  // Since we need to create permissions only for certain analyses, we cannot go with `addProcessEntityFilter`
  @EventListener
  public void onCcImport(CcImportEvent event) throws Exception {
      for (Integer id : event.getSavedAnalysesIds()) {
          authorizer.addPermissionsFromTemplate(featureAnalysisPermissionTemplates, id.toString());
      }
  }
}
