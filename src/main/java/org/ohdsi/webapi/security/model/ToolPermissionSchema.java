package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ToolPermissionSchema extends EntityPermissionSchema {
    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("tool:%s:delete", "Delete Tool with id = %s");
    }};
    private static Map<String, String> readPermissions = new HashMap<String, String>() {
        {
            put("tool:%s:get", "View Tool with id = %s");
        }
    };

    public ToolPermissionSchema() {
        super(EntityType.TOOL, readPermissions, writePermissions);
    }

}