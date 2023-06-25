package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IncidenceRatePermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("ir:%s:get", "Read Incidence Rate with ID=%s"); // TODO
        put("ir:%s:export:get", "Export Incidence Rate with ID=%s");
        put("ir:%s:put", "Edit Incidence Rate with ID=%s");
        put("ir:%s:delete", "Delete Incidence Rate with ID=%s");
    }};

  private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
        put("ir:*:get", "view list of incident rates");
	put("ir:get", "view list of incident rates");                                                      
        put("ir:*:version:get", "Get list of IR analsis versions");
	put("ir:*:version:*:get", "Get IR analysis version");
    }
    };
  
    public IncidenceRatePermissionSchema() {

        super(EntityType.INCIDENCE_RATE, new HashMap<>(), writePermissions);
    }
}
