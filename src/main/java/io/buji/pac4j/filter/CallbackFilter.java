package io.buji.pac4j.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Temporary stub for CallbackFilter (removed in buji-pac4j 9+)
 * TODO: Refactor OAuth/SAML to use pac4j 6.x architecture
 */
public class CallbackFilter implements Filter {
    private String defaultUrl;
    
    public void setDefaultUrl(String url) {
        this.defaultUrl = url;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // Stub implementation - OAuth/SAML disabled pending refactor
        chain.doFilter(request, response);
    }
}
