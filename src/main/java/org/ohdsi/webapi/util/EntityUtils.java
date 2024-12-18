package org.ohdsi.webapi.util;

import com.cosium.spring.data.jpa.entity.graph.domain2.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;

public class EntityUtils {

    private EntityUtils() {

    }
    
    public static EntityGraph fromAttributePaths(final String... strings) {

        return DynamicEntityGraph.loading().addPath(strings).build();
    }
}
