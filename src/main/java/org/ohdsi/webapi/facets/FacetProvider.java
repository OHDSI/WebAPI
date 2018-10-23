package org.ohdsi.webapi.facets;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface FacetProvider {
    String getName();

    List<Pair<Object, Integer>> getValues(String entityName);
}
