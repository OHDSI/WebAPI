package org.ohdsi.webapi.security.model;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityPermissionSchema {

    private final EntityType entityType;
    private final Map<String, String> readPermissions;
    private final Map<String, String> writePermissions;

    @Autowired
    protected PermissionManager permissionManager;

    public EntityPermissionSchema(EntityType entityType, Map<String, String> readPermissions, Map<String, String> writePermissions) {

        this.entityType = entityType;
        this.readPermissions = readPermissions;
        this.writePermissions = Collections.unmodifiableMap(writePermissions);
    }

    public EntityType getEntityType() {

        return entityType;
    }

    public Map<String, String> getReadPermissions() {

        return readPermissions;
    }

    public Map<String, String> getWritePermissions() {

        return writePermissions;
    }

    public Map<String, String> getAllPermissions() {

        Map<String, String> permissions = new HashMap<>();
        permissions.putAll(getReadPermissions());
        permissions.putAll(getWritePermissions());
        return permissions;
    }

    public void onInsert(CommonEntity commonEntity) {

        addPermissionsToCurrentUserFromTemplate(commonEntity, getAllPermissions());
    }

    public void onDelete(CommonEntity commonEntity) {

        Map<String, String> permissionTemplates = getAllPermissions();
        permissionManager.removePermissionsFromTemplate(permissionTemplates, commonEntity.getId().toString());
    }

    protected void addPermissionsToCurrentUserFromTemplate(CommonEntity commonEntity, Map<String, String> template) {

        String login = permissionManager.getSubjectName();
        RoleEntity role = permissionManager.getUserPersonalRole(login);
        permissionManager.addPermissionsFromTemplate(role, template, commonEntity.getId().toString());
    }
}
