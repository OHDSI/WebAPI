package com.jnj.honeur.webapi.shiro.management;

import com.jnj.honeur.webapi.shiro.filters.HoneurOriginFilter;
import org.ohdsi.webapi.shiro.management.AtlasRegularSecurity;
import org.ohdsi.webapi.shiro.management.FilterChainBuilder;
import org.ohdsi.webapi.shiro.management.FilterTemplates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.Map;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.*;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
@Primary
@DependsOn("flyway")
public class HoneurRemoteSecurity extends AtlasRegularSecurity {

    @Value("${security.honeur.request.origin}")
    private String honeurRequestOrigin;

    @Override
    public Map<FilterTemplates, Filter> getFilters() {
        Map<FilterTemplates, Filter> filters = super.getFilters();
        filters.put(HONEUR_REQUEST, new HoneurOriginFilter(honeurRequestOrigin));
        return filters;
    }

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        // the order does matter - first match wins
        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setBeforeOAuthFilters(SSL, CORS, FORCE_SESSION_CREATION)
                .setAfterOAuthFilters(UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                .setRestFilters(SSL, NO_SESSION_CREATION, CORS)
                .setAuthcFilter(JWT_AUTHC)
                .setAuthzFilter(AUTHZ)
                // login/logout
                .addRestPath("/user/login/openid", FORCE_SESSION_CREATION, OIDC_AUTH, UPDATE_TOKEN, SEND_TOKEN_IN_REDIRECT)
                .addRestPath("/user/login/windows",NEGOTIATE_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/kerberos", KERBEROS_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/db",  JDBC_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/ldap", LDAP_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/login/ad", AD_FILTER, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/refresh", JWT_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_HEADER)
                .addRestPath("/user/logout", LOGOUT)
                .addRestPath("/hss/token", "honeur-request")    // added for HONEUR
                .addRestPath("/actuator/health/liveness")
                .addRestPath("/actuator/health/readyness")
                .addProtectedRestPath("/hss/user")
                .addProtectedRestPath("/cohortdefinition/hss/select")       // added for HONEUR
                .addOAuthPath("/user/oauth/google", GOOGLE_AUTHC)
                .addOAuthPath("/user/oauth/facebook", FACEBOOK_AUTHC)
                .addOAuthPath("/user/oauth/github", GITHUB_AUTHC)
                .addPath("/user/login/cas", SSL, CORS, FORCE_SESSION_CREATION, CAS_AUTHC, UPDATE_TOKEN, SEND_TOKEN_IN_URL)
                .addPath("/user/oauth/callback", SSL, HANDLE_UNSUCCESSFUL_OAUTH, OAUTH_CALLBACK)
                .addPath("/user/cas/callback", SSL, HANDLE_CAS, UPDATE_TOKEN, SEND_TOKEN_IN_URL);

        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
    }

}
