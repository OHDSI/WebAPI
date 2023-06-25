package org.ohdsi.webapi.security.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReusablePermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("reusable:%s:delete", "Delete reusable");
        put("reusable:%s:put", "Update reusable");
    }};

  private static Map<String, String> readPermissions = new HashMap<String, String>() {{                                     
        put("reusable:%s:get", "view reusable with id %s");                                                      
        put("reusable:%s:expression:get", "Resolve reusable %s expression");                                            
        put("reusable:%s:version:*:get", "Get expression for reusable %s items for default source");
		}
	};
  
    public ReusablePermissionSchema() {

        super(EntityType.REUSABLE, new HashMap<>(), writePermissions);
    }
}
