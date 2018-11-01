package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ModifiedDateFacetProvider extends AbstractDateColumnBasedFacetProvider {

    public static final String FACET_NAME = "Updated";
    private static final String FIELD_NAME = "modifiedDate";
    private static final String COLUMN_NAME = "modified_date";

    public ModifiedDateFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getField() {
        return FIELD_NAME;
    }

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    protected String getColumn() {
        return COLUMN_NAME;
    }

}
