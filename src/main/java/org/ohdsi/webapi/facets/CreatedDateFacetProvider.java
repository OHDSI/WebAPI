package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CreatedDateFacetProvider extends AbstractDateColumnBasedFacetProvider {
    public CreatedDateFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getField() {
        return "createdDate";
    }

    @Override
    public String getName() {
        return "Created";
    }

    @Override
    protected String getColumn() {
        return "created_date";
    }
}
