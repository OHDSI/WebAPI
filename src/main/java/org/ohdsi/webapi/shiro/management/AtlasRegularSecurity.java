package org.ohdsi.webapi.shiro.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.filters.*;
import org.ohdsi.webapi.shiro.filters.auth.ActiveDirectoryAuthFilter;
import org.ohdsi.webapi.shiro.filters.auth.AtlasJwtAuthFilter;
import org.ohdsi.webapi.shiro.filters.auth.JdbcAuthFilter;
import org.ohdsi.webapi.shiro.filters.auth.KerberosAuthFilter;
import org.ohdsi.webapi.shiro.filters.auth.LdapAuthFilter;
import org.ohdsi.webapi.shiro.filters.auth.SamlHandleFilter;
import org.ohdsi.webapi.shiro.mapper.ADUserMapper;
import org.ohdsi.webapi.shiro.mapper.LdapUserMapper;
import org.ohdsi.webapi.shiro.realms.ADRealm;
import org.ohdsi.webapi.shiro.realms.JdbcAuthRealm;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.ohdsi.webapi.shiro.realms.KerberosAuthRealm;
import org.ohdsi.webapi.shiro.realms.LdapRealm;
import org.ohdsi.webapi.user.importer.providers.LdapProvider;
import org.ohdsi.webapi.util.ResourceUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.callback.CallbackUrlResolver;
import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.credentials.authenticator.UserInfoOidcAuthenticator;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;

import javax.naming.Context;
import javax.servlet.Filter;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.odysseusinc.arachne.commons.utils.QuoteUtils.dequote;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.*;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = Constants.SecurityProviders.REGULAR)
@DependsOn("flyway")
public class AtlasRegularSecurity extends AtlasSecurity {

    private final Logger logger = LoggerFactory.getLogger(AtlasRegularSecurity.class);

    @Value("${security.token.expiration}")
    private int tokenExpirationIntervalInSeconds;

    @Value("${security.oauth.callback.ui}")
    private String oauthUiCallback;

    @Value("${security.oauth.callback.api}")
    private String oauthApiCallback;

    @Value("${security.oauth.callback.urlResolver}")
    private String oauthCallbackUrlResolver;

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

    @Value("${security.kerberos.spn}")
    private String kerberosSpn;

    @Value("${security.kerberos.keytabPath}")
    private String kerberosKeytabPath;

    @Value("${security.ldap.dn}")
    private String userDnTemplate;

    @Value("${security.ldap.url}")
    private String ldapUrl;

    @Value("${security.ldap.searchString}")
    private String ldapSearchString;

    @Value("${security.ldap.searchBase}")
    private String ldapSearchBase;

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

    @Value("${security.ad.searchString}")
    private String adSearchString;

    @Value("${security.ad.ignore.partial.result.exception}")
    private Boolean adIgnorePartialResultException;

    @Value("${security.google.accessToken.enabled}")
    private Boolean googleAccessTokenEnabled;

    @Value("${security.saml.keyManager.storePassword}")
    private String keyStorePassword;

    @Value("${security.saml.keyManager.passwords.arachnenetwork}")
    private String privateKeyPassword;

    @Value("${security.saml.entityId}")
    private String identityProviderEntityId;

    @Value("${security.saml.idpMetadataLocation}")
    private String metadataLocation;

    @Value("${security.saml.keyManager.keyStoreFile}")
    private String keyStoreFile;

    @Value("${security.saml.keyManager.defaultKey}")
    private String alias;

    @Value("${security.saml.metadataLocation}")
    private String spMetadataLocation;

    @Value("${security.saml.callbackUrl}")
    private String samlCallbackUrl;

    @Value("${security.saml.maximumAuthenticationLifetime}")
    private int maximumAuthenticationLifetime;

    @Autowired
    @Qualifier("activeDirectoryProvider")
    private LdapProvider adLdapProvider;

    @Autowired
    @Qualifier("authDataSource")
    private DataSource jdbcDataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ADUserMapper adUserMapper;

    @Autowired
    private LdapUserMapper ldapUserMapper;

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

    @Value("${security.saml.enabled:false}")
    private boolean samlEnabled;

    @Value("${security.auth.windows.enabled}")
    private boolean windowsAuthEnabled;

    @Value("${security.auth.kerberos.enabled}")
    private boolean kerberosAuthEnabled;

    @Value("${security.auth.jdbc.enabled}")
    private boolean jdbcAuthEnabled;

    @Value("${security.auth.ldap.enabled}")
    private boolean ldapAuthEnabled;

    @Value("${security.auth.ad.enabled}")
    private boolean adAuthEnabled;

    @Value("${security.auth.cas.enabled}")
    private boolean casAuthEnabled;

    @Value("${security.auth.openid.enabled}")
    private boolean openidAuthEnabled;

    @Value("${security.auth.facebook.enabled}")
    private boolean facebookAuthEnabled;

    @Value("${security.auth.github.enabled}")
    private boolean githubAuthEnabled;

    @Value("${security.auth.google.enabled}")
    private boolean googleAuthEnabled;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private PermissionManager permissionManager;
    
    @Autowired
    private ObjectMapper objectMapper;    

    public AtlasRegularSecurity(EntityPermissionSchemaResolver permissionSchemaResolver) {

        super(permissionSchemaResolver);
    }

    @Override
    public Map<FilterTemplates, Filter> getFilters() {

        Map<FilterTemplates, Filter> filters = super.getFilters();

        filters.put(LOGOUT, new LogoutFilter(eventPublisher));
        filters.put(UPDATE_TOKEN, new UpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds,
                this.redirectUrl));

        filters.put(ACCESS_AUTHC, new GoogleAccessTokenFilter(restTemplate, permissionManager, Collections.emptySet()));
        filters.put(JWT_AUTHC, new AtlasJwtAuthFilter());
        if (this.jdbcAuthEnabled) {
            filters.put(JDBC_FILTER, new JdbcAuthFilter(eventPublisher));
        }
        if (this.kerberosAuthEnabled) {
            filters.put(KERBEROS_FILTER, new KerberosAuthFilter());
        }
        if (this.ldapAuthEnabled) {
            filters.put(LDAP_FILTER, new LdapAuthFilter(eventPublisher));
        }
        if (this.adAuthEnabled) {
            filters.put(AD_FILTER, new ActiveDirectoryAuthFilter(eventPublisher));
        }
        if (this.windowsAuthEnabled) {
            filters.put(NEGOTIATE_AUTHC, new NegotiateAuthenticationFilter());
        }

        filters.put(SEND_TOKEN_IN_URL, new SendTokenInUrlFilter(this.oauthUiCallback));
        filters.put(SEND_TOKEN_IN_HEADER, new SendTokenInHeaderFilter(this.objectMapper));

        filters.put(RUN_AS, new RunAsFilter(userRepository));

        // OAuth
        //
        CallbackUrlResolver urlResolver = getCallbackUrlResolver();
        List<Client> clients = new ArrayList<>();
        if (this.googleAuthEnabled) {
            Google2Client googleClient = new Google2Client(this.googleApiKey, this.googleApiSecret);
            googleClient.setCallbackUrl(oauthApiCallback);
            googleClient.setCallbackUrlResolver(urlResolver);
            googleClient.setScope(Google2Client.Google2Scope.EMAIL_AND_PROFILE);
            clients.add(googleClient);
        }

        if (this.facebookAuthEnabled) {
            FacebookClient facebookClient = new FacebookClient(this.facebookApiKey, this.facebookApiSecret);
            facebookClient.setScope("email");
            facebookClient.setFields("email");
            clients.add(facebookClient);
        }

        if (this.githubAuthEnabled) {
            GitHubClient githubClient = new GitHubClient(this.githubApiKey, this.githubApiSecret);
            githubClient.setScope("user:email");
            clients.add(githubClient);
        }

        if (this.openidAuthEnabled) {
            OidcConfiguration configuration = oidcConfCreator.build();
            if (StringUtils.isNotBlank(configuration.getClientId())) {
                // https://www.pac4j.org/4.0.x/docs/clients/openid-connect.html
                // OidcClient allows indirect login through UI with code flow            
                OidcClient oidcClient = new OidcClient(configuration);
                oidcClient.setCallbackUrl(oauthApiCallback);
                oidcClient.setCallbackUrlResolver(urlResolver);
                clients.add(oidcClient);
                // HeaderClient allows api access with a bearer token from the identity provider
                UserInfoOidcAuthenticator authenticator = new UserInfoOidcAuthenticator(configuration);
                HeaderClient headerClient = new HeaderClient("Authorization", "Bearer ", authenticator);
                clients.add(headerClient);
            } else {
                logger.warn("openidAuth is enabled but no client id is provided");
            }
        }

        if (clients.size() > 0) {
            Config cfg =
                    new Config(
                            new Clients(
                                    this.oauthApiCallback,
                                    clients
                            )
                    );

            // assign clients to filters
            if (this.googleAuthEnabled) {
                SecurityFilter googleOauthFilter = new SecurityFilter();
                googleOauthFilter.setConfig(cfg);
                googleOauthFilter.setClients("Google2Client");
                filters.put(GOOGLE_AUTHC, googleOauthFilter);
            }

            if (this.facebookAuthEnabled) {
                SecurityFilter facebookOauthFilter = new SecurityFilter();
                facebookOauthFilter.setConfig(cfg);
                facebookOauthFilter.setClients("FacebookClient");
                filters.put(FACEBOOK_AUTHC, facebookOauthFilter);
            }

            if (this.githubAuthEnabled) {
                SecurityFilter githubOauthFilter = new SecurityFilter();
                githubOauthFilter.setConfig(cfg);
                githubOauthFilter.setClients("GitHubClient");
                filters.put(GITHUB_AUTHC, githubOauthFilter);
            }

            if (this.openidAuthEnabled) {
                SecurityFilter oidcFilter = new SecurityFilter();
                oidcFilter.setConfig(cfg);
                oidcFilter.setClients("OidcClient");
                filters.put(OIDC_AUTH, oidcFilter);

                SecurityFilter oidcDirectFilter = new SecurityFilter();
                oidcDirectFilter.setConfig(cfg);
                oidcDirectFilter.setClients("HeaderClient");
                filters.put(OIDC_DIRECT_AUTH, oidcDirectFilter);
            }

            CallbackFilter callbackFilter = new CallbackFilter();
            callbackFilter.setConfig(cfg);
            filters.put(OAUTH_CALLBACK, callbackFilter);
            filters.put(HANDLE_UNSUCCESSFUL_OAUTH, new RedirectOnFailedOAuthFilter(this.oauthUiCallback));
        }

        if (this.casAuthEnabled) {
            this.setUpCAS(filters);
        }
        if (this.samlEnabled) {
            this.setUpSaml(filters);
        }

