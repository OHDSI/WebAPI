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
import java.util.concurrent.TimeUnit;

public abstract class AbstractDateColumnBasedFacetProvider extends AbstractColumnBasedFacetProvider {
    private static final long SECONDS_IN_DAY = TimeUnit.DAYS.toSeconds(1);
    private static final String TWO_WEEKS_AGO = "2+ Weeks Ago";
    private static final String LAST_WEEK = "Last Week";
    private static final String THIS_WEEK = "This Week";
    private static final String WITHIN_24_HOURS = "Within 24 Hours";
    private static final String JUST_NOW = "Just Now";

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

    @SuppressWarnings("unchecked")
    private <T> Predicate createItemPredicate(FacetItem item, CriteriaBuilder cb, Root<T> root) {
        final Path field = root.get(getField());
        switch (item.text) {
            case TWO_WEEKS_AGO: {
                final Date from = getDaysFromNow(14);
                return cb.or(cb.lessThanOrEqualTo(field, cb.literal(from)), cb.isNull(field));
            }
            case LAST_WEEK: {
                final Date from = getDaysFromNow(14);
                final Date to = getDaysFromNow(7);
                return cb.between(field, cb.literal(from), cb.literal(to));
            }
            case THIS_WEEK: {
                final Date from = getDaysFromNow(7);
                return cb.greaterThanOrEqualTo(field, cb.literal(from));
            }
            case WITHIN_24_HOURS: {
                final Date from = getDaysFromNow(1);
                return cb.greaterThanOrEqualTo(field, cb.literal(from));
            }
            case JUST_NOW: {
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
        double days = date != null ? (Instant.now().toEpochMilli() - date.getTime()) / 1000.0 / SECONDS_IN_DAY : 100500;
        if (days < .01) {
            return JUST_NOW;
        } else if (days < 1) {
            return WITHIN_24_HOURS;
        } else if (days < 7) {
            return THIS_WEEK;
        } else if (days < 14) {
            return LAST_WEEK;
        } else {
            return TWO_WEEKS_AGO;
        }
    }

    private Date getDaysFromNow(int days) {
        return Date.from(Instant.now().minusSeconds(days * SECONDS_IN_DAY));
    }
}
