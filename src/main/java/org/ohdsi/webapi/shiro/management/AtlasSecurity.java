package org.ohdsi.webapi.shiro.management;

import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.OidcConfCreator;
import org.ohdsi.webapi.shiro.*;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.filters.KerberosAuthFilter;
import org.ohdsi.webapi.shiro.realms.KerberosAuthRealm;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

/**
 *
 * @author gennadiy.anisimov
 */
public class AtlasSecurity extends Security {
  public static final String TOKEN_ATTRIBUTE = "TOKEN";
  public static final String PERMISSIONS_ATTRIBUTE = "PERMISSIONS";
  private final Log log = LogFactory.getLog(getClass());

  @Autowired
  private PermissionManager authorizer;

  @Autowired
  SourceRepository sourceRepository;

  @Autowired
  OidcConfCreator oidcConfCreator;

  @Value("${security.token.expiration}")
  private int tokenExpirationIntervalInSeconds;

  @Value("${server.port}")
  private int sslPort;

  @Value("${security.ssl.enabled}")
  private boolean sslEnabled;

  @Value("${security.oauth.callback.ui}")
  private String oauthUiCallback;

  @Value("${security.oauth.callback.api}")
  private String oauthApiCallback;

  @Value("${security.oauth.google.apiKey}")
  private String googleApiKey;

  @Value("${security.oauth.google.apiSecret}")
  private String googleApiSecret;

  @Value("${security.oauth.facebook.apiKey}")
  private String facebookApiKey;

  @Value("${security.oauth.facebook.apiSecret}")
  private String facebookApiSecret;

  @Value("${security.oid.redirectUrl}")
  private String redirectUrl;

  @Value("${security.kerberos.spn}")
  private String kerberosSpn;

  @Value("${security.kerberos.keytabPath}")
  private String kerberosKeytabPath;

  @Value("${security.ldap.dn}")
  private String userDnTemplate;

  @Value("${security.ldap.url}")
  private String ldapUrl;

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


  @Autowired
  @Qualifier("authDataSource")
  private DataSource jdbcDataSource;

  @Value("${security.db.datasource.authenticationQuery}")
  private String jdbcAuthenticationQuery;

  private final Set<String> defaultRoles = new LinkedHashSet<>();

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> conceptsetCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> sourcePermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> incidenceRatePermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> estimationPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> plpPermissionTemplate = new LinkedHashMap<>();

  public AtlasSecurity() {
    this.defaultRoles.add("public");

    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");

    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:put", "Update Concept Set with ID = %s");
    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:items:put", "Update Items of Concept Set with ID = %s");
    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:delete", "Delete Concept Set with ID = %s");

    this.sourcePermissionTemplates.put("cohortdefinition:*:report:%s:get", "Get Inclusion Rule Report for Source with SourceKey = %s");
    this.sourcePermissionTemplates.put("cohortdefinition:*:generate:%s:get", "Generate Cohort on Source with SourceKey = %s");

    this.incidenceRatePermissionTemplates.put("ir:%s:get", "Read Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:info:get", "Read Incidence Rate info with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:report:*:get", "Report Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:copy:get", "Copy Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:export:get", "Export Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:put", "Edit Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:delete", "Delete Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:info:*:delete", "Delete Incidence Rate info with ID=%s");

    this.estimationPermissionTemplates.put("comparativecohortanalysis:%s:put", "Edit Estimation with ID=%s");
    this.estimationPermissionTemplates.put("comparativecohortanalysis:%s:delete", "Delete Estimation with ID=%s");

    this.plpPermissionTemplate.put("plp:%s:put", "Edit Population Level Prediction with ID=%s");
    this.plpPermissionTemplate.put("plp:%s:delete", "Delete Population Level Prediction with ID=%s");
    this.plpPermissionTemplate.put("plp:%s:get", "Read Population Level Prediction with ID=%s");
    this.plpPermissionTemplate.put("plp:%s:copy:get", "Copy Population Level Prediction with ID=%s");
  }

