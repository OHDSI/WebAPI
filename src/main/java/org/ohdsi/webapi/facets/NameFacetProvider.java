package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class NameFacetProvider extends AbstractTextColumnBasedFacetProvider {
    private static final String FACET_NAME = "Name";
    private static final String FIELD_NAME = "name";
    private static final String COLUMN_NAME = "name";

    public NameFacetProvider(JdbcTemplate jdbcTemplate) {
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
