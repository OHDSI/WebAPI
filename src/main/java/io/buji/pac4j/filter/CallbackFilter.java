package io.buji.pac4j.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.savedrequest.SavedRequestHandler;
import org.pac4j.jee.context.JEEFrameworkParameters;

import java.io.IOException;

/**
 * Callback filter for pac4j 6.x / buji-pac4j 9.x integration
 * Handles OAuth/SAML callback after successful authentication
 */
public class CallbackFilter implements Filter {
    private String defaultUrl = "/";
    private Config config;
    private CallbackLogic callbackLogic;

    public CallbackFilter() {
        this.callbackLogic = new DefaultCallbackLogic();
    }

    public void setDefaultUrl(String url) {
        this.defaultUrl = url;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * Set a custom SavedRequestHandler on the callback logic.
     * This allows customizing where users are redirected after authentication.
     */
    public void setSavedRequestHandler(SavedRequestHandler savedRequestHandler) {
        if (this.callbackLogic instanceof DefaultCallbackLogic) {
            ((DefaultCallbackLogic) this.callbackLogic).setSavedRequestHandler(savedRequestHandler);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        JEEFrameworkParameters parameters = new JEEFrameworkParameters(httpRequest, httpResponse);

        // Execute pac4j 6.x callback logic
        callbackLogic.perform(
            config,
            defaultUrl,
            true, // renewSession
            null, // defaultClient
            parameters
        );
        // Callback logic handles the response, don't continue chain
    }
}
