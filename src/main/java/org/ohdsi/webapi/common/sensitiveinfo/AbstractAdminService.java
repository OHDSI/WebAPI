package org.ohdsi.webapi.common.sensitiveinfo;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.security.authz.Role;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;


public abstract class AbstractAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAdminService.class);

    @Value("${sensitiveinfo.admin.role}")
    private String adminRole;

    @Value("${sensitiveinfo.moderator.role}")
    private String moderatorRole;

    @Autowired
    private AuthorizationService permissionManager;

    protected boolean isAdmin() {
        return isInRole(this.adminRole);
    }

    protected boolean isModerator() {
        return isInRole(this.moderatorRole);
    }

    private boolean isInRole(final String role) {
        try {
            WebApiPrincipal prinicipal = permissionManager.getAuthenticatedPrincipal();
            if (Objects.nonNull(prinicipal)) {
                java.util.List<Role> roles = permissionManager.getUserRoles(prinicipal.getUserId());
                return roles.stream().anyMatch(r -> Objects.nonNull(r.name()) && r.name().equalsIgnoreCase(role));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check rights, fallback to regular", e);
        }
        return false;
    }
}
