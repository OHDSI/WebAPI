package org.ohdsi.webapi.migration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.AfterClass;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

// Copied from https://github.com/testcontainers/testcontainers-java/blob/8dee638fc4c5ad3c11f0156abf97cb148b58db27/modules/jdbc-test/src/test/java/org/testcontainers/junit/AbstractContainerDatabaseTest.java
public abstract class AbstractContainerDatabaseTest {

    private final static Set<HikariDataSource> datasourcesForCleanup = new HashSet<>();

    static ResultSet performQuery(JdbcDatabaseContainer container, String sql) throws SQLException {
        DataSource ds = getDataSource(container);
        Statement statement = ds.getConnection().createStatement();
        statement.execute(sql);
        ResultSet resultSet = statement.getResultSet();

        if(resultSet != null) {
            resultSet.next();
        }

        return resultSet;
    }

    static DataSource getDataSource(JdbcDatabaseContainer container) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());

        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        datasourcesForCleanup.add(dataSource);

        return dataSource;
    }

    @AfterClass
    public static void teardown() {
        datasourcesForCleanup.forEach(HikariDataSource::close);
    }
}