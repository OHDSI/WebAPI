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
        put("pathway-analysis:%s:get", "view pathway analysis with id %s");
	put("pathway-analysis:get", "view pathway analysis");
        put("pathway-analysis:%s:expression:get", "Resolve pathway analysis %s expression");
	put("pathway-analysis:version:*:expression:get", "Get list of pathway analysis versions");	
	put("pathway-analysis:%s:version:*:expression:get", "Get expression for pathway analysis %s items for default source");
	put("pathway-analysis:*:generation:get", "");
	put("pathway-analysis:generation:*:get", "");
	put("pathway-analysis:generation:*:get", "");
	put("pathway-analysis:generation:*:design:get", "");
	put("pathway-analysis:*:export:get", "");
	put("pathway-analysis:*:exists:get", "");

	    }
	};  
  
    public PathwayAnalysisPermissionSchema() {

        super(EntityType.PATHWAY_ANALYSIS, new HashMap<>(), writePermissions);
    }
}
