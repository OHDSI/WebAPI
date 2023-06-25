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
        put("estimation:get", "get list of estimations");
	put("estimation:%s:get", "get estimation with id %s");	
      }
      };
  
    public EstimationPermissionSchema() {

        super(EntityType.ESTIMATION, new HashMap<>(), writePermissions);
    }
}
