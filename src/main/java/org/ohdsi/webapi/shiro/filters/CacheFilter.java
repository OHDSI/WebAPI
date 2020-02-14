package org.ohdsi.webapi.shiro.filters;

import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class CacheFilter implements Filter {

    private final List<String> skippedPaths = Arrays.asList("/actuator/health/liveness", "/actuator/health/readyness");

    @Autowired
    private PermissionManager permissionManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final String path = httpServletRequest.getPathInfo();
        if(!skippedPaths.contains(path))
            permissionManager.clearAuthorizationInfoCache();
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
