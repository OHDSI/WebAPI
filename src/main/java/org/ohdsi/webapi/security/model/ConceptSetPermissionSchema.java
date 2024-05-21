package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptSetPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("conceptset:%s:put", "Update Concept Set with ID = %s");
        put("conceptset:%s:items:put", "Update Items of Concept Set with ID = %s");
        put("conceptset:%s:delete", "Delete Concept Set with ID = %s");
    }};

    private static Map<String, String> readPermissions = new HashMap<String, String>() {{
        put("conceptset:%s:get", "view conceptset  definition with id %s");
        put("conceptset:%s:expression:get", "Resolve concept set %s expression");
        put("conceptset:%s:version:*:expression:get", "Get expression for concept set %s items for default source");
    }};

    public ConceptSetPermissionSchema() {

        super(EntityType.CONCEPT_SET, readPermissions, writePermissions);
    }
}
