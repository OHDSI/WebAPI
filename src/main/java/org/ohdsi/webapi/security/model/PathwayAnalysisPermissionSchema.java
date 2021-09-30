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

    public PathwayAnalysisPermissionSchema() {

        super(EntityType.PATHWAY_ANALYSIS, new HashMap<>(), writePermissions);
    }
}
