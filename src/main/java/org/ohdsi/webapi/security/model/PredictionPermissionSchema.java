package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PredictionPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("prediction:%s:put", "Edit Estimation with ID=%s");
        put("prediction:%s:delete", "Delete Estimation with ID=%s");
    }};

  private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
        put("prediction:get", "view prediction");                                                      
        put("prediction:prediction:generation:*:result:get", "Resolve prediction %s expression");
    }
    };
  
    public PredictionPermissionSchema() {

        super(EntityType.PREDICTION, readPermissions, writePermissions);
    }
}
