package io.buji.pac4j.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.jee.context.JEEFrameworkParameters;

import java.io.IOException;

/**
 * Security filter for pac4j 6.x / buji-pac4j 9.x integration
 * Protects resources by requiring authentication via configured clients
 */
public class SecurityFilter implements Filter {
    private String clients;
    private String authorizers;
    private Config config;
    private SecurityLogic securityLogic;
    
    public SecurityFilter() {
        this.securityLogic = new DefaultSecurityLogic();
    }
    
    public void setClients(String clients) {
        this.clients = clients;
    }
    
    public void setAuthorizers(String authorizers) {
        this.authorizers = authorizers;
    }
    
    public void setConfig(Config config) {
        this.config = config;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        JEEFrameworkParameters parameters = new JEEFrameworkParameters(httpRequest, httpResponse);
        
        // Execute pac4j 6.x security logic
        securityLogic.perform(
            config,
            (webContext, sessionStore, profiles) -> {
                // On success, continue the filter chain
                try {
                    chain.doFilter(request, response);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            },
            clients,
            authorizers,
            null, // matchers
            parameters
        );
    }
}
