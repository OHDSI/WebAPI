package com.jnj.honeur.webapi.shiro.management;

import com.jnj.honeur.webapi.cas.filter.CASSessionFilter;
import com.jnj.honeur.webapi.shiro.filters.HoneurLogoutFilter;
import com.jnj.honeur.webapi.shiro.filters.HoneurJwtAuthFilter;
import com.jnj.honeur.webapi.shiro.filters.HoneurUpdateAccessTokenFilter;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.filters.*;
import org.ohdsi.webapi.shiro.management.AtlasSecurity;
import org.ohdsi.webapi.shiro.realms.ADRealm;
import org.ohdsi.webapi.shiro.realms.JdbcAuthRealm;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.ohdsi.webapi.shiro.realms.LdapRealm;
import org.ohdsi.webapi.user.importer.providers.LdapProvider;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.HttpMethod;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.ohdsi.webapi.util.QuoteUtils.dequote;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasCustomSecurity")
@DependsOn("flyway")
public class AtlasCustomSecurity extends AtlasSecurity {

  private final Log logger = LogFactory.getLog(getClass());

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Value("${security.token.expiration}")
  private int tokenExpirationIntervalInSeconds;

  @Value("${webapi.central}")
  private boolean central;

  @Value("${security.cas.tgc.domain}")
  private String casTgcDomain;

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

  @Value("${security.db.datasource.authenticationQuery}")
  private String jdbcAuthenticationQuery;

  @Value("${security.ad.searchFilter}")
  private String adSearchFilter;

  @Value("${security.ad.ignore.partial.result.exception}")
  private Boolean adIgnorePartialResultException;

  @Autowired
  @Qualifier("activeDirectoryProvider")
  private LdapProvider adLdapProvider;

  @Autowired(required = false)
  @Qualifier("authDataSource")
  private DataSource jdbcDataSource;

  @Value("${security.oid.redirectUrl}")
  private String redirectUrl;

  @Value("${security.cas.loginUrl}")
  private String casLoginUrl;

  @Value("${security.cas.callbackUrl}")
  private String casCallbackUrl;

  @Value("${security.cas.serverUrl}")
  private String casServerUrl;

  @Value("${security.cas.cassvcs}")
  private String casSvcs;

  @Value("${security.cas.casticket}")
  private String casticket;

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> cohortdefinitionExporterPermissionTemplatesToDelete = new LinkedHashMap<>();
  private final Map<String, String> cohortdefinitionImporterPermissionTemplates = new LinkedHashMap<>();

  public AtlasCustomSecurity() {
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:get", "View Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:export:get", "Export Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:hss:list:%s:get", "List Cohort Definition Generation Results in Amazon for Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:hss:%s:select:*:post", "Import Cohort Definition Generation Results for Cohort Definition with ID = %s");

    this.cohortdefinitionExporterPermissionTemplatesToDelete
            .put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
    this.cohortdefinitionExporterPermissionTemplatesToDelete
            .put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");
    this.cohortdefinitionExporterPermissionTemplatesToDelete
            .put("cohortdefinition:%s:export:get", "Export Cohort Definition with ID = %s");

//    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:generate:*:get", "Generate Cohort Definition generation results for defintion with ID = %s");
    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");
    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:get", "View Cohort Definition with ID = %s");
//    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:export:*:get", "Export Cohort Definition generation results for defintion with ID = %s");
    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:info:get", "Get Cohort Definition Info for cohort definition with ID = %s");

  }

  @PostConstruct
  private void addHoneurLocalRoleIfRemote(){
    if(!central){
      this.defaultRoles.add("HONEUR-local");
    }
  }

