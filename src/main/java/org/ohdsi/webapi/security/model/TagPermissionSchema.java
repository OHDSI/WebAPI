package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TagPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("tag:%s:delete", "Delete tag");
        put("tag:%s:put", "Update tag");
    }};

    public TagPermissionSchema() {

        super(EntityType.TAG, new HashMap<>(), writePermissions);
    }
}
