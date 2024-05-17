package org.ohdsi.webapi;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
public abstract class AbstractDatabaseTest {
    static class JdbcTemplateTestWrapper extends ExternalResource {
        @Override
        protected void before() throws Throwable {
            jdbcTemplate = new JdbcTemplate(getDataSource());
            try {
                // note for future reference: should probably either define a TestContext DataSource with these params
                // or make it so this proparty is only set once (during database initialization) since the below will run for each test class (but only be effective once)
                System.setProperty("datasource.url", getDataSource().getConnection().getMetaData().getURL());
                System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    static class DriverExcludeTestWrapper extends ExternalResource {
        @Override
        protected void before() throws Throwable {
            // Put the redshift driver at the end so that it doesn't
            // conflict with postgres queries
            java.util.Enumeration<Driver> drivers =  DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getName().contains("com.amazon.redshift.jdbc")) {
                    try {
                        DriverManager.deregisterDriver(driver);
                        DriverManager.registerDriver(driver);
                    } catch (SQLException e) {
                        throw new RuntimeException("Could not deregister redshift driver", e);
                    }
                }
            }
        }
    }

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(new DriverExcludeTestWrapper())
            .around(pg = new PostgresSingletonRule())
            .around(new JdbcTemplateTestWrapper());

    protected static PostgresSingletonRule pg;

    protected static JdbcTemplate jdbcTemplate;

    protected static DataSource getDataSource() {
        return pg.getEmbeddedPostgres().getPostgresDatabase();
    }
   
    protected void truncateTable (String tableName) {
      jdbcTemplate.execute("TRUNCATE %s CASCADE".formatted(tableName));
    }
    protected void resetSequence(String sequenceName) {
      jdbcTemplate.execute("ALTER SEQUENCE %s RESTART WITH 1".formatted(sequenceName));
    }
}
