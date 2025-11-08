package io.buji.pac4j.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Temporary stub for SecurityFilter (removed in buji-pac4j 9+)
 * TODO: Refactor OAuth/SAML to use pac4j 6.x architecture
 */
public class SecurityFilter implements Filter {
    private String clients;
    private String authorizers;
    
    public void setClients(String clients) {
        this.clients = clients;
    }
    
    public void setAuthorizers(String authorizers) {
        this.authorizers = authorizers;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        // Stub implementation - OAuth/SAML disabled pending refactor
        chain.doFilter(request, response);
    }
}
