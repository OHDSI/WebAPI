package org.ohdsi.webapi.shiro.management;

import org.apache.shiro.realm.Realm;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.shiro.filters.GoogleIapJwtAuthFilter;
import org.ohdsi.webapi.shiro.realms.JwtAuthRealm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import java.util.Map;
import java.util.Set;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.AUTHZ;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CORS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.JWT_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_CACHE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SSL;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = Constants.SecurityProviders.GOOGLE)
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

    public AtlasGoogleSecurity(EntityPermissionSchemaResolver permissionSchemaResolver) {

        super(permissionSchemaResolver);
    }

    @Override
    protected FilterChainBuilder getFilterChainBuilder() {

        FilterChainBuilder filterChainBuilder = new FilterChainBuilder()
                .setRestFilters(SSL, NO_SESSION_CREATION, CORS, NO_CACHE)
                .setAuthcFilter(JWT_AUTHC)
                .setAuthzFilter(AUTHZ);

        setupProtectedPaths(filterChainBuilder);

        return filterChainBuilder.addRestPath("/**");
    }

    @Override
    public Map<FilterTemplates, Filter> getFilters() {

        Map<FilterTemplates, Filter> filters = super.getFilters();
        filters.put(JWT_AUTHC, new GoogleIapJwtAuthFilter(authorizer, defaultRoles, googleCloudProjectId, googleBackendServiceId));
        return filters;
    }

    @Override
    public Set<Realm> getRealms() {
        Set<Realm> realms = super.getRealms();
        realms.add(new JwtAuthRealm(this.authorizer));
        return realms;
    }
}
