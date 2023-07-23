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
	put("estimation:%s:get", "Get Estimation instance");
	put("estimation:%s:generation:get", "View Estimation Generations");
	put("estimation:%s:copy:get", "Copy Estimation instance");
	put("estimation:%s:download:get", "Download Estimation package");
	put("estimation:%s:export:get", "Export Estimation");
	put("estimation:%s:generation:get", "View Estimation Generations");
	put("comparativecohortanalysis:%s:get","Get estimation");
      }
      };

  
    public EstimationPermissionSchema() {
        super(EntityType.ESTIMATION, readPermissions, writePermissions);
    }
}
