package org.ohdsi.webapi.shiro.filters;

import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

@Component
public class CacheFilter implements Filter {

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private PermissionService permissionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        permissionManager.clearAuthorizationInfoCache();
        permissionService.clearPermissionCache();
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
