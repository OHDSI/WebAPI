package org.ohdsi.webapi.feanalysis;

import org.ohdsi.webapi.facets.AbstractTextColumnFilterProvider;
import org.springframework.stereotype.Component;

@Component
public class DescriptionFilterProvider extends AbstractTextColumnFilterProvider {
    private static final String FACET_NAME = "Description";
    private static final String FIELD_NAME = "descr";

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    public String getField() {
        return FIELD_NAME;
    }
}
