package org.ohdsi.webapi.security.entity;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EntityPermissionSchemaResolver {

    private static Map<EntityType, EntityPermissionSchema> entityPermissionSchemas = new HashMap<>();

    public EntityPermissionSchemaResolver(List<EntityPermissionSchema> entityPermissionSchemaList) {

        entityPermissionSchemaList.forEach(s -> entityPermissionSchemas.put(s.getEntityType(), s));
    }

    public EntityPermissionSchema getForType(EntityType entityType) {

        return entityPermissionSchemas.get(entityType);
    }
}