  @Override
  protected FilterChainBuilder getFilterChainBuilder() {

    FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
            .setOAuthFilters("ssl, cors, forceSessionCreation", "updateToken, sendTokenInUrl")
            .setRestFilters("ssl, noSessionCreation, cors, casSessionFilter")
            .setAuthcFilter("jwtAuthc")
            .setAuthzFilter("authz")
            // login/logout
            .addRestPath("/user/login/openid", "forceSessionCreation, oidcAuth, updateToken, sendTokenInRedirect")
            .addRestPath("/user/login/windows","negotiateAuthc, updateToken, sendTokenInHeader")
            .addRestPath("/user/login/kerberos","kerberosFilter, updateToken, sendTokenInHeader")
            .addRestPath("/user/login/db", "jdbcFilter, updateToken, sendTokenInHeader")
            .addRestPath("/user/login/ldap", "ldapFilter, updateToken, sendTokenInHeader")
            .addRestPath("/user/login/ad", "adFilter, updateToken, sendTokenInHeader")
            .addRestPath("/user/refresh", "jwtAuthc, updateToken, sendTokenInHeader")
            .addRestPath("/user/logout", "logout")
            .addOAuthPath("/user/oauth/google", "googleAuthc")
            .addOAuthPath("/user/oauth/facebook", "facebookAuthc")
            .addPath("/user/login/cas", "ssl, cors, forceSessionCreation, casAuthc, updateToken, sendTokenInUrl")
            .addPath("/user/oauth/callback", "ssl, handleUnsuccessfullOAuth, oauthCallback")
            .addPath("/user/cas/callback", "ssl, handleCas, updateToken, sendTokenInUrl")

            .addProtectedRestPath("/cohortdefinition/hss/select", "createPermissionsOnImportCohortDefinition")
            .addProtectedRestPath("/cohortdefinition/*", "deletePermissionsOnDeleteCohortDefinition");

    setupProtectedPaths(filterChainBuilder);

    return filterChainBuilder.addRestPath("/**");
  }

    @Override
  public Map<String, Filter> getFilters() {

      Map<String, Filter> filters = super.getFilters();

      filters.put("logout", new HoneurLogoutFilter());
      filters.put("updateToken", new HoneurUpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds));

      filters.put("jwtAuthc", new HoneurJwtAuthFilter());
      filters.put("jdbcFilter", new JdbcAuthFilter(eventPublisher));
      filters.put("kerberosFilter", new KerberosAuthFilter());
      filters.put("ldapFilter", new LdapAuthFilter(eventPublisher));
      filters.put("adFilter", new ActiveDirectoryAuthFilter(eventPublisher));
      filters.put("negotiateAuthc", new NegotiateAuthenticationFilter());

      filters.put("sendTokenInUrl", new SendTokenInUrlFilter(this.oauthUiCallback));
      filters.put("sendTokenInHeader", new SendTokenInHeaderFilter());
      filters.put("sendTokenInRedirect", new SendTokenInRedirectFilter(redirectUrl));

      filters.put("createPermissionsOnImportCohortDefinition", this.getCreatePermissionsOnImportCohortDefinitionFilter());
      filters.put("deletePermissionsOnExportCohortDefinition", this.getDeletePermissionsOnExportCohortDefinitionFilter());
      filters.put("createPermissionsOnCreateCohortDefinition", this.getCreatePermissionsOnCreateCohortDefinitionFilter());
      filters.put("deletePermissionsOnDeleteCohortDefinition", this.getDeletePermissionsOnDeleteCohortDefinitionFilter());
      filters.put("casSessionFilter", new CASSessionFilter(true, casTgcDomain));

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

      this.setUpCAS(filters);

