package org.ohdsi.webapi.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Servlet filter that provides HTTP ETag support for endpoints annotated with {@link UseEtag}.
 * <p>
 * This filter:
 * <ul>
 *   <li>Looks up the handler method for the request</li>
 *   <li>If annotated with {@code @UseEtag}, wraps the response to capture the body</li>
 *   <li>After the response is written, computes an ETag from the body content</li>
 *   <li>If the client's {@code If-None-Match} header matches, returns 304 Not Modified</li>
 *   <li>Otherwise, adds the {@code ETag} header to the response</li>
 * </ul>
 * </p>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class EtagFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(EtagFilter.class);

    private final RequestMappingHandlerMapping handlerMapping;

    public EtagFilter(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if the handler method is annotated with @UseEtag
        if (!hasUseEtagAnnotation(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap response to capture the body
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        // Execute the filter chain (controller writes to wrapped response)
        chain.doFilter(request, wrappedResponse);

        // Process ETag after response is written
        processEtag(httpRequest, wrappedResponse);
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

    private void processEtag(HttpServletRequest request, ContentCachingResponseWrapper response)
            throws IOException {

        byte[] content = response.getContentAsByteArray();

        // Only process ETag for successful responses with content
        int status = response.getStatus();
        if (!isSuccessStatus(status) || content.length == 0) {
            response.copyBodyToResponse();
            return;
        }

        String etag = EtagUtil.generateEtag(content);
        if (etag == null) {
            response.copyBodyToResponse();
            return;
        }

        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);

        // Override Spring Security's default "no-store" with ETag-compatible caching
        // "private, max-age=0, must-revalidate" forces browser to cache but always revalidate
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=0, must-revalidate");
        response.setHeader(HttpHeaders.ETAG, etag);
        // Expose ETag header for CORS requests so browser/JS can access it
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.ETAG);

        if (EtagUtil.matches(ifNoneMatch, etag)) {
            // Client has current version - return 304 without body
            response.resetBuffer();
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            response.flushBuffer();
        } else {
            // Return full response with ETag header
            response.copyBodyToResponse();
        }
    }

    private boolean isSuccessStatus(int status) {
        return status >= 200 && status < 300;
    }
}
