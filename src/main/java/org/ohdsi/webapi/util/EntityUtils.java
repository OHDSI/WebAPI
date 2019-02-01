package org.ohdsi.webapi.util;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;

public class EntityUtils {

    private EntityUtils() {

    }
    
    public static EntityGraph fromAttributePaths(final String... strings) {

        return EntityGraphUtils.fromAttributePaths(strings);
    }
}
