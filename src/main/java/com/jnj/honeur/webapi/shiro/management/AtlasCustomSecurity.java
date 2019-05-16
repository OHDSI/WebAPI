package com.jnj.honeur.webapi.shiro.management;

import com.jnj.honeur.webapi.shiro.filters.*;
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
import org.ohdsi.webapi.shiro.management.FilterChainBuilder;
import org.ohdsi.webapi.shiro.management.FilterTemplates;
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
import org.pac4j.oauth.client.GitHubClient;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.*;
import static org.ohdsi.webapi.util.QuoteUtils.dequote;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasCustomSecurity")
@DependsOn("flyway")
public class AtlasCustomSecurity extends AtlasSecurity {

    public static final String FINGERPRINT_ATTRIBUTE = "FINGERPRINT";

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${security.token.expiration}")
    private int tokenExpirationIntervalInSeconds;

    @Value("${webapi.central}")
    private boolean central;

    @Value("${security.cas.tgc.domain}")
    private String casTgcDomain;

    @Value("${security.cookies.domain}")
    private String cookiesDomain;

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

    @Value("${security.oauth.github.apiKey}")
    private String githubApiKey;

    @Value("${security.oauth.github.apiSecret}")
    private String githubApiSecret;

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
    private void addHoneurLocalRoleIfRemote() {
        if (!central) {
            this.defaultRoles.add("HONEUR-local");
        }
    }

    @Override
    public Map<FilterTemplates, Filter> getFilters() {

        Map<FilterTemplates, Filter> filters = super.getFilters();

        filters.put(LOGOUT, new HoneurLogoutFilter(eventPublisher));
        filters.put(UPDATE_TOKEN, new HoneurUpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds, this.cookiesDomain));

        filters.put(JWT_AUTHC, new HoneurJwtAuthFilter());
        filters.put(JDBC_FILTER, new JdbcAuthFilter(eventPublisher));
        filters.put(KERBEROS_FILTER, new KerberosAuthFilter());
        filters.put(LDAP_FILTER, new LdapAuthFilter(eventPublisher));
        filters.put(AD_FILTER, new ActiveDirectoryAuthFilter(eventPublisher));
        filters.put(NEGOTIATE_AUTHC, new NegotiateAuthenticationFilter());

        filters.put(SEND_TOKEN_IN_URL, new HoneurSendTokenInUrlFilter(this.oauthUiCallback));
        filters.put(SEND_TOKEN_IN_HEADER, new HoneurSendTokenInHeaderFilter());
        filters.put(SEND_TOKEN_IN_REDIRECT, new HoneurSendTokenInRedirectFilter(redirectUrl));

        // OAuth
        //
        Google2Client googleClient = new Google2Client(this.googleApiKey, this.googleApiSecret);
        googleClient.setScope(Google2Client.Google2Scope.EMAIL);

        FacebookClient facebookClient = new FacebookClient(this.facebookApiKey, this.facebookApiSecret);
        facebookClient.setScope("email");
        facebookClient.setFields("email");

        GitHubClient githubClient = new GitHubClient(this.githubApiKey, this.githubApiSecret);
        githubClient.setScope("user:email");

        OidcConfiguration configuration = oidcConfCreator.build();
        OidcClient oidcClient = new OidcClient(configuration);

        Config cfg =
                new Config(
                        new Clients(
                                this.oauthApiCallback
                                , googleClient
                                , facebookClient
                                , githubClient
                                , oidcClient
                                // ... put new clients here and then assign them to filters ...
                        )
                );

        // assign clients to filters
        SecurityFilter googleOauthFilter = new SecurityFilter();
        googleOauthFilter.setConfig(cfg);
        googleOauthFilter.setClients("Google2Client");
        filters.put(GOOGLE_AUTHC, googleOauthFilter);

        SecurityFilter facebookOauthFilter = new SecurityFilter();
        facebookOauthFilter.setConfig(cfg);
        facebookOauthFilter.setClients("FacebookClient");
        filters.put(FACEBOOK_AUTHC, facebookOauthFilter);

