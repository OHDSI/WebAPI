package org.ohdsi.webapi.service;

import java.io.IOException;;
import java.util.List;
import org.ohdsi.webapi.events.EntityName;

public interface EntitiesContainer {
    
    List<String> getNestedEntitiesIds(String content, EntityName entityName) throws IOException;
}
