package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public abstract class AbstractDateColumnBasedFacetProvider extends AbstractColumnBasedFacetProvider {
    public static final int DAY = 24 * 60 * 60;

    public AbstractDateColumnBasedFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    protected abstract String getField();

    @Override
    @SuppressWarnings("unchecked")
    public Predicate createPredicate(List<FilterItem> items, CriteriaBuilder criteriaBuilder, Root root) {
        assert items != null && !items.isEmpty();
        if(items.size() == 1) {
            final FilterItem item = items.get(0);
            switch (item.text) {
                case "2+ Weeks Ago": {
                    final Date from = getDaysFromNow(14);
                    return criteriaBuilder.lessThanOrEqualTo(root.get(getField()), criteriaBuilder.literal(from));
                }
                case "Last Week": {
                    final Date from = getDaysFromNow(14);
                    final Date to = getDaysFromNow(7);
                    return criteriaBuilder.between(root.get(getField()), criteriaBuilder.literal(from), criteriaBuilder.literal(to));
                }
                case "This Week": {
                    final Date from = getDaysFromNow(7);
                    return criteriaBuilder.lessThanOrEqualTo(root.get(getField()), criteriaBuilder.literal(from));
                }
                case "Within 24 Hours": {
                    final Date from = getDaysFromNow(1);
                    return criteriaBuilder.lessThanOrEqualTo(root.get(getField()), criteriaBuilder.literal(from));
                }
                case "Just Now": {
                    final Date from = Date.from(Instant.now().minusSeconds(DAY / 100));
                    return criteriaBuilder.lessThanOrEqualTo(root.get(getField()), criteriaBuilder.literal(from));
                }
            }
        }
        return null;
    }


    @Override
    protected String getKey(ResultSet resultSet) throws SQLException {
        return String.valueOf(resultSet.getDate(1));
    }

    @Override
    protected String getText(ResultSet resultSet) throws SQLException {
        return getKey(resultSet);
    }

    private Date getDaysFromNow(int days) {
        return Date.from(Instant.now().minusSeconds(days * 24 * 60 * 60));
    }
}