        SecurityFilter githubOauthFilter = new SecurityFilter();
        githubOauthFilter.setConfig(cfg);
        githubOauthFilter.setClients("GitHubClient");
        filters.put(GITHUB_AUTHC, githubOauthFilter);

        SecurityFilter oidcFilter = new SecurityFilter();
        oidcFilter.setConfig(cfg);
        oidcFilter.setClients("OidcClient");
        filters.put(OIDC_AUTH, oidcFilter);

        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(cfg);
        filters.put(OAUTH_CALLBACK, callbackFilter);
        filters.put(HANDLE_UNSUCCESSFUL_OAUTH, new RedirectOnFailedOAuthFilter(this.oauthUiCallback));

        this.setUpCAS(filters);

        return filters;
    }

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        // the order does matter - first match wins
        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setBeforeOAuthFilters(SSL, CORS, FORCE_SESSION_CREATION)
                .setAfterOAuthFilters(UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                .setRestFilters(SSL, NO_SESSION_CREATION, CORS, CAS_SESSION)
                .setAuthcFilter(JWT_AUTHC)
                .setAuthzFilter(AUTHZ)
                // login/logout
                .addRestPath("/user/login/openid", FORCE_SESSION_CREATION, OIDC_AUTH, UPDATE_TOKEN, SEND_TOKEN_IN_REDIRECT)
                .addRestPath("/user/login/windows", NEGOTIATE_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/kerberos", KERBEROS_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/db", JDBC_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/ldap", LDAP_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/ad", AD_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/refresh", JWT_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/logout", LOGOUT)
                .addOAuthPath("/user/oauth/google", GOOGLE_AUTHC)
                .addOAuthPath("/user/oauth/facebook", FACEBOOK_AUTHC)
                .addOAuthPath("/user/oauth/github", GITHUB_AUTHC)
                .addPath("/user/login/cas", SSL, CORS, FORCE_SESSION_CREATION, CAS_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                .addPath("/user/oauth/callback", SSL, HANDLE_UNSUCCESSFUL_OAUTH, OAUTH_CALLBACK)
                .addPath("/user/cas/callback", SSL, HANDLE_CAS, UPDATE_TOKEN, SEND_TOKEN_IN_URL)

                .addProtectedRestPath("/cohortdefinition/hss/select", CREATE_PERMISSIONS_ON_IMPORT_COHORT_DEFINITION)
                .addProtectedRestPath("/cohortdefinition/", CREATE_PERMISSIONS_ON_CREATE_COHORT_DEFINITION)
                .addProtectedRestPath("/cohortdefinition/*", DELETE_PERMISSIONS_ON_DELETE_COHORT_DEFINITION)
                .addProtectedRestPath("/cohortdefinition/*/export", DELETE_PERMISSIONS_ON_EXPORT_COHORT_DEFINITION, CREATE_PERMISSIONS_ON_CREATE_COHORT_DEFINITION)
                .addProtectedRestPath("/cohortdefinition/hss/select", CREATE_PERMISSIONS_ON_IMPORT_COHORT_DEFINITION);


        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
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
            public void doProcessResponseContent(String content) throws Exception {
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
                    return false;
                }
            }

            @Override
            public void doProcessResponseContent(String content) throws Exception {
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
                    return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
                }
            }

            @Override
            public void doProcessResponseContent(String content) throws Exception {
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

    private void setUpCAS(Map<FilterTemplates, Filter> filters) {
        try {
            /**
             * CAS config
             */
            CasConfiguration casConf = new CasConfiguration();

            String casLoginUrlString;
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
            filters.put(CAS_AUTHC, casAuthnFilter);

            /**
             * CAS callback filter
             */
            CasHandleFilter casHandleFilter = new CasHandleFilter(cas20Validator, casCallbackUrl, casticket);
            filters.put(HANDLE_CAS, casHandleFilter);

        } catch (UnsupportedEncodingException e) {
            this.logger.error("Atlas security filter errors: {}", e);
        }
    }
}
