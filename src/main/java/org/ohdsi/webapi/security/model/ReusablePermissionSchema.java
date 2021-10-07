package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReusablePermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("reusable:*:delete", "Delete reusable");
        put("reusable:*:put", "Update reusable");
        put("reusable:post", "Create reusable");
    }};

    public ReusablePermissionSchema() {

        super(EntityType.REUSABLE, new HashMap<>(), writePermissions);
    }
}
