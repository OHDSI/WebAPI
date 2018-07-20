package org.ohdsi.webapi.shiro.management;

import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.shiro.filters.GoogleIapJwtAuthFilter;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasGoogleSecurity")
@DependsOn("flyway")
public class AtlasGoogleSecurity extends AtlasSecurity {

    // Execute in console to get the ID:
    // gcloud config get-value account | tr -cd "[0-9]"
    @Value("${security.googleIap.cloudProjectId}")
    private Long googleCloudProjectId;

    // Execute in console to get the ID:
    // gcloud compute backend-services describe my-backend-service --global --format="value(id)"
    @Value("${security.googleIap.backendServiceId}")
    private Long googleBackendServiceId;

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setRestFilters("ssl, noSessionCreation, cors")
                .setAuthcFilter("jwtAuthc")
                .setAuthzFilter("authz");

        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
    }

    @Override
    public Map<String, Filter> getFilters() {

        Map<String, Filter> filters = super.getFilters();
        filters.put("jwtAuthc", new GoogleIapJwtAuthFilter(authorizer, defaultRoles, googleCloudProjectId, googleBackendServiceId));
        return filters;
    }

    @Override
    public Set<Realm> getRealms() {
        Set<Realm> realms = super.getRealms();
        realms.add(new JwtAuthRealm(this.authorizer));
        return realms;
    }
}
