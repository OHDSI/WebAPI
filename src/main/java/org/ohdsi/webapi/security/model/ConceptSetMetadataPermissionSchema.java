package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptSetMetadataPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {
        {
            put("conceptset:*:metadata:put", "Create Concept Set Metadata");
            put("conceptset:update:*:metadata:put", "Update Concept Set Metadata");
            put("conceptset:%s:metadata:delete", "Delete Concept Set Metadata with ID %s");
        }
    };

    private static Map<String, String> readPermissions = new HashMap<String, String>() {
        {
            put("conceptset:%s:metadata:get", "List Concept Set Metadata with id %s");
            put("conceptset:*:metadata:get", "View Concept Set Metadata expression");
        }
    };

    public ConceptSetMetadataPermissionSchema() {

        super(EntityType.CONCEPT_SET_METADATA, readPermissions, writePermissions);
    }
}
