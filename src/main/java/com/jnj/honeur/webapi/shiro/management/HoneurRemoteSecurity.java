package com.jnj.honeur.webapi.shiro.management;

import com.jnj.honeur.webapi.shiro.filters.HoneurOriginFilter;
import org.ohdsi.webapi.shiro.management.AtlasRegularSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
@Primary
@DependsOn("flyway")
public class HoneurRemoteSecurity extends AtlasRegularSecurity {

    @Value("${security.honeur.request.origin}")
    private String honeurRequestOrigin;

    @Override
    public Map<String, Filter> getFilters() {
        Map<String, Filter> filters = super.getFilters();
        filters.put("honeur-request", new HoneurOriginFilter(honeurRequestOrigin));
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
                .addRestPath("/hss/token", "honeur-request") // added for HONEUR
                .addProtectedRestPath("/cohortdefinition/hss/select") // added for HONEUR
                .addOAuthPath("/user/oauth/google", "googleAuthc")
                .addOAuthPath("/user/oauth/facebook", "facebookAuthc")
                .addPath("/user/login/cas", "ssl, cors, forceSessionCreation, casAuthc, updateToken, sendTokenInUrl")
                .addPath("/user/oauth/callback", "ssl, handleUnsuccessfullOAuth, oauthCallback")
                .addPath("/user/cas/callback", "ssl, handleCas, updateToken, sendTokenInUrl");


        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
    }

}
