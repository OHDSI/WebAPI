package io.buji.pac4j.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import java.io.IOException;

/**
 * Temporary stub for CallbackFilter (removed in buji-pac4j 9+)
 * TODO: Refactor OAuth/SAML to use pac4j 6.x architecture
 */
public class CallbackFilter implements Filter {
    private String defaultUrl;
    private Config config;
    
    public void setDefaultUrl(String url) {
        this.defaultUrl = url;
    }
    
    public void setConfig(Config config) {
        this.config = config;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // Stub implementation - OAuth/SAML disabled pending refactor
        chain.doFilter(request, response);
    }
}
