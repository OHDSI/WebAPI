package org.ohdsi.webapi.shiro.management;

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
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.ohdsi.webapi.user.importer.providers.LdapProvider;
import org.ohdsi.webapi.shiro.filters.LogoutFilter;
import org.ohdsi.webapi.shiro.filters.SendTokenInHeaderFilter;
import org.ohdsi.webapi.shiro.filters.SendTokenInRedirectFilter;
import org.ohdsi.webapi.shiro.filters.UpdateAccessTokenFilter;
import org.ohdsi.webapi.shiro.realms.ADRealm;
import org.ohdsi.webapi.shiro.filters.ActiveDirectoryAuthFilter;
import org.ohdsi.webapi.shiro.filters.JdbcAuthFilter;
import org.ohdsi.webapi.shiro.realms.JdbcAuthRealm;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.ohdsi.webapi.shiro.filters.LdapAuthFilter;
import org.ohdsi.webapi.shiro.realms.LdapRealm;
import org.ohdsi.webapi.shiro.filters.RedirectOnFailedOAuthFilter;
import org.ohdsi.webapi.shiro.filters.SendTokenInUrlFilter;
import org.ohdsi.webapi.shiro.filters.AtlasJwtAuthFilter;
import org.ohdsi.webapi.shiro.filters.CasHandleFilter;
import org.ohdsi.webapi.shiro.filters.KerberosAuthFilter;
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

import javax.naming.Context;
import javax.servlet.Filter;
import javax.sql.DataSource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.ohdsi.webapi.util.QuoteUtils.dequote;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
@DependsOn("flyway")
public class AtlasRegularSecurity extends AtlasSecurity {
    
    private final Log logger = LogFactory.getLog(AtlasRegularSecurity.class);

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

    @Autowired
    @Qualifier("authDataSource")
    private DataSource jdbcDataSource;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
    
    @Override
    public Map<String, Filter> getFilters() {

        Map<String, Filter> filters = super.getFilters();

        filters.put("logout", new LogoutFilter(eventPublisher));
        filters.put("updateToken", new UpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds));

        filters.put("jwtAuthc", new AtlasJwtAuthFilter());
        filters.put("jdbcFilter", new JdbcAuthFilter(eventPublisher));
        filters.put("kerberosFilter", new KerberosAuthFilter());
        filters.put("ldapFilter", new LdapAuthFilter(eventPublisher));
        filters.put("adFilter", new ActiveDirectoryAuthFilter(eventPublisher));
        filters.put("negotiateAuthc", new NegotiateAuthenticationFilter());

        filters.put("sendTokenInUrl", new SendTokenInUrlFilter(this.oauthUiCallback));
        filters.put("sendTokenInHeader", new SendTokenInHeaderFilter());
        filters.put("sendTokenInRedirect", new SendTokenInRedirectFilter(redirectUrl));

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

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        // the order does matter - first match wins
        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setOAuthFilters("ssl, cors, forceSessionCreation", "updateToken, sendTokenInUrl")
                .setRestFilters("ssl, noSessionCreation, cors")
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
                .addPath("/user/cas/callback", "ssl, handleCas, updateToken, sendTokenInUrl");

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
            casConf.setTicketValidator(cas20Validator);
            
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
