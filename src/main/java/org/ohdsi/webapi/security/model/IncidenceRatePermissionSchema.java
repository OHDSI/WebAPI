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
        put("ir:%s:get", "view list of incident rates");
        put("ir:%s:version:get", "Get list of IR analsis versions");
	put("ir:%s:version:*:get", "Get IR analysis version");
	put("ir:%s:copy:get","Copy incidence rate");
	put("ir:%s:info:get","Get IR info");
	put("ir:%s:design:get","Export Incidence Rates design");
    }
    };
  
    public IncidenceRatePermissionSchema() {

        super(EntityType.INCIDENCE_RATE, readPermissions, writePermissions);
    }
}
