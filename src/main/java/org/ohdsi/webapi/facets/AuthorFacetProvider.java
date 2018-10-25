package org.ohdsi.webapi.facets;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorFacetProvider extends AbstractColumnBasedFacetProvider {


    private final UserRepository userRepository;

    public AuthorFacetProvider(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        super(jdbcTemplate);
        this.userRepository = userRepository;
    }

    @Override
    public String getName() {
        return "Author";
    }

    @Override
    public Predicate createPredicate(List<FilterItem> items, CriteriaBuilder criteriaBuilder, Root root) {
        assert items != null && !items.isEmpty();
        final List<Long> ids = items.stream().map(item -> Long.valueOf(item.key)).collect(Collectors.toList());
        return root.get("createdBy").in(ids);
    }

    @Override
    protected String getColumn() {
        return "created_by_id";
    }

    @Override
    protected String getKey(ResultSet resultSet) throws SQLException {
        return String.valueOf(resultSet.getInt(1));
    }

    @Override
    protected String getText(ResultSet resultSet) throws SQLException {
        return userRepository.getUserLoginById((long) resultSet.getInt(1));
    }
}

