package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PathwayAnalysisPermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("pathway-analysis:%s:put", "Update Pathway Analysis with ID = %s");
        put("pathway-analysis:%s:sql:*:get", "Get analysis sql for Pathway Analysis with ID = %s");
        put("pathway-analysis:%s:delete", "Delete Pathway Analysis with ID = %s");
    }};

  private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
	put("pathway-analysis:%s:get", "Get Pathways Analysis instance");
	put("pathway-analysis:%s:generation:get", "Get Pathways Analysis generations list");
	put("pathway-analysis:generation:*:get", "Get Pathways Analysis generation instance");
	put("pathway-analysis:generation:*:result:get", "Get Pathways Analysis generation results");
	put("pathway-analysis:generation:*:design:get", "Get Pathways Analysis generation design");
	put("pathway-analysis:%s:version:get", "Get list of pathway analysis versions");
	put("pathway-analysis:%s:version:*:get", "Get pathway analysis version");
      }
    };  
  
    public PathwayAnalysisPermissionSchema() {

      super(EntityType.PATHWAY_ANALYSIS, readPermissions, writePermissions);
    }
}
