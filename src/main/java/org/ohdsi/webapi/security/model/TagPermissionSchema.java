package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TagPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("tag:*:delete", "Delete tag");
        put("tag:*:get", "Get tag");
        put("tag:*:put", "Update tag");
        put("tag:post", "Create tag");
        put("tag:search:get", "Search tags by name");
        put("tag:get", "List tags");
    }};

    public TagPermissionSchema() {

        super(EntityType.TAG, new HashMap<>(), writePermissions);
    }
}
