package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class NameFacetProvider extends AbstractColumnBasedFacetProvider {
    private static final String FACET_NAME = "Name";
    private static final String FIELD_NAME = "name";
    private static final String COLUMN_NAME = "name";

    public NameFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    protected String getKey(ResultSet resultSet) throws SQLException {
        return getText(resultSet);
    }

    @Override
    protected String getColumn() {
        return COLUMN_NAME;
    }

    @Override
    protected String getText(ResultSet resultSet) throws SQLException {
        return resultSet.getString(1);
    }

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    public <T> Predicate createFacetPredicate(List<FacetItem> items, CriteriaBuilder criteriaBuilder, Root<T> root) {
        throw new IllegalStateException("faceted search on text columns not supported");
    }

    @Override
    public <T> Predicate createTextSearchPredicate(String field, String text, CriteriaBuilder criteriaBuilder, Root<T> root) {
        return criteriaBuilder.like(root.get(FIELD_NAME), text + '%');
    }
}
