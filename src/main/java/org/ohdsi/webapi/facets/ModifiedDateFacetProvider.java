package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ModifiedDateFacetProvider extends AbstractColumnBasedFacetProvider {

    public ModifiedDateFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public String getName() {
        return "Updated";
    }

    @Override
    protected String getColumn() {
        return "created_date";
    }

    @Override
    protected Object getValue(ResultSet resultSet) throws SQLException {
        return resultSet.getDate(1);
    }
}
