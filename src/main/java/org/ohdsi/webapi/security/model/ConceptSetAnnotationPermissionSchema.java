package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptSetAnnotationPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {
        {
            put("conceptset:*:annotation:put", "Create Concept Set Annotation");
            put("conceptset:update:*:annotation:put", "Update Concept Set Annotation");
            put("conceptset:annotation:%s:delete", "Delete Concept Set Annotation with ID %s");
        }
    };

    private static Map<String, String> readPermissions = new HashMap<String, String>() {
        {
            put("conceptset:%s:annotation:get", "List Concept Set Annotation with id %s");
            put("conceptset:*:annotation:get", "View Concept Set Annotation expression");
        }
    };

    public ConceptSetAnnotationPermissionSchema() {

        super(EntityType.CONCEPT_SET_ANNOTATION, readPermissions, writePermissions);
    }
}
