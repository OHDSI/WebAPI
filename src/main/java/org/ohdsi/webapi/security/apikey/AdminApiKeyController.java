package org.ohdsi.webapi.security.apikey;

import org.ohdsi.webapi.audittrail.AuditTrailService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administrator access to every user's API keys, guarded by {@code admin:apikeys}.
 *
 * <pre>
 *   GET    /admin/apikeys                   – list all keys with their owners (no secrets/hashes)
 *   DELETE /admin/apikeys/{keyIdentifier}   – revoke (disable) any key; {@code ?remove} deletes it
 * </pre>
 */
@RestController
@RequestMapping("/admin/apikeys")
public class AdminApiKeyController {

    private final ApiKeyService apiKeyService;
    private final AuthorizationService authorizationService;
    private final AuditTrailService auditTrailService;

    public AdminApiKeyController(ApiKeyService apiKeyService, AuthorizationService authorizationService,
            AuditTrailService auditTrailService) {
        this.apiKeyService = apiKeyService;
        this.authorizationService = authorizationService;
        this.auditTrailService = auditTrailService;
    }

    @GetMapping
    @PreAuthorize("isPermitted('admin:apikeys')")
    public List<ApiKeyService.AdminApiKeyInfo> list() {
        return apiKeyService.listAll();
    }

    @DeleteMapping("/{keyIdentifier}")
    @PreAuthorize("isPermitted('admin:apikeys')")
    public ResponseEntity<Void> revoke(
            @PathVariable String keyIdentifier,
            @RequestParam(name = "remove", required = false) String remove) {
        String adminLogin = authorizationService.getCurrentUser().login();
        try {
            if (remove != null) {
                String owner = apiKeyService.deleteAny(keyIdentifier, adminLogin);
                auditTrailService.logApiKeyDeletedByAdmin(adminLogin, keyIdentifier, owner);
            } else {
                String owner = apiKeyService.revokeAny(keyIdentifier, adminLogin);
                auditTrailService.logApiKeyRevokedByAdmin(adminLogin, keyIdentifier, owner);
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
