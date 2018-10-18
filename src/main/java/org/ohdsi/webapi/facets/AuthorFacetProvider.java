package org.ohdsi.webapi.facets;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorFacetProvider implements FacetProvider {
    private static final String[] GET_VALUES_PARAMS = { "values_column", "table"};
    public static final String COLUMN = "created_date";
    private final String getValuesQuery = ResourceHelper.GetResourceAsString("/resources/facets.sql/getValues.sql");

    private final JdbcTemplate jdbcTemplate;

    public AuthorFacetProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() {
        return "Author";
    }

    @Override
    public String getColumn() {
        return "created_by_id";
    }

    @Override
    public List<Object> getValues(String entityName) {
        //TODO - fix query to return user, not id
        final String query = SqlRender.renderSql(getValuesQuery, GET_VALUES_PARAMS, new String[]{ COLUMN, entityName });
        return jdbcTemplate.query(query, (resultSet, i) -> resultSet.getObject(1));
    }

}

