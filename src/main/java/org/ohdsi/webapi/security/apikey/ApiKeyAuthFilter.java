package org.ohdsi.webapi.security.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.ohdsi.webapi.security.authc.WebApiAuthenticationToken;
import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Servlet filter that authenticates requests carrying an {@code X-API-KEY} header.
 *
 * <p>The filter runs before Spring Security's {@code BearerTokenAuthenticationFilter}.
 * If the header is absent the request is passed down the chain unchanged, allowing
 * normal JWT authentication to proceed.  If the header is present but the key is
 * invalid, a 401 response is returned immediately.
 *
 * <p>Successful API key authentication sets a {@link WebApiAuthenticationToken} with
 * a {@code null} session ID in the {@link SecurityContextHolder}, matching the
 * stateless intent of API key access.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-KEY";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            // No API key header — let the JWT filter handle the request normally.
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> userOpt = apiKeyService.validate(apiKey);
        if (userOpt.isEmpty()) {
            log.debug("Rejected invalid API key from {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid or expired API key\"}");
            return;
        }

        WebApiPrincipal principal = new WebApiPrincipal(userOpt.get());
        // Authorities are empty, consistent with the existing JWT converter behaviour.
        // Per-endpoint @PreAuthorize rules evaluate permissions via the AuthorizationService.
        Collection<GrantedAuthority> authorities = List.of();
        // sessionId is null: API key requests are stateless and carry no session.
        WebApiAuthenticationToken auth = WebApiAuthenticationToken.authenticated(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
