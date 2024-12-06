package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.Filter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.ohdsi.webapi.OidcConfCreator;
import org.ohdsi.webapi.cohortcharacterization.CcImportEvent;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.filters.CorsFilter;
import org.ohdsi.webapi.shiro.filters.ForceSessionCreationFilter;
import org.ohdsi.webapi.shiro.filters.ResponseNoCacheFilter;
import org.ohdsi.webapi.shiro.filters.UrlBasedAuthorizingFilter;
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
  public static final String AUTH_CLIENT_ATTRIBUTE = "AUTH_CLIENT";
  public static final String AUTH_FILTER_ATTRIBUTE = "AuthenticatingFilter";
  public static final String PERMISSIONS_ATTRIBUTE = "PERMISSIONS";

  public static final String AUTH_CLIENT_SAML = "AUTH_CLIENT_SAML";
  public static final String AUTH_CLIENT_ALL = "ALL";

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected PermissionManager authorizer;

  @Autowired
  protected SourceRepository sourceRepository;

  @Autowired
  protected OidcConfCreator oidcConfCreator;

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
            .addRestPath("/ddl/achilles")
            .addRestPath("/ddl/extra")

            .addRestPath("/saml/saml-metadata")
            .addRestPath("/saml/slo")

            //executionservice callbacks
            .addRestPath("/executionservice/callbacks/**")

            .addRestPath("/permission/access/**/*", JWT_AUTHC) // Authorization check is done inside controller

            //i18n
            .addRestPath("/i18n")
            .addRestPath("/i18n/**")

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
  public ModularRealmAuthenticator getAuthenticator() {
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new NegotiateAuthenticationStrategy());

    return authenticator;
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
    try {
      if (SecurityUtils.getSubject().isAuthenticated()) {
        return authorizer.getSubjectName();
      }
    } catch (UnavailableSecurityManagerException e) {
      log.warn("No security manager is available, authenticated as anonymous");
    }
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
