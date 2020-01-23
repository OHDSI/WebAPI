package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CohortSamplePermissionSchema extends EntityPermissionSchema {
    private static Map<String, String> readPermissions = new HashMap<String, String>() {{
        put("cohortsample:*:*:%s:get", "Get Cohort Sample with ID = %s");
    }};

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("cohortsample:*:*:%s:delete", "Delete Cohort Sample with ID = %s");
    }};

    public CohortSamplePermissionSchema() {
        super(EntityType.COHORT_SAMPLE, readPermissions, writePermissions);
    }
}
