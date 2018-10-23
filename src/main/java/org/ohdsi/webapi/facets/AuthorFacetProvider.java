package org.ohdsi.webapi.facets;

import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

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
    protected String getColumn() {
        return "created_by_id";
    }

    @Override
    protected Object getValue(ResultSet resultSet) throws SQLException {
        final long id = resultSet.getInt(1);
        final String login = userRepository.getUserLoginById(id);
        return new UserService.User(id,  login);
    }
}

