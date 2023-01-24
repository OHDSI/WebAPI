package org.ohdsi.webapi.common.sensitiveinfo;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAdminService.class);

    @Value("${sensitiveinfo.admin.role}")
    private String adminRole;

    @Value("${sensitiveinfo.moderator.role}")
    private String moderatorRole;

    @Value("${security.provider}")
    private String securityProvider;

    @Autowired
    private PermissionManager permissionManager;

    protected boolean isSecured() {
        return !Constants.SecurityProviders.DISABLED.equals(securityProvider);
    }

    protected boolean isAdmin() {
        return isInRole(this.adminRole);
    }

    protected boolean isModerator() {
        return isInRole(this.moderatorRole);
    }

    private boolean isInRole(final String role) {
        if (!isSecured()) {
            return true;
        }
        try {
            UserEntity currentUser = permissionManager.getCurrentUser();
            if (Objects.nonNull(currentUser)) {
                Set<RoleEntity> roles = permissionManager.getUserRoles(currentUser.getId());
                return roles.stream().anyMatch(r -> Objects.nonNull(r.getName()) && r.getName().equalsIgnoreCase(role));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to check rights, fallback to regular", e);
        }
        return false;
    }
}
