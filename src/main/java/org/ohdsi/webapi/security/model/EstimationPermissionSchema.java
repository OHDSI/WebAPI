package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EstimationPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("estimation:%s:put", "Edit Estimation with ID=%s");
        put("estimation:%s:delete", "Delete Estimation with ID=%s");
    }};

    private static Map<String, String> readPermissions = new HashMap<String, String>() {{ 
        put("estimation:get", "Get Estimation list");
	put("estimation:*:get", "Get Estimation instance");
	put("estimation:*:generation:get", "View Estimation Generations");
	put("estimation:generation:*:result:get", "View Estimation Generation Results");
      }
      };
  
    public EstimationPermissionSchema() {
        super(EntityType.ESTIMATION, readPermissions, writePermissions);
    }
}
