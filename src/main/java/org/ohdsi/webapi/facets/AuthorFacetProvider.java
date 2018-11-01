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
public class AuthorFacetProvider extends AbstractUserFacetProvider {
    public static final String FACET_NAME = "Author";

    private static final String FIELD_NAME = "createdBy";
    private static final String COLUMN_NAME = "created_by_id";

    public AuthorFacetProvider(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        super(jdbcTemplate, userRepository);
    }

    @Override
    public String getName() {
        return FACET_NAME;
    }

    @Override
    protected String getField() {
        return FIELD_NAME;
    }

    @Override
    protected String getColumn() {
        return COLUMN_NAME;
    }

}

