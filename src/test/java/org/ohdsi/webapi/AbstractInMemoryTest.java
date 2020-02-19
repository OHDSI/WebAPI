package org.ohdsi.webapi;

import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
@Ignore//do not run this test
public abstract class AbstractInMemoryTest {
    @ClassRule
    public static TestRule chain = RuleChain.outerRule(new DriverExcludeTestWrapper())
            .around(pg = EmbeddedPostgresRules.singleInstance())
            .around(new JdbcTemplateTestWrapper());

    protected static SingleInstancePostgresRule pg;

    protected static JdbcTemplate jdbcTemplate;

    protected static DataSource getDataSource() {
        return pg.getEmbeddedPostgres().getPostgresDatabase();
    }

    static class JdbcTemplateTestWrapper extends ExternalResource {
        @Override
        protected void before() throws Throwable {
            jdbcTemplate = new JdbcTemplate(getDataSource());
            try {
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
}
