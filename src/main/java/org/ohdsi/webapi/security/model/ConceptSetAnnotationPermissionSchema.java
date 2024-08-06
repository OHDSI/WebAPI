package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConceptSetAnnotationPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {
        {
            put("conceptset:annotation:%s:delete", "Delete Concept Set Annotation with ID %s");
        }
    };

    private static Map<String, String> readPermissions = new HashMap<String, String>() {
        {
        }
    };

    public ConceptSetAnnotationPermissionSchema() {
        super(EntityType.CONCEPT_SET_ANNOTATION, readPermissions, writePermissions);
    }
}
