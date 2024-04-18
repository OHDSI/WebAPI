package org.ohdsi.webapi.security.model;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ToolPermissionSchema extends EntityPermissionSchema {
    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("tool:%s:delete", "Delete Tool with by id = %s");
        put("tool:%s:put", "Update Tool with by id = %s");
        put("tool:%s:post", "Create Tool with by id = %s");
    }};
    private static Map<String, String> readPermissions = new HashMap<String, String>() {
        {
            put("tool:%s:get", "View Tool with by id = %s");
        }
    };

    public ToolPermissionSchema() {
        super(EntityType.TOOL, readPermissions, writePermissions);
    }

}