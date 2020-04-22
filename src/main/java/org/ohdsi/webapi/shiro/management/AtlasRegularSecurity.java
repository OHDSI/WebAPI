package org.ohdsi.webapi.shiro.management;

import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.filters.CasHandleFilter;
import org.ohdsi.webapi.shiro.filters.LogoutFilter;
import org.ohdsi.webapi.shiro.filters.RedirectOnFailedOAuthFilter;
import org.ohdsi.webapi.shiro.filters.RunAsFilter;
import org.ohdsi.webapi.shiro.filters.SendTokenInHeaderFilter;
import org.ohdsi.webapi.shiro.filters.SendTokenInRedirectFilter;
import org.ohdsi.webapi.shiro.filters.SendTokenInUrlFilter;
import org.ohdsi.webapi.shiro.filters.UpdateAccessTokenFilter;
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
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.io.Resource;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.GitHubClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;
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
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;

import javax.naming.Context;
import javax.servlet.Filter;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static com.odysseusinc.arachne.commons.utils.QuoteUtils.dequote;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.AD_FILTER;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.AUTHZ;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CAS_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CORS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.FACEBOOK_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.FORCE_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.GITHUB_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.GOOGLE_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.HANDLE_CAS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.HANDLE_SAML;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.HANDLE_UNSUCCESSFUL_OAUTH;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.JDBC_FILTER;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.JWT_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.KERBEROS_FILTER;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.LDAP_FILTER;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.LOGOUT;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NEGOTIATE_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_CACHE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.OAUTH_CALLBACK;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.OIDC_AUTH;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.RUN_AS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SAML_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SEND_TOKEN_IN_HEADER;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SEND_TOKEN_IN_REDIRECT;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SEND_TOKEN_IN_URL;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SSL;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.UPDATE_TOKEN;

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

    @Value("${security.saml.emabled:false}")
    private boolean samlEnabled;

    public AtlasRegularSecurity(EntityPermissionSchemaResolver permissionSchemaResolver) {

        super(permissionSchemaResolver);
    }

    @Override
    public Map<FilterTemplates, Filter> getFilters() {

        Map<FilterTemplates, Filter> filters = super.getFilters();

        filters.put(LOGOUT, new LogoutFilter(eventPublisher));
        filters.put(UPDATE_TOKEN, new UpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds,
                this.redirectUrl));

        filters.put(JWT_AUTHC, new AtlasJwtAuthFilter());
        filters.put(JDBC_FILTER, new JdbcAuthFilter(eventPublisher));
        filters.put(KERBEROS_FILTER, new KerberosAuthFilter());
        filters.put(LDAP_FILTER, new LdapAuthFilter(eventPublisher));
        filters.put(AD_FILTER, new ActiveDirectoryAuthFilter(eventPublisher));
        filters.put(NEGOTIATE_AUTHC, new NegotiateAuthenticationFilter());

        filters.put(SEND_TOKEN_IN_URL, new SendTokenInUrlFilter(this.oauthUiCallback));
        filters.put(SEND_TOKEN_IN_HEADER, new SendTokenInHeaderFilter());
        filters.put(SEND_TOKEN_IN_REDIRECT, new SendTokenInRedirectFilter(redirectUrl));

        filters.put(RUN_AS, new RunAsFilter(userRepository));

        // OAuth
        Config cfg =
                new Config(
                        new Clients(
                                this.oauthApiCallback
                                , getGoogle2Client()
                                , getFacebookClient()
                                , getGitHubClient()
                                , getOidcClient()
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
        if (this.samlEnabled) {
            this.setUpSaml(filters);
        }

        return filters;
    }

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        // the order does matter - first match wins
        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setBeforeOAuthFilters(SSL, CORS, FORCE_SESSION_CREATION)
                .setAfterOAuthFilters(UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                .setRestFilters(SSL, NO_SESSION_CREATION, CORS, NO_CACHE)
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
                .addProtectedRestPath("/user/runas", RUN_AS, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/logout", LOGOUT)
                .addOAuthPath("/user/oauth/google", GOOGLE_AUTHC)
                .addOAuthPath("/user/oauth/facebook", FACEBOOK_AUTHC)
                .addOAuthPath("/user/oauth/github", GITHUB_AUTHC)
                .addPath("/user/login/cas", SSL, CORS, FORCE_SESSION_CREATION, CAS_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                .addPath("/user/oauth/callback", SSL, HANDLE_UNSUCCESSFUL_OAUTH, OAUTH_CALLBACK)
                .addPath("/user/cas/callback", SSL, HANDLE_CAS, UPDATE_TOKEN, SEND_TOKEN_IN_URL);

        if (this.samlEnabled) {
            filterChainBuilder
                    .addPath("/user/login/saml", SSL, CORS, FORCE_SESSION_CREATION, SAML_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_URL);
            filterChainBuilder
                    .addPath("/user/saml/callback", SSL, HANDLE_SAML, UPDATE_TOKEN, SEND_TOKEN_IN_URL);
        }

        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
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
        realms.add(new KerberosAuthRealm(kerberosSpn, kerberosKeytabPath));
        realms.add(ldapRealm());
        realms.add(activeDirectoryRealm());

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
        Resource keystorePath = CommonHelper.getResource(keyStoreFile);
        Resource metadataLocationPath = CommonHelper.getResource(metadataLocation);
        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration(
                keystorePath,
                alias,
                null,
                keyStorePassword,
                privateKeyPassword,
                metadataLocationPath);
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId(identityProviderEntityId);

        cfg.setServiceProviderMetadataPath(spMetadataLocation);
        cfg.setDestinationBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);

        final SAML2Client saml2Client = new SAML2Client(cfg);
        Config samlCfg = new Config(new Clients(samlCallbackUrl, saml2Client));

        SecurityFilter samlAuthFilter = new SecurityFilter();
        samlAuthFilter.setConfig(samlCfg);
        samlAuthFilter.setClients("saml2Client");
        filters.put(SAML_AUTHC, samlAuthFilter);

        SamlHandleFilter samlHandleFilter = new SamlHandleFilter(saml2Client);
        filters.put(HANDLE_SAML, samlHandleFilter);
    }

    private Google2Client getGoogle2Client() {

        Google2Client googleClient = new Google2Client(this.googleApiKey, this.googleApiSecret);
        googleClient.setScope(Google2Client.Google2Scope.EMAIL);
        return googleClient;
    }

    private JndiLdapRealm ldapRealm() {
        JndiLdapRealm realm = new LdapRealm(ldapSearchString, ldapSearchBase, ldapUserMapper);
        realm.setUserDnTemplate(dequote(userDnTemplate));
        JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
        contextFactory.setUrl(dequote(ldapUrl));
        contextFactory.setPoolingEnabled(false);
        contextFactory.getEnvironment().put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        realm.setContextFactory(contextFactory);
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
            casConf.setTicketValidator(cas20Validator);

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
