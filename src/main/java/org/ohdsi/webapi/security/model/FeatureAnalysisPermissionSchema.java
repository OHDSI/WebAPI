package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FeatureAnalysisPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("feature-analysis:%s:put", "Update Feature Analysis with ID = %s");
        put("feature-analysis:%s:delete", "Delete Feature Analysis with ID = %s");
    }};

    private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
	put("feature-analysis:%s:get", "get feature analysis");
        put("feature-analysis:aggregates:get", "feature-analysis:aggregates:get");                                                   
      }
      };
  
    public FeatureAnalysisPermissionSchema() {

        super(EntityType.FE_ANALYSIS, readPermissions, writePermissions);
    }
}
