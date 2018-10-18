package org.ohdsi.webapi.facets;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModifiedDateFacetProvider implements FacetProvider {
    private static final String[] GET_VALUES_PARAMS = { "values_column", "table"};
    public static final String COLUMN = "created_date";
    private final String getValuesQuery = ResourceHelper.GetResourceAsString("/resources/facets.sql/getValues.sql");
    private final JdbcTemplate jdbcTemplate;

    public ModifiedDateFacetProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() {
        return "Modified";
    }

    @Override
    public String getColumn() {
        return "modified_date";
    }

    @Override
    public List<Object> getValues(String entityName) {
        final String query = SqlRender.renderSql(getValuesQuery, GET_VALUES_PARAMS, new String[]{ COLUMN, entityName });
        return jdbcTemplate.query(query, (resultSet, i) -> resultSet.getObject(1));
    }
}
