package org.ohdsi.webapi.feanalysis;

import org.ohdsi.webapi.facets.AbstractTextColumnBasedFacetProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DescriptionFacetProvider extends AbstractTextColumnBasedFacetProvider {
    private static final String FACET_NAME = "Description";
    private static final String FIELD_NAME = "descr";
    private static final String COLUMN_NAME = "descr";

    public DescriptionFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getColumn() {
        return COLUMN_NAME;
    }

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    public String getField() {
        return FIELD_NAME;
    }
}
