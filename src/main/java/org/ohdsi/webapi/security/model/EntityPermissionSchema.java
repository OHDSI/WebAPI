package org.ohdsi.webapi.security.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class EntityPermissionSchema {

    private final EntityType entityType;
    private final Map<String, String> readPermissions;
    private final Map<String, String> writePermissions;

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
}