  @Override
  public Map<String, String> getFilterChain() {

    return new FilterChainBuilder()
      .setOAuthFilters("ssl, cors, forceSessionCreation", "updateToken, sendTokenInUrl")
      .setRestFilters("ssl, noSessionCreation, cors")
      .setAuthcFilter("jwtAuthc")
      .setAuthzFilter("authz")

      // the order does matter - first match wins

      // protected resources
      //
      // login/logout
      .addRestPath("/user/login/openid", "forceSessionCreation, oidcAuth, updateToken, sendTokenInRedirect")
      .addRestPath("/user/login/windows","negotiateAuthc, updateToken, sendTokenInHeader")
      .addRestPath("/user/login/kerberos","kerberosFilter, updateToken, sendTokenInHeader")
      .addRestPath("/user/login/db", "jdbcFilter, updateToken, sendTokenInHeader")
      .addRestPath("/user/login/ldap", "ldapFilter, updateToken, sendTokenInHeader")
      .addRestPath("/user/login/ad", "adFilter, updateToken, sendTokenInHeader")
      .addRestPath("/user/refresh", "jwtAuthc, updateToken, sendTokenInHeader")
      .addRestPath("/user/logout", "invalidateToken, logout")
      .addOAuthPath("/user/oauth/google", "googleAuthc")
      .addOAuthPath("/user/oauth/facebook", "facebookAuthc")
      .addPath("/user/oauth/callback", "ssl, handleUnsuccessfullOAuth, oauthCallback")

      // permissions
      .addProtectedRestPath("/user/**")
      .addProtectedRestPath("/role/**")
      .addProtectedRestPath("/permission/**")

      // concept set
      .addProtectedRestPath("/conceptset", "jwtAuthc, authz, createPermissionsOnCreateConceptSet")
      .addProtectedRestPath("/conceptset/*/items", "jwtAuthc, authz")
      .addProtectedRestPath("/conceptset/*", "jwtAuthc, authz, deletePermissionsOnDeleteConceptSet")

      // incidence rates
      .addProtectedRestPath("/ir", "jwtAuthc, authz, createPermissionsOnCreateIR")
      .addProtectedRestPath("/ir/*/copy", "createPermissionsOnCopyIR")
      .addProtectedRestPath("/ir/*", "jwtAuthc, authz")

      // comparative cohort analysis (estimation)
      .addProtectedRestPath("/comparativecohortanalysis", "createPermissionsOnCreateEstimation")
      .addProtectedRestPath("/comparativecohortanalysis/*", "deletePermissionsOnDeleteEstimation")

      // population level prediction
      .addProtectedRestPath("/plp", "createPermissionsOnCreatePlp")
      .addProtectedRestPath("/plp/*/copy", "createPermissionsOnCopyPlp")
      .addProtectedRestPath("/plp/*", "deletePermissionsOnDeletePlp")

      // cohort definition
      .addProtectedRestPath("/cohortdefinition", "createPermissionsOnCreateCohortDefinition")
      .addProtectedRestPath("/cohortdefinition/*/copy", "createPermissionsOnCreateCohortDefinition")
      .addProtectedRestPath("/cohortdefinition/*", "deletePermissionsOnDeleteCohortDefinition")
      .addProtectedRestPath("/cohortdefinition/*/info")
      .addProtectedRestPath("/cohortdefinition/sql")
      .addProtectedRestPath("/cohortdefinition/*/generate/*")
      .addProtectedRestPath("/cohortdefinition/*/report/*")
      .addProtectedRestPath("/*/cohortresults/*/breakdown")
      .addProtectedRestPath("/job/execution")
      .addProtectedRestPath("/job")

      // configuration
      .addProtectedRestPath("/source/refresh")
      .addProtectedRestPath("/source/priorityVocabulary")

      // cohort analysis
      .addProtectedRestPath("/cohortanalysis")
      .addProtectedRestPath("/cohortanalysis/*")

      // cohort results
      .addProtectedRestPath("/cohortresults/*")

      // evidence
      .addProtectedRestPath("/evidence/*")

      // execution service
      .addProtectedRestPath("/execution_service/*")

      // feasibility
      .addProtectedRestPath("/feasibility")
      .addProtectedRestPath("/feasibility/*")

      // featureextraction
      .addProtectedRestPath("/featureextraction/*")

      // vocabulary services
      .addProtectedRestPath("/vocabulary/*")

      // data sources
      .addProtectedRestPath("/cdmresults/*")

      // not protected resources - all the rest
      .addRestPath("/**")

      .build();
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, javax.servlet.Filter> filters = new HashMap<>();

    filters.put("logout", new LogoutFilter());
    filters.put("noSessionCreation", new NoSessionCreationFilter());
    filters.put("forceSessionCreation", new ForceSessionCreationFilter());
    filters.put("jwtAuthc", new JwtAuthFilter());
    filters.put("negotiateAuthc", new NegotiateAuthenticationFilter());
    filters.put("updateToken", new UpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds));
    filters.put("invalidateToken", new InvalidateAccessTokenFilter());
    filters.put("authz", new UrlBasedAuthorizingFilter());
    filters.put("createPermissionsOnCreateCohortDefinition", this.getCreatePermissionsOnCreateCohortDefinitionFilter());
    filters.put("createPermissionsOnCreateConceptSet", this.getCreatePermissionsOnCreateConceptSetFilter());
    filters.put("deletePermissionsOnDeleteCohortDefinition", this.getDeletePermissionsOnDeleteCohortDefinitionFilter());
    filters.put("deletePermissionsOnDeleteConceptSet", this.getDeletePermissionsOnDeleteConceptSetFilter());
    filters.put("deletePermissionsOnDeleteEstimation", this.getDeletePermissionsOnDeleteFilter(estimationPermissionTemplates));
    filters.put("deletePermissionsOnDeletePlp", this.getDeletePermissionsOnDeleteFilter(plpPermissionTemplate));
    filters.put("createPermissionsOnCreateIR", this.getCreatePermissionsOnCreateIncidenceRateFilter());
    filters.put("createPermissionsOnCopyIR", this.getCreatePermissionsOnCopyIncidenceRateFilter());
    filters.put("createPermissionsOnCreateEstimation", this.getCreatePermissionsOnCreateFilter(estimationPermissionTemplates, "analysisId"));
    filters.put("createPermissionsOnCreatePlp", this.getCreatePermissionsOnCreateFilter(plpPermissionTemplate, "analysisId"));
    filters.put("createPermissionsOnCopyPlp", this.getCreatePermissionsOnCopyFilter(plpPermissionTemplate, ".*plp/.*/copy", "analysisId"));
    filters.put("cors", new CorsFilter());
    filters.put("skipFurtherFiltersIfNotPost", this.getSkipFurtherFiltersIfNotPostFilter());
    filters.put("skipFurtherFiltersIfNotPut", this.getSkipFurtherFiltersIfNotPutFilter());
    filters.put("skipFurtherFiltersIfNotPutOrPost", this.getskipFurtherFiltersIfNotPutOrPostFilter());
    filters.put("skipFurtherFiltersIfNotPutOrDelete", this.getskipFurtherFiltersIfNotPutOrDeleteFilter());
    filters.put("sendTokenInUrl", new SendTokenInUrlFilter(this.oauthUiCallback));
    filters.put("sendTokenInHeader", new SendTokenInHeaderFilter());
    filters.put("sendTokenInRedirect", new SendTokenInRedirectFilter(redirectUrl));
    filters.put("ssl", this.getSslFilter());
    filters.put("jdbcFilter", new JdbcAuthFilter());
    filters.put("kerberosFilter", new KerberosAuthFilter());
    filters.put("ldapFilter", new LdapAuthFilter());
    filters.put("adFilter", new ActiveDirectoryAuthFilter());

