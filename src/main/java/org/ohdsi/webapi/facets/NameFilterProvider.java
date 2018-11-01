package org.ohdsi.webapi.facets;

import org.springframework.stereotype.Component;

@Component
public class NameFilterProvider extends AbstractTextColumnFilterProvider {
    private static final String FACET_NAME = "Name";
    private static final String FIELD_NAME = "name";

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    public String getField() {
        return FIELD_NAME;
    }
}
