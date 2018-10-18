package org.ohdsi.webapi.facets;

import java.util.List;

public interface FacetProvider {
    String getName();
    String getColumn();

    List<Object> getValues(String entityName);
}
