package org.ohdsi.webapi.shiro.filters;

import org.pac4j.core.context.CallContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.redirect.OidcRedirectionActionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Custom OIDC redirection action builder that rewrites authorization URLs
 * to use an external base URL for browser-facing redirects.
 *
 * This is needed when WebAPI fetches OIDC discovery from an internal URL
 * (which returns internal endpoints) but needs to redirect users to
 * external browser-accessible URLs.
 */
public class ExternalUrlOidcRedirectionActionBuilder extends OidcRedirectionActionBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUrlOidcRedirectionActionBuilder.class);

    private final String internalBaseUrl;
    private final String externalBaseUrl;

    public ExternalUrlOidcRedirectionActionBuilder(OidcClient client,
                                                    String internalBaseUrl, String externalBaseUrl) {
        super(client);
        this.internalBaseUrl = normalizeUrl(internalBaseUrl);
        this.externalBaseUrl = normalizeUrl(externalBaseUrl);
        logger.info("ExternalUrlOidcRedirectionActionBuilder initialized: internal={}, external={}",
                    this.internalBaseUrl, this.externalBaseUrl);
    }

    private String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        // Remove .well-known suffix if present
        if (url.contains("/.well-known/")) {
            url = url.substring(0, url.indexOf("/.well-known/"));
        }
        // Remove trailing slash
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Override
    public Optional<RedirectionAction> getRedirectionAction(CallContext ctx) {
        Optional<RedirectionAction> action = super.getRedirectionAction(ctx);

        if (action.isPresent() && internalBaseUrl != null && externalBaseUrl != null
                && !internalBaseUrl.equals(externalBaseUrl)) {
            RedirectionAction originalAction = action.get();

            if (originalAction instanceof FoundAction) {
                FoundAction foundAction = (FoundAction) originalAction;
                String originalLocation = foundAction.getLocation();

                if (originalLocation != null && originalLocation.contains(internalBaseUrl)) {
                    String newLocation = originalLocation.replace(internalBaseUrl, externalBaseUrl);
                    logger.debug("Rewrote OIDC redirect URL from {} to {}", originalLocation, newLocation);
                    return Optional.of(new FoundAction(newLocation));
                }
            }
        }

        return action;
    }
}
