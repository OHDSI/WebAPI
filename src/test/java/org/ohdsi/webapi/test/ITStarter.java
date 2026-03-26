package org.ohdsi.webapi.test;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // SecurityIT.class, // DISABLED - Jersey-specific test
        JobServiceIT.class,
        VocabularyServiceIT.class,
        CDMResultsServiceIT.class
})
@TestPropertySource(locations = "/application-test.properties")
public class ITStarter extends AbstractSpringSecurity {

    private static EmbeddedPostgres pg;
    private static final Logger log = LoggerFactory.getLogger(ITStarter.class);

    @BeforeClass
    public static void before() throws IOException {

        if (pg == null) {
            pg = EmbeddedPostgres.start();
            try {
                String jdbcUrl = pg.getPostgresDatabase().getConnection().getMetaData().getURL();
                System.setProperty("datasource.url", jdbcUrl);
                System.setProperty("spring.flyway.url", jdbcUrl);
                System.setProperty("security.auth.db.datasource.url", jdbcUrl);
                System.setProperty("security.auth.db.datasource.username", "postgres");
                System.setProperty("security.auth.db.datasource.password", "postgres");
                System.setProperty("security.auth.db.datasource.schema", "public");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // set up Spring Security test principal (replaces legacy Shiro subject)
            org.ohdsi.webapi.security.identity.WebApiPrincipal principal = new org.ohdsi.webapi.security.identity.WebApiPrincipal(1L, "admin@odysseusinc.com");
            setSubject(principal);
        }
    }

    public static DataSource getDataSource() {
        return pg.getPostgresDatabase();
    }

    @AfterClass
    public static void tearDownSubject() {
        
        String callerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String currentClassName = Thread.currentThread().getStackTrace()[1].getClassName();
        if (pg != null && currentClassName.equalsIgnoreCase(callerClassName)) {
            try {
                //unbind the subject from the current thread
                clearSubject();
                pg.close();
            } catch (Exception ex) {
                log.warn("Error while stopping the embedded PostgreSQL instance", ex);
            }
        }
    }
}
