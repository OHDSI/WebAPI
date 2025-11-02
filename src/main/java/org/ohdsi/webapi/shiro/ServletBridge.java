package org.ohdsi.webapi.shiro;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Bridge utility for Shiro 2.0 jakarta.servlet compatibility.
 * Shiro 2.0 has incomplete jakarta migration - ServletBridge.toHttp() still expects javax.servlet
 * This bridge provides safe casting without WebUtils dependency.
 */
public class ServletBridge {
    
    /**
     * Cast ServletRequest to HttpServletRequest.
     * Replacement for ServletBridge.toHttp(ServletRequest)
     */
    public static HttpServletRequest toHttp(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            return (HttpServletRequest) request;
        }
        throw new IllegalArgumentException("Request must be an instance of HttpServletRequest");
    }
    
    /**
     * Cast ServletResponse to HttpServletResponse.
     * Replacement for ServletBridge.toHttp(ServletResponse)
     */
    public static HttpServletResponse toHttp(ServletResponse response) {
        if (response instanceof HttpServletResponse) {
            return (HttpServletResponse) response;
        }
        throw new IllegalArgumentException("Response must be an instance of HttpServletResponse");
    }
}
