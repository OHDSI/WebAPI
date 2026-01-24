package org.ohdsi.webapi.shiro.filters;

import io.buji.pac4j.filter.CallbackFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.ohdsi.webapi.shiro.ServletBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Custom callback filter that intercepts redirects and ensures
 * successful authentication always redirects to the Atlas UI,
 * not to the originally saved request URL.
 */
public class AtlasCallbackFilter extends CallbackFilter {

    private static final Logger logger = LoggerFactory.getLogger(AtlasCallbackFilter.class);
    private String atlasRedirectUrl;

    public void setAtlasRedirectUrl(String atlasRedirectUrl) {
        this.atlasRedirectUrl = atlasRedirectUrl;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = ServletBridge.toHttp(servletRequest);
        HttpServletResponse response = ServletBridge.toHttp(servletResponse);

        // Use WARN level to ensure it appears in logs
        logger.warn("AtlasCallbackFilter.doFilter ENTERED for URI: {}", request.getRequestURI());
        System.out.println("AtlasCallbackFilter.doFilter ENTERED for URI: " + request.getRequestURI());

        // Wrap the response to intercept redirects (via sendRedirect OR setHeader/setStatus)
        RedirectCapturingResponseWrapper responseWrapper = new RedirectCapturingResponseWrapper(response);

        // Execute the parent callback filter with the wrapped response
        super.doFilter(request, responseWrapper, filterChain);

        logger.warn("AtlasCallbackFilter: After parent filter - redirectLocation='{}', status={}, isRedirect={}",
                   responseWrapper.getRedirectLocation(), responseWrapper.getCapturedStatus(), responseWrapper.isRedirect());
        System.out.println("AtlasCallbackFilter: After parent filter - redirectLocation=" + responseWrapper.getRedirectLocation());

        // If a redirect was captured (either via sendRedirect or setHeader/setStatus), override it
        if (responseWrapper.getRedirectLocation() != null) {
            String capturedRedirect = responseWrapper.getRedirectLocation();
            logger.warn("AtlasCallbackFilter: Intercepted redirect to '{}', atlasRedirectUrl='{}'",
                       capturedRedirect, atlasRedirectUrl);

            // Only override if it's not already pointing to Atlas and we have a configured URL
            if (atlasRedirectUrl != null && !capturedRedirect.contains("/atlas/")) {
                logger.warn("AtlasCallbackFilter: Overriding redirect to Atlas UI: {}", atlasRedirectUrl);
                response.sendRedirect(atlasRedirectUrl);
            } else {
                // Use the original redirect
                logger.warn("AtlasCallbackFilter: Using original redirect: {}", capturedRedirect);
                response.sendRedirect(capturedRedirect);
            }
        } else {
            logger.warn("AtlasCallbackFilter: No redirect captured, response committed={}", response.isCommitted());
            System.out.println("AtlasCallbackFilter: No redirect captured, response committed=" + response.isCommitted());
        }
    }

    /**
     * Wrapper that captures redirect calls instead of executing them.
     * Intercepts both sendRedirect() and setHeader("Location", ...) + setStatus(302)
     * since pac4j uses the latter approach via JEEHttpActionAdapter.
     */
    private static class RedirectCapturingResponseWrapper extends HttpServletResponseWrapper {
        private String redirectLocation;
        private int statusCode = 200;
        private final HttpServletResponse originalResponse;

        public RedirectCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
            this.originalResponse = response;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            // Don't actually redirect, just capture the location
            logger.warn("RedirectCapturingResponseWrapper: sendRedirect called with '{}'", location);
            System.out.println("RedirectCapturingResponseWrapper: sendRedirect called with '" + location + "'");
            this.redirectLocation = location;
            this.statusCode = 302;
        }

        @Override
        public void setStatus(int sc) {
            logger.warn("RedirectCapturingResponseWrapper: setStatus called with {}", sc);
            System.out.println("RedirectCapturingResponseWrapper: setStatus called with " + sc);
            this.statusCode = sc;
            // Don't pass through redirect status codes
            if (sc != 302 && sc != 301 && sc != 303 && sc != 307 && sc != 308) {
                super.setStatus(sc);
            }
        }

        @Override
        public void setHeader(String name, String value) {
            logger.warn("RedirectCapturingResponseWrapper: setHeader called with '{}' = '{}'", name, value);
            System.out.println("RedirectCapturingResponseWrapper: setHeader called with '" + name + "' = '" + value + "'");
            if ("Location".equalsIgnoreCase(name)) {
                this.redirectLocation = value;
            } else {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            logger.warn("RedirectCapturingResponseWrapper: addHeader called with '{}' = '{}'", name, value);
            System.out.println("RedirectCapturingResponseWrapper: addHeader called with '" + name + "' = '" + value + "'");
            if ("Location".equalsIgnoreCase(name)) {
                this.redirectLocation = value;
            } else {
                super.addHeader(name, value);
            }
        }

        public String getRedirectLocation() {
            return redirectLocation;
        }

        public int getCapturedStatus() {
            return statusCode;
        }

        public boolean isRedirect() {
            return redirectLocation != null && (statusCode == 302 || statusCode == 301 || statusCode == 303 || statusCode == 307 || statusCode == 308);
        }
    }
}