    // OAuth
    //
    Google2Client googleClient = new Google2Client(this.googleApiKey, this.googleApiSecret);
    googleClient.setScope(Google2Client.Google2Scope.EMAIL);

    FacebookClient facebookClient = new FacebookClient(this.facebookApiKey, this.facebookApiSecret);
    facebookClient.setScope("email");
    facebookClient.setFields("email");

    OidcConfiguration configuration = oidcConfCreator.build();
    OidcClient oidcClient = new OidcClient(configuration);

    Config cfg =
            new Config(
                    new Clients(
                            this.oauthApiCallback
                            , googleClient
                            , facebookClient
                            , oidcClient
                            // ... put new clients here and then assign them to filters ...
                    )
            );

    // assign clients to filters
    SecurityFilter googleOauthFilter = new SecurityFilter();
    googleOauthFilter.setConfig(cfg);
    googleOauthFilter.setClients("Google2Client");
    filters.put("googleAuthc", googleOauthFilter);

    SecurityFilter facebookOauthFilter = new SecurityFilter();
    facebookOauthFilter.setConfig(cfg);
    facebookOauthFilter.setClients("FacebookClient");
    filters.put("facebookAuthc", facebookOauthFilter);

    SecurityFilter oidcFilter = new SecurityFilter();
    oidcFilter.setConfig(cfg);
    oidcFilter.setClients("OidcClient");
    filters.put("oidcAuth", oidcFilter);

