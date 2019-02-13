package org.ohdsi.webapi.shiro.management;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterChainBuilder {

    private Map<String, String> filterChain = new LinkedHashMap<>();
    private String restFilters;
    private String authcFilter;
    private String authzFilter;
    private String filtersBeforeOAuth;
    private String filtersAfterOAuth;

    public FilterChainBuilder setRestFilters(FilterTemplates... restFilters) {
        this.restFilters = convertArrayToString(restFilters);
        return this;
    }

    public FilterChainBuilder setBeforeOAuthFilters(FilterTemplates... filtersBeforeOAuth) {
        this.filtersBeforeOAuth = convertArrayToString(filtersBeforeOAuth);
        return this;
    }

    public FilterChainBuilder setAfterOAuthFilters(FilterTemplates... filtersAfterOAuth) {
        this.filtersAfterOAuth = convertArrayToString(filtersAfterOAuth);
        return this;
    }

    public FilterChainBuilder setAuthcFilter(FilterTemplates... authcFilters) {
        this.authcFilter = convertArrayToString(authcFilters);
        return this;
    }

    public FilterChainBuilder setAuthzFilter(FilterTemplates... authzFilters) {
        this.authzFilter = convertArrayToString(authzFilters);
        return this;
    }

    public FilterChainBuilder addRestPath(String path, String filters) {
        return this.addPath(path, this.restFilters + ", " + filters);
    }

    public FilterChainBuilder addRestPath(String path, FilterTemplates... filters) {
        return addRestPath(path, convertArrayToString(filters));
    }

    public FilterChainBuilder addRestPath(String path) {
        return this.addPath(path, this.restFilters);
    }

    public FilterChainBuilder addOAuthPath(String path, FilterTemplates... oauthFilters) {
        return this.addPath(path, filtersBeforeOAuth + ", " + convertArrayToString(oauthFilters) + ", " + filtersAfterOAuth);
    }

    public FilterChainBuilder addProtectedRestPath(String path) {
        return this.addRestPath(path, this.authcFilter + ", " + this.authzFilter);
    }

    public FilterChainBuilder addProtectedRestPath(String path, FilterTemplates... filters) {

        String filtersStr = convertArrayToString(filters);
        return this.addRestPath(path, authcFilter + ", " + authzFilter + ", " + filtersStr);
    }

    public FilterChainBuilder addPath(String path, FilterTemplates... filters) {
        return addPath(path, convertArrayToString(filters));
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

    private String convertArrayToString(FilterTemplates... templates){
        return Arrays.stream(templates)
                .map(FilterTemplates::getTemplateName)
                .collect(Collectors.joining(", "));
    }
}
