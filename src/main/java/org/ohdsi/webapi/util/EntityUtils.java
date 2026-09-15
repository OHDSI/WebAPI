package org.ohdsi.webapi.util;

import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphType;
import com.cosium.spring.data.jpa.entity.graph.domain.NamedEntityGraph;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Entity graphs are created as JPA load graphs, so attributes that are not listed keep their mapped fetch type.
 * The library default is a fetch graph, which Hibernate enforces since 5.4.2x by making every attribute
 * that is not listed lazy (e.g. the eager Source.daimons), leading to LazyInitializationException.
 */
public class EntityUtils {

    private EntityUtils() {

    }

    public static EntityGraph fromAttributePaths(final String... strings) {

        Assert.notEmpty(strings, "At least one attribute path is required.");
        return new DynamicEntityGraph(EntityGraphType.LOAD, Arrays.asList(strings));
    }

    public static EntityGraph fromName(final String name) {

        return new NamedEntityGraph(EntityGraphType.LOAD, name);
    }
}
