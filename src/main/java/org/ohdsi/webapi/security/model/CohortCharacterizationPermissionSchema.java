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

   private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
       put("cohort-characterization:%s:get", "Get cohort characterization");                         
       put("cohort-characterization:%s:generation:get", "Get cohort characterization generations");
       put("cohort-characterization:generation:*:get", "Get cohort characterization generation");
       put("cohort-characterization:design:get", "cohort-characterization:design:get");
       put("cohort-characterization:%s:design:get", "Get cohort characterization design");                         
       put("cohort-characterization:design:%s:get", "view cohort characterization with id %s");
       put("cohort-characterization:%s:version:get", "Get list of characterization versions");                         
       put("cohort-characterization:%s:version:*:get", "Get list of characterization version");
    }};
  
    public CohortCharacterizationPermissionSchema() {

        super(EntityType.COHORT_CHARACTERIZATION, readPermissions, writePermissions);
    }
}
