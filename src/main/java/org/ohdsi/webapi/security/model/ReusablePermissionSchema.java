package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReusablePermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("reusable:%s:delete", "Delete reusable");
        put("reusable:%s:put", "Update reusable");
    }};

    public ReusablePermissionSchema() {

        super(EntityType.REUSABLE, new HashMap<>(), writePermissions);
    }
}
