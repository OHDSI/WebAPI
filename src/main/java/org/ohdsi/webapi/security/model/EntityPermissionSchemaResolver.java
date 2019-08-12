package org.ohdsi.webapi.security.model;

import org.ohdsi.webapi.model.CommonEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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

    public EntityPermissionSchema getForClass(Class<? extends CommonEntity> clazz) {

        return getForType(getEntityType(clazz));
    }

    public EntityType getEntityType(Class<? extends CommonEntity> clazz) {

        return Arrays.stream(EntityType.values())
            .filter(e -> e.getEntityClass().isAssignableFrom(clazz))
            .findFirst()
            .orElse(null);
    }
}