    CallbackFilter callbackFilter = new CallbackFilter();
    callbackFilter.setConfig(cfg);
    filters.put("oauthCallback", callbackFilter);
    filters.put("handleUnsuccessfullOAuth", new RedirectOnFailedOAuthFilter(this.oauthUiCallback));

    return filters;
  }

  @Override
  public Set<Realm> getRealms() {
    Set<Realm> realms = new LinkedHashSet<>();

    realms.add(new JwtAuthRealm(this.authorizer));
    realms.add(new NegotiateAuthenticationRealm());
    realms.add(new Pac4jRealm());
    realms.add(new JdbcAuthRealm(jdbcDataSource, jdbcAuthenticationQuery));
    realms.add(new KerberosAuthRealm(kerberosSpn, kerberosKeytabPath));
    realms.add(ldapRealm());
    realms.add(activeDirectoryRealm());

    return realms;
  }

  private JndiLdapRealm ldapRealm() {
    JndiLdapRealm realm = new LdapRealm();
    realm.setUserDnTemplate(userDnTemplate);
    JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
    contextFactory.setUrl(ldapUrl);
    contextFactory.setPoolingEnabled(false);
    contextFactory.getEnvironment().put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    realm.setContextFactory(contextFactory);
    return realm;
  }

  private ActiveDirectoryRealm activeDirectoryRealm() {
    ActiveDirectoryRealm realm = new ADRealm();
    realm.setUrl(adUrl);
    realm.setSearchBase(adSearchBase);
    realm.setPrincipalSuffix(adPrincipalSuffix);
    realm.setSystemUsername(adSystemUsername);
    realm.setSystemPassword(adSystemPassword);
    return realm;
  }

  @Override
  public Authenticator getAuthenticator() {
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new NegotiateAuthenticationStrategy());

    return authenticator;
  }

  private void addSourceRole(String sourceKey) throws Exception {
    String roleName = String.format("Source user (%s)", sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      return;
    }

    RoleEntity role = this.authorizer.addRole(roleName);
    this.authorizer.addPermissionsFromTemplate(role, this.sourcePermissionTemplates, sourceKey);
  }

  @PostConstruct
  private void initRolesForSources() {
    try {
      for (Source source : sourceRepository.findAll()) {
          this.addSourceRole(source.getSourceKey());
      }
    }
    catch (Exception e) {
      log.error(e);
    }
  }

  private Filter getCreatePermissionsOnCreateCohortDefinitionFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "copy")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        }
        else {
          return  HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        }
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, cohortdefinitionCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateIncidenceRateFilter() {
    return  new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, incidenceRatePermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateFilter(Map<String, String> template, String idField) {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, idField);
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, template, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCopyIncidenceRateFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return WebUtils.toHttp(request).getRequestURI().matches(".*ir/.*/copy");
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, incidenceRatePermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCopyFilter(Map<String, String> template, String pathRegex, String idField) {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return WebUtils.toHttp(request).getRequestURI().matches(pathRegex);
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, idField);
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, template, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateConceptSetFilter() {
    return  new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        return  HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, conceptsetCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteFilter(Map<String, String> template) {
    return new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())) {
          return;
        }

        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(template, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteCohortDefinitionFilter() {
    return new AdviceFilter() {
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
        authorizer.removePermissionsFromTemplate(cohortdefinitionCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteIRFilter() {
    return new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())){
          return;
        }
        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(incidenceRatePermissionTemplates, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteConceptSetFilter() {
    return new AdviceFilter() {
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
        authorizer.removePermissionsFromTemplate(conceptsetCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getSkipFurtherFiltersIfNotPostFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        return !HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }
    };
  }

  private Filter getSkipFurtherFiltersIfNotPutFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        return !HttpMethod.PUT.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }
    };
  }

  private Filter getskipFurtherFiltersIfNotPutOrPostFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        String httpMethod = WebUtils.toHttp(request).getMethod();
        return !(HttpMethod.PUT.equalsIgnoreCase(httpMethod) || HttpMethod.POST.equalsIgnoreCase(httpMethod));
      }
    };
  }

  private Filter getskipFurtherFiltersIfNotPutOrDeleteFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        String httpMethod = WebUtils.toHttp(request).getMethod();
        return !(HttpMethod.PUT.equalsIgnoreCase(httpMethod) || HttpMethod.DELETE.equalsIgnoreCase(httpMethod));
      }
    };
  }

  private Filter getSslFilter() {
    SslFilter sslFilter = new SslFilter();
    sslFilter.setPort(sslPort);
    sslFilter.setEnabled(sslEnabled);
    return sslFilter;
  }

  @Override
  public String getSubject() {
    if (SecurityUtils.getSubject().isAuthenticated())
      return authorizer.getSubjectName();
    else
      return "anonymous";
  }

  private class FilterChainBuilder {

    private Map<String, String> filterChain = new LinkedHashMap<>();
    private String restFilters;
    private String authcFilter;
    private String authzFilter;
    private String filtersBeforeOAuth;
    private String filtersAfterOAuth;

    public FilterChainBuilder setRestFilters(String restFilters) {
      this.restFilters = restFilters;
      return this;
    }

    public FilterChainBuilder setOAuthFilters(String filtersBeforeOAuth, String filtersAfterOAuth) {
      this.filtersBeforeOAuth = filtersBeforeOAuth;
      this.filtersAfterOAuth = filtersAfterOAuth;
      return this;
    }

    public FilterChainBuilder setAuthcFilter(String authcFilter) {
      this.authcFilter = authcFilter;
      return this;
    }

    public FilterChainBuilder setAuthzFilter(String authzFilter) {
      this.authzFilter = authzFilter;
      return this;
    }

    public FilterChainBuilder addRestPath(String path, String filters) {
      return this.addPath(path, this.restFilters + ", " + filters);
    }

    public FilterChainBuilder addRestPath(String path) {
      return this.addPath(path, this.restFilters);
    }

    public FilterChainBuilder addOAuthPath(String path, String oauthFilter) {
      return this.addPath(path, filtersBeforeOAuth + ", " + oauthFilter + ", " + filtersAfterOAuth);
    }

    public FilterChainBuilder addProtectedRestPath(String path) {
      return this.addRestPath(path, this.authcFilter + ", " + this.authzFilter);
    }

    public FilterChainBuilder addProtectedRestPath(String path, String filters) {
      return this.addRestPath(path, authcFilter + ", " + authzFilter + ", " + filters);
    }

    public FilterChainBuilder addPath(String path, String filters) {
      path = path.replaceAll("/+$", "");
      this.filterChain.put(path, filters);

      // If path ends with non wildcard character, need to add two paths -
      // one without slash at the end and one with slash at the end, because
      // both URLs like www.domain.com/myapp/mypath and www.domain.com/myapp/mypath/
      // (note the slash at the end) are falling into the same method, but
      // for filter chain these are different paths
      if (!path.endsWith("*")) {
        this.filterChain.put(path + "/", filters);
      }

      return this;
    }

    public Map<String, String> build() {
      return filterChain;
    }
  }
}
