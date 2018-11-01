package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractTextColumnBasedFacetProvider extends AbstractColumnBasedFacetProvider {
    public AbstractTextColumnBasedFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getKey(ResultSet resultSet) throws SQLException {
        return resultSet.getString(1);
    }

    @Override
    protected String getText(ResultSet resultSet) throws SQLException {
        return getKey(resultSet);
    }

    public abstract String getField();
}
