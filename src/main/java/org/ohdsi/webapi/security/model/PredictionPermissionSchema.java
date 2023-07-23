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
        put("prediction:%s:get", "Get Prediction instance");
	put("prediction:%s:copy:get", "Copy Prediction instance");
	put("prediction:%s:download:get", "Download Prediction package");
	put("prediction:%s:export:get", "Export Prediction");
	put("prediction:%s:generation:get", "View Prediction Generations");
	put("prediction:%s:exists:get", "Check name uniqueness of prediction");
	put("plp:%s:get", "Get population level prediction");
    }
    };
    
    public PredictionPermissionSchema() {

        super(EntityType.PREDICTION, readPermissions, writePermissions);
    }
}
