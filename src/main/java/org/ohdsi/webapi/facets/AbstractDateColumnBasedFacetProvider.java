package org.ohdsi.webapi.facets;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public abstract class AbstractDateColumnBasedFacetProvider extends AbstractColumnBasedFacetProvider {
    private static final int SECONDS_IN_DAY = 24 * 60 * 60;

    public AbstractDateColumnBasedFacetProvider(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    protected abstract String getField();

    @Override
    public <T> Predicate createFacetPredicate(List<FacetItem> items, CriteriaBuilder cb, Root<T> root) {
        assert items != null && !items.isEmpty();
        return items.size() == 1
                ? createItemPredicate(items.get(0), cb, root)
                : cb.or(items.stream().map(item -> createItemPredicate(item, cb, root)).toArray(Predicate[]::new));
    }

    @Override
    public <T> Predicate createTextSearchPredicate(String field, String text, CriteriaBuilder criteriaBuilder, Root<T> root) {
        throw new IllegalStateException("text search on date columns not supported");
    }

    @SuppressWarnings("unchecked")
    private <T> Predicate createItemPredicate(FacetItem item, CriteriaBuilder cb, Root<T> root) {
        final Path field = root.get(getField());
        switch (item.text) {
            case "2+ Weeks Ago": {
                final Date from = getDaysFromNow(14);
                return cb.or(cb.lessThanOrEqualTo(field, cb.literal(from)), cb.isNull(field));
            }
            case "Last Week": {
                final Date from = getDaysFromNow(14);
                final Date to = getDaysFromNow(7);
                return cb.between(field, cb.literal(from), cb.literal(to));
            }
            case "This Week": {
                final Date from = getDaysFromNow(7);
                return cb.greaterThanOrEqualTo(field, cb.literal(from));
            }
            case "Within 24 Hours": {
                final Date from = getDaysFromNow(1);
                return cb.greaterThanOrEqualTo(field, cb.literal(from));
            }
            case "Just Now": {
                final Date from = Date.from(Instant.now().minusSeconds(SECONDS_IN_DAY / 100));
                return cb.greaterThanOrEqualTo(field, cb.literal(from));
            }
            default:
                throw new IllegalArgumentException("unknown date facet value");
        }

    }


    @Override
    protected String getKey(ResultSet resultSet) throws SQLException {
        final java.sql.Date date = resultSet.getDate(1);
        return date != null ? new SimpleDateFormat().format(date) : "";
    }

    @Override
    protected String getText(ResultSet resultSet) throws SQLException {
        final java.sql.Date date = resultSet.getDate(1);
        double days = date != null ? (new Date().getTime() - date.getTime()) / 1000.0 / 60 / 60 / 24 : 100500;
        if (days < .01) {
            return "Just Now";
        } else if (days < 1) {
            return "Within 24 Hours";
        } else if (days < 7) {
            return "This Week";
        } else if (days < 14) {
            return "Last Week";
        } else {
            return "2+ Weeks Ago";
        }
    }

    private Date getDaysFromNow(int days) {
        return Date.from(Instant.now().minusSeconds(days * SECONDS_IN_DAY));
    }
}
