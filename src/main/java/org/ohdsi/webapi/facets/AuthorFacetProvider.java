package org.ohdsi.webapi.facets;

import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorFacetProvider extends AbstractColumnBasedFacetProvider {
    private static final String FACET_NAME = "Author";
    private static final String FIELD_NAME = "createdBy";
    private static final String COLUMN_NAME = "created_by_id";

    private final UserRepository userRepository;

    public AuthorFacetProvider(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        super(jdbcTemplate);
        this.userRepository = userRepository;
    }

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    public <T> Predicate createFacetPredicate(List<FacetItem> items, CriteriaBuilder criteriaBuilder, Root<T> root) {
        assert items != null && !items.isEmpty();
        final List<Long> ids = items.stream().map(item -> Long.valueOf(item.key)).collect(Collectors.toList());
        return root.get(FIELD_NAME).in(ids);
    }

    @Override
    public <T> Predicate createTextSearchPredicate(String field, String text, CriteriaBuilder criteriaBuilder, Root<T> root) {
        final Path<String> userName = root.get(FIELD_NAME).get("name");
        return criteriaBuilder.like(userName, text + '%');
    }

    @Override
    protected String getColumn() {
        return COLUMN_NAME;
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

