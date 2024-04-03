package org.ohdsi.webapi.security.model;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
@Component
public class ToolPermissionSchema extends EntityPermissionSchema {
    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("tool:*:delete", "Delete tool");
        put("tool:*:put", "Update tool");
        put("tool:*:post", "Edit tool");
    }};
    private static Map<String, String> readPermissions = new HashMap<String, String>() {{
        put("tool:get", "view tool with id %s");
        put("tool:*:get", "Resolve tool %s expression");
    }
    };
    public ToolPermissionSchema() {
        super(EntityType.TOOL, new HashMap<>(), writePermissions);
    }
}