package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CohortDefinitionPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
        put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");
        put("cohortdefinition:%s:check:post", "Fix Cohort Definition with ID = %s");
    }};

    public CohortDefinitionPermissionSchema() {

        super(EntityType.COHORT_DEFINITION, new HashMap<>(), writePermissions);
    }
}
