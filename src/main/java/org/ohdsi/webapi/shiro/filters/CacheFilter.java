package org.ohdsi.webapi.shiro.filters;

import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Component
public class CacheFilter implements Filter {

    @Autowired
    private PermissionManager permissionManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        permissionManager.clearAuthorizationInfoCache();
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
