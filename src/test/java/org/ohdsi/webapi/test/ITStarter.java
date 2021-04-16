package org.ohdsi.webapi.test;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SecurityIT.class,
        JobServiceIT.class,
        CohortAnalysisServiceIT.class,
        VocabularyServiceIT.class
})
@TestPropertySource(locations = "/application-test.properties")
public class ITStarter extends AbstractShiro {

    private static EmbeddedPostgres pg;
    private static final Logger log = LoggerFactory.getLogger(ITStarter.class);

    @BeforeClass
    public static void before() throws IOException {

        if (pg == null) {
            pg = EmbeddedPostgres.start();
            try {
                System.setProperty("datasource.url", pg.getPostgresDatabase().getConnection().getMetaData().getURL());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
            System.setProperty("security.db.datasource.url", System.getProperty("datasource.url"));
            System.setProperty("security.db.datasource.username", "postgres");
            System.setProperty("security.db.datasource.password", "postgres");
            System.setProperty("security.db.datasource.schema", "public");

            //set up shiro
            Subject subjectUnderTest = Mockito.mock(Subject.class);
            SimplePrincipalCollection principalCollection = Mockito.mock(SimplePrincipalCollection.class);
            Mockito.when(subjectUnderTest.isAuthenticated()).thenReturn(true);
            Mockito.when(subjectUnderTest.getPrincipals()).thenReturn(principalCollection);
            Mockito.when(principalCollection.getPrimaryPrincipal()).thenReturn("admin@odysseusinc.com");

            //bind the subject to the current thread
            setSubject(subjectUnderTest);
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
