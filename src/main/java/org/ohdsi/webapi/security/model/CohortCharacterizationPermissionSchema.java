package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CohortCharacterizationPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("cohort-characterization:%s:put", "Update Cohort Characterization with ID = %s");
        put("cohort-characterization:%s:delete", "Delete Cohort Characterization with ID = %s");
    }};

    public CohortCharacterizationPermissionSchema() {

        super(EntityType.COHORT_CHARACTERIZATION, new HashMap<>(), writePermissions);
    }
}
