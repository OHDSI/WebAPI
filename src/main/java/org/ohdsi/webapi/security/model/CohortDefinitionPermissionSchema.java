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

  private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
        put("cohortdefinition:get", "Get list of Cohort Definitions");
	put("cohortdefinition:%s:get", "Get Cohort Definition by ID");     
    }
    };
  
    public CohortDefinitionPermissionSchema() {

        super(EntityType.COHORT_DEFINITION, readPermissions, writePermissions);
    }
}
