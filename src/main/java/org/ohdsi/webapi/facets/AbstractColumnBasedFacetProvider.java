package org.ohdsi.webapi.facets;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractColumnBasedFacetProvider implements FacetProvider {
    private static final String[] GET_VALUES_PARAMS = { "values_column", "table"};
    private final String getValuesQuery = ResourceHelper.GetResourceAsString("/resources/facets.sql/getValues.sql");
    private final JdbcTemplate jdbcTemplate;

    public AbstractColumnBasedFacetProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FacetItem> getValues(String entityName) {
        final String query = SqlRender.renderSql(getValuesQuery, GET_VALUES_PARAMS, new String[]{ getColumn(), entityName });
        final Map<String, FacetItem> grouped = new HashMap<>();
        jdbcTemplate.query(query, (resultSet) -> {
            final String text = getText(resultSet);
            final String key = getKey(resultSet);
            final int count = resultSet.getInt(2);
            grouped.merge(text, new FacetItem(text, key, count), (i1, i2) -> {i1.count += i2.count; return i1;});
        });
        return new ArrayList<>(grouped.values());
    }

    protected abstract String getKey(ResultSet resultSet) throws SQLException;

    protected abstract String getColumn();

    protected abstract String getText(ResultSet resultSet) throws SQLException;
}
