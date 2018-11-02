package org.ohdsi.webapi.facets;

import org.springframework.stereotype.Component;

@Component
public class NameFilterProvider extends AbstractTextColumnFilterProvider {
    public static final String COLUMN_NAME = "Name";
    private static final String FIELD_NAME = "name";

    @Override
    public String getName() {
        return COLUMN_NAME;
    }

    @Override
    public String getField() {
        return FIELD_NAME;
    }
}
