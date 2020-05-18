package org.ohdsi.webapi.test;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;
import org.ohdsi.webapi.test.entity.AbstractShiro;
import org.ohdsi.webapi.test.entity.CCEntityTest;
import org.ohdsi.webapi.test.entity.CohortDefinitionEntityTest;
import org.ohdsi.webapi.test.entity.ConceptSetEntityTest;
import org.ohdsi.webapi.test.entity.EstimationEntityTest;
import org.ohdsi.webapi.test.entity.IREntityTest;
import org.ohdsi.webapi.test.entity.PathwayEntityTest;
import org.ohdsi.webapi.test.entity.PredictionEntityTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SecurityIT.class,
        JobServiceIT.class,
        VocabularyServiceIT.class,
        CohortAnalysisServiceIT.class,        
        ConceptSetEntityTest.class,
        CohortDefinitionEntityTest.class,
        CCEntityTest.class,
        IREntityTest.class,
        PathwayEntityTest.class,
        EstimationEntityTest.class,
        PredictionEntityTest.class        
})
@TestPropertySource(locations = "/application-test.properties")
public class ITStarter extends AbstractShiro{

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
