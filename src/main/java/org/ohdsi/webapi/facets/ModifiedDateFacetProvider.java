package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ModifiedDateFacetProvider extends AbstractDateColumnBasedFacetProvider {

    public ModifiedDateFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getField() {
        return "modifiedDate";
    }

    @Override
    public String getName() {
        return "Updated";
    }

    @Override
    protected String getColumn() {
        return "modified_date";
    }

}