    return filters;
  }

  private Filter getCreatePermissionsOnImportCohortDefinitionFilter() {
      return new ProcessResponseContentFilter() {
          @Override
          protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
              HttpServletRequest httpRequest = WebUtils.toHttp(request);
              String path = httpRequest.getPathInfo().replaceAll("/+$", "");

              if (StringUtils.endsWithIgnoreCase(path, "hss/select")) {
                  return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
              }
              return false;
          }

          @Override
          protected void doProcessResponseContent(String content) throws Exception {
              String id = this.parseJsonField(content, "id");
              RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
              authorizer.addPermissionsFromTemplate(currentUserPersonalRole, cohortdefinitionImporterPermissionTemplates,
                      id);
          }
      };
  }

  private Filter getDeletePermissionsOnExportCohortDefinitionFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "export")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        } else {
          return  false;
        }
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String previousVersion = this.parseJsonField(content, "previousVersion");
        String id = this.parseJsonField(previousVersion, "id");
        authorizer.removePermissionsFromTemplate(cohortdefinitionExporterPermissionTemplatesToDelete, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateCohortDefinitionFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "copy")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        } else if (StringUtils.endsWithIgnoreCase(path, "export")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        } else {
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
        authorizer.removePermissionsFromTemplate(cohortdefinitionImporterPermissionTemplates, id);
      }
    };
  }

  @Override
  public Set<Realm> getRealms() {
    Set<Realm> realms = super.getRealms();

    realms.add(new JwtAuthRealm(this.authorizer));
    realms.add(new NegotiateAuthenticationRealm());
    realms.add(new Pac4jRealm());
    if (jdbcDataSource != null) {
      realms.add(new JdbcAuthRealm(jdbcDataSource, jdbcAuthenticationQuery));
    }
    realms.add(ldapRealm());
    realms.add(activeDirectoryRealm());

    return realms;
  }

  private JndiLdapRealm ldapRealm() {
    JndiLdapRealm realm = new LdapRealm();
    realm.setUserDnTemplate(dequote(userDnTemplate));
    JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
    contextFactory.setUrl(dequote(ldapUrl));
    contextFactory.setPoolingEnabled(false);
    contextFactory.getEnvironment().put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    realm.setContextFactory(contextFactory);
    return realm;
  }

  private ActiveDirectoryRealm activeDirectoryRealm() {
    ActiveDirectoryRealm realm = new ADRealm(getLdapTemplate(), adSearchFilter);
    realm.setUrl(dequote(adUrl));
    realm.setSearchBase(dequote(adSearchBase));
    realm.setPrincipalSuffix(dequote(adPrincipalSuffix));
    realm.setSystemUsername(dequote(adSystemUsername));
    realm.setSystemPassword(dequote(adSystemPassword));
    return realm;
  }

  private LdapTemplate getLdapTemplate() {

    if (StringUtils.isNotBlank(adSearchFilter)) {
      return adLdapProvider.getLdapTemplate();
    }
    return null;
  }

  private void setUpCAS(Map<String, Filter> filters) {
    try {
      /**
       * CAS config
       */
      CasConfiguration casConf = new CasConfiguration();

      String casLoginUrlString = "";
      if (casSvcs != null && !"".equals(casSvcs)) {
        casLoginUrlString = casLoginUrl + "?cassvc=" + casSvcs + "&casurl="
                + URLEncoder.encode(casCallbackUrl, StandardCharsets.UTF_8.name());
      } else {
        casLoginUrlString = casLoginUrl + "?casurl="
                + URLEncoder.encode(casCallbackUrl, StandardCharsets.UTF_8.name());
      }
      casConf.setLoginUrl(casLoginUrlString);

      Cas20ServiceTicketValidator cas20Validator = new Cas20ServiceTicketValidator(casServerUrl);
      casConf.setDefaultTicketValidator(cas20Validator);

      CasClient casClient = new CasClient(casConf);
      Config casCfg = new Config(new Clients(casCallbackUrl, casClient));

      /**
       * CAS filter
       */
      SecurityFilter casAuthnFilter = new SecurityFilter();
      casAuthnFilter.setConfig(casCfg);
      casAuthnFilter.setClients("CasClient");
      filters.put("casAuthc", casAuthnFilter);

      /**
       * CAS callback filter
       */
      CasHandleFilter casHandleFilter = new CasHandleFilter(cas20Validator, casCallbackUrl, casticket);
      filters.put("handleCas", casHandleFilter);

    } catch (UnsupportedEncodingException e) {
      this.logger.error("Atlas security filter errors:" + this.toString());
      e.printStackTrace();
    }
  }
}
