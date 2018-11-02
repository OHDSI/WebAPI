package org.ohdsi.webapi.feanalysis;

import org.ohdsi.webapi.facets.AbstractTextColumnFilterProvider;
import org.springframework.stereotype.Component;

@Component
public class DescriptionFilterProvider extends AbstractTextColumnFilterProvider {
    public static final String COLUMN_NAME = "Description";
    private static final String FIELD_NAME = "descr";

    @Override
    public String getName() {
        return COLUMN_NAME;
    }

    @Override
    public String getField() {
        return FIELD_NAME;
    }
}
