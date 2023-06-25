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
       put("cohort-characterization:get", "view cohort characterization list");                         
       put("cohort-characterization:%s:get", "view cohort characterization with id %s");
       put("cohort-characterization:generation:get", "view cohort characterization generation list");                         
       put("cohort-characterization:generation:%s:get", "view cohort characterization geneartion with id %s");
       put("cohort-characterization:design:get", "view cohort characterization design");                         
       put("cohort-characterization:design:%s:get", "view cohort characterization with id %s");
    }};
  
    public CohortCharacterizationPermissionSchema() {

        super(EntityType.COHORT_CHARACTERIZATION, new HashMap<>(), writePermissions);
    }
}