        return filters;
    }

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        List<FilterTemplates> authcFilters = googleAccessTokenEnabled ? Arrays.asList(ACCESS_AUTHC, JWT_AUTHC) :
                Collections.singletonList(JWT_AUTHC);
        // the order does matter - first match wins
        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setRestFilters(SSL, NO_SESSION_CREATION, CORS, NO_CACHE)
                .setAuthcFilter(authcFilters.toArray(new FilterTemplates[0]))
                .setAuthzFilter(AUTHZ)
                // login/logout
                .addRestPath("/user/refresh", JWT_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addProtectedRestPath("/user/runas", RUN_AS, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/logout", LOGOUT);

        // MUST be called before adding OAuth filters
        if (this.openidAuthEnabled || this.googleAuthEnabled || this.facebookAuthEnabled || this.githubAuthEnabled) {
            filterChainBuilder
                    .setBeforeOAuthFilters(SSL, CORS, FORCE_SESSION_CREATION)
                    .setAfterOAuthFilters(UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                    .addPath("/user/oauth/callback", OAUTH_CALLBACK_FILTERS)
                    .addPath("/user/oauth/callback/*", OAUTH_CALLBACK_FILTERS);
        }

        if (this.openidAuthEnabled) {
            filterChainBuilder
                    .addRestPath("/user/login/openid", FORCE_SESSION_CREATION, OIDC_AUTH, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                    .addRestPath("/user/login/openidDirect", FORCE_SESSION_CREATION, OIDC_DIRECT_AUTH, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER);
        }

        if (this.googleAuthEnabled) {
            filterChainBuilder.addOAuthPath("/user/oauth/google", GOOGLE_AUTHC);
        }

        if (this.facebookAuthEnabled) {
            filterChainBuilder.addOAuthPath("/user/oauth/facebook", FACEBOOK_AUTHC);
        }

        if (this.githubAuthEnabled) {
            filterChainBuilder.addOAuthPath("/user/oauth/github", GITHUB_AUTHC);
        }

        if (this.kerberosAuthEnabled) {
            filterChainBuilder.addRestPath("/user/login/kerberos", KERBEROS_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER);
        }

        if (this.windowsAuthEnabled) {
            filterChainBuilder.addRestPath("/user/login/windows", NEGOTIATE_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER);
        }

        if (this.jdbcAuthEnabled) {
            filterChainBuilder.addRestPath("/user/login/db", JDBC_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER);
        }

        if (this.ldapAuthEnabled) {
            filterChainBuilder.addRestPath("/user/login/ldap", LDAP_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER);
        }

        if (this.adAuthEnabled) {
            filterChainBuilder.addRestPath("/user/login/ad", AD_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER);
        }

        if (this.casAuthEnabled) {
            filterChainBuilder
                    .addPath("/user/login/cas", SSL, CORS, FORCE_SESSION_CREATION, CAS_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                    .addPath("/user/cas/callback", SSL, HANDLE_CAS, UPDATE_TOKEN, SEND_TOKEN_IN_URL);
        }


        if (this.samlEnabled) {
            filterChainBuilder
                    .addPath("/user/login/saml", SSL, CORS, FORCE_SESSION_CREATION, SAML_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                    .addPath("/user/login/samlForce", SSL, CORS, FORCE_SESSION_CREATION, SAML_AUTHC_FORCE, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                    .addPath("/user/saml/callback", SSL, HANDLE_SAML, UPDATE_TOKEN, SEND_TOKEN_IN_URL);
        }

        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
    }

    @Override
    public Set<Realm> getRealms() {
        Set<Realm> realms = super.getRealms();

        realms.add(new JwtAuthRealm(this.authorizer));
        if (this.windowsAuthEnabled) {
            realms.add(new NegotiateAuthenticationRealm());
        }
        realms.add(new Pac4jRealm());
        if (jdbcAuthEnabled && jdbcDataSource != null) {
            realms.add(new JdbcAuthRealm(jdbcDataSource, jdbcAuthenticationQuery));
        }
        if (this.kerberosAuthEnabled) {
            realms.add(new KerberosAuthRealm(kerberosSpn, kerberosKeytabPath));
        }
        if (this.ldapAuthEnabled) {
            realms.add(ldapRealm());
        }
        if (this.adAuthEnabled) {
            realms.add(activeDirectoryRealm());
        }

        return realms;
    }

    private OidcClient getOidcClient() {

        OidcConfiguration configuration = oidcConfCreator.build();
        return new OidcClient(configuration);
    }

    private GitHubClient getGitHubClient() {

        GitHubClient githubClient = new GitHubClient(this.githubApiKey, this.githubApiSecret);
        githubClient.setScope("user:email");
        return githubClient;
    }

    private FacebookClient getFacebookClient() {

        FacebookClient facebookClient = new FacebookClient(this.facebookApiKey, this.facebookApiSecret);
        facebookClient.setScope("email");
        facebookClient.setFields("email");
        return facebookClient;
    }

    private void setUpSaml(Map<FilterTemplates, Filter> filters) {
        try {
            SAML2Client client = setUpSamlClient(filters, SAML_AUTHC, false);
            SAML2Client clientForce = setUpSamlClient(filters, SAML_AUTHC_FORCE, true);

            SamlHandleFilter samlHandleFilter = new SamlHandleFilter(client, clientForce, this.oauthUiCallback);
            filters.put(HANDLE_SAML, samlHandleFilter);

            samlEnabled = true;
        } catch (Exception e) {
            samlEnabled = false;
            filters.remove(SAML_AUTHC_FORCE);
            filters.remove(SAML_AUTHC);
            filters.remove(HANDLE_SAML);
            logger.error("Failed to initlize SAML filters: " + e.getMessage());
        }
    }

    private SAML2Client setUpSamlClient(Map<FilterTemplates, Filter> filters, FilterTemplates template, boolean isForcedAuth) {
        final SAML2Configuration cfg = new SAML2Configuration(
                ResourceUtils.mapPathToResource(keyStoreFile),
                alias,
                null,
                keyStorePassword,
                privateKeyPassword,
                ResourceUtils.mapPathToResource(metadataLocation));

        cfg.setMaximumAuthenticationLifetime(this.maximumAuthenticationLifetime);
        cfg.setServiceProviderEntityId(identityProviderEntityId);
        cfg.setForceAuth(isForcedAuth);

        cfg.setServiceProviderMetadataPath(spMetadataLocation);
        cfg.setAuthnRequestBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);

        final SAML2Client saml2Client = new SAML2Client(cfg);
        Config samlCfg = new Config(new Clients(samlCallbackUrl, saml2Client));

        SecurityFilter samlAuthFilter = new SecurityFilter();
        samlAuthFilter.setConfig(samlCfg);
        samlAuthFilter.setClients("saml2Client");
        filters.put(template, samlAuthFilter);

        return saml2Client;
    }

    private Google2Client getGoogle2Client() {

        Google2Client googleClient = new Google2Client(this.googleApiKey, this.googleApiSecret);
        googleClient.setScope(Google2Client.Google2Scope.EMAIL);
        return googleClient;
    }

    private DefaultLdapRealm ldapRealm() {
        DefaultLdapRealm realm = new LdapRealm(ldapSearchString, ldapSearchBase, ldapUserMapper);

        JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
        contextFactory.setUrl(dequote(ldapUrl));
        contextFactory.setPoolingEnabled(false);
        contextFactory.getEnvironment().put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        realm.setContextFactory(contextFactory);
        if (StringUtils.isNotEmpty(userDnTemplate)) {
            realm.setUserDnTemplate(dequote(userDnTemplate));
        }
        return realm;
    }

    private ActiveDirectoryRealm activeDirectoryRealm() {
        ActiveDirectoryRealm realm = new ADRealm(getLdapTemplate(), adSearchFilter, adSearchString, adUserMapper);
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

    private CallbackUrlResolver getCallbackUrlResolver() {

        CallbackUrlResolver resolver = new QueryParameterCallbackUrlResolver();
        if (Constants.CallbackUrlResolvers.PATH.equals(oauthCallbackUrlResolver)) {
            resolver = new PathParameterCallbackUrlResolver();
        } else if (!Constants.CallbackUrlResolvers.QUERY.equals(oauthCallbackUrlResolver)) {
            this.logger.warn("OAuth Callback Url Resolver {} not supported, ignored and used default QueryParameterUrlResolver", oauthCallbackUrlResolver);
        }
        return resolver;
    }

    public boolean isSamlEnabled() {
        return samlEnabled;
    }
}
