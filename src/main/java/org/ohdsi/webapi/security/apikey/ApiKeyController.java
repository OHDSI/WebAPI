package org.ohdsi.webapi.security.apikey;

import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * REST endpoints for managing personal API keys.
 *
 * <p>All endpoints require an authenticated principal (interactive session or an
 * existing API key).  The raw key is returned exactly once, at creation time.
 *
 * <pre>
 *   POST   /user/apikeys        – generate a new key
 *   GET    /user/apikeys        – list keys for the current user (no secrets/hashes)
 *   DELETE /user/apikeys/{id}   – revoke (disable) a key
 * </pre>
 */
@RestController
@RequestMapping("/user/apikeys")
public class ApiKeyController {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyController.class);

    private final ApiKeyService apiKeyService;
    private final AuthorizationService authorizationService;

    public ApiKeyController(ApiKeyService apiKeyService, AuthorizationService authorizationService) {
        this.apiKeyService = apiKeyService;
        this.authorizationService = authorizationService;
    }

    /**
     * Request body for creating an API key.
     *
     * @param name          short label for the key (required)
     * @param description   optional longer description
     * @param expiresInDays number of days until the key expires; null or 0 means the key never expires
     */
    public record CreateRequest(String name, String description, Integer expiresInDays) {
    }

    /**
     * Generate a new API key for the currently authenticated user.
     *
     * <p>The response includes the full plaintext key in {@code rawKey}.
     * This is the only time it will ever be returned — store it securely.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiKeyService.ApiKeyResult> create(@RequestBody CreateRequest request) {

        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String login = authorizationService.getCurrentUser().login();
        Instant expiresAt = (request.expiresInDays() != null && request.expiresInDays() > 0)
                ? Instant.now().plus(request.expiresInDays(), ChronoUnit.DAYS)
                : null;
        ApiKeyService.ApiKeyResult result = apiKeyService.generate(
                login, request.name(), request.description(), expiresAt);

        log.info("API key created via controller: identifier={}, user={}", result.keyIdentifier(), login);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * List all API keys for the currently authenticated user.
     * Key hashes and secrets are never included in the response.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ApiKeyService.ApiKeyInfo> list() {
        String login = authorizationService.getCurrentUser().login();
        return apiKeyService.list(login);
    }

    /**
     * Revoke or permanently delete an API key by its public identifier.
     * <ul>
     *   <li>Without {@code ?remove}: soft-disables the key (visible in list, cannot authenticate).</li>
     *   <li>With {@code ?remove}: permanently removes the record from the database.</li>
     * </ul>
     * Returns 404 if the key does not exist or does not belong to the caller.
     */
    @DeleteMapping("/{keyIdentifier}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> revoke(
            @PathVariable String keyIdentifier,
            @RequestParam(name = "remove", required = false) String remove) {
        String login = authorizationService.getCurrentUser().login();
        try {
            if (remove != null) {
                apiKeyService.delete(keyIdentifier, login);
            } else {
                apiKeyService.revoke(keyIdentifier, login);
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.debug("Revoke rejected for key identifier={}, user={}: {}", keyIdentifier, login, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
