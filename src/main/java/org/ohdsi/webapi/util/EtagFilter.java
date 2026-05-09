package org.ohdsi.webapi.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * Servlet filter that provides HTTP ETag support for endpoints annotated with {@link UseEtag}.
 * <p>
 * Extends Spring's {@link ShallowEtagHeaderFilter} to leverage its ETag generation and 
 * 304 Not Modified handling, while adding selective filtering based on the {@code @UseEtag} 
 * annotation.
 * </p>
 * <p>
 * This filter:
 * <ul>
 *   <li>Skips ETag processing for methods not annotated with {@code @UseEtag}</li>
 *   <li>Sets appropriate Cache-Control headers for browser caching with revalidation</li>
 *   <li>Exposes ETag header for CORS requests</li>
 *   <li>Generates weak ETags (W/"...") per RFC 7232</li>
 * </ul>
 * </p>
 */
@Component
public class EtagFilter extends ShallowEtagHeaderFilter {

    private static final Logger LOG = LoggerFactory.getLogger(EtagFilter.class);

    private final RequestMappingHandlerMapping handlerMapping;

    public EtagFilter(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
        setWriteWeakETag(true);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !hasUseEtagAnnotation(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // Override Spring Security's default "no-store" with ETag-compatible caching
        // "private, max-age=0, must-revalidate" forces browser to cache but always revalidate
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=0, must-revalidate");
        // Expose ETag header for CORS requests so browser/JS can access it
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.ETAG);
        
        super.doFilterInternal(request, response, filterChain);
    }

    private boolean hasUseEtagAnnotation(HttpServletRequest request) {
        try {
            HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
            if (handlerChain == null) {
                return false;
            }

            Object handler = handlerChain.getHandler();
            if (handler instanceof HandlerMethod handlerMethod) {
                return handlerMethod.hasMethodAnnotation(UseEtag.class);
            }
        } catch (Exception e) {
            LOG.debug("Failed to look up handler for ETag check: {}", e.getMessage());
        }
        return false;
    }
}
