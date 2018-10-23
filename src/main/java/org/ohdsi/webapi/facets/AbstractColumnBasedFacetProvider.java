package org.ohdsi.webapi.facets;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractColumnBasedFacetProvider implements FacetProvider {
    private static final String[] GET_VALUES_PARAMS = { "values_column", "table"};
    private final String getValuesQuery = ResourceHelper.GetResourceAsString("/resources/facets.sql/getValues.sql");
    private final JdbcTemplate jdbcTemplate;

    public AbstractColumnBasedFacetProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Pair<Object, Integer>> getValues(String entityName) {
        final String query = SqlRender.renderSql(getValuesQuery, GET_VALUES_PARAMS, new String[]{ getColumn(), entityName });
        return jdbcTemplate.query(query, (resultSet, i) -> new ImmutablePair<>(getValue(resultSet), resultSet.getInt(2)));
    }

    protected abstract String getColumn();

    protected abstract Object getValue(ResultSet resultSet) throws SQLException;
}
