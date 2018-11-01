package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    @Override
    public <T> Predicate createFacetPredicate(List<FacetItem> items, CriteriaBuilder criteriaBuilder, Root<T> root) {
        throw new IllegalStateException("faceted search on text columns not supported");
    }

    @Override
    public <T> Predicate createTextSearchPredicate(String field, String text, Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.like(root.get(getField()), '%' + text + '%');
    }

    public abstract String getField();
}
