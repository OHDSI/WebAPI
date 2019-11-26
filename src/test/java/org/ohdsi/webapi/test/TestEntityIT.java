package org.ohdsi.webapi.test;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;
import org.ohdsi.webapi.test.entity.AbstractShiroTest;
import org.ohdsi.webapi.test.entity.cohortcharacterization.copy.TestCCCopy;
import org.ohdsi.webapi.test.entity.cohortcharacterization.create.TestCCCreate;
import org.ohdsi.webapi.test.entity.cohortcharacterization.importing.TestCCImport;
import org.ohdsi.webapi.test.entity.cohortdefinition.copy.TestCDCopy;
import org.ohdsi.webapi.test.entity.cohortdefinition.create.TestCDCreate;
import org.ohdsi.webapi.test.entity.conceptset.copy.TestCSCopy;
import org.ohdsi.webapi.test.entity.conceptset.create.TestCSCreate;
import org.ohdsi.webapi.test.entity.estimation.copy.TestEstimationCopy;
import org.ohdsi.webapi.test.entity.estimation.create.TestEstimationCreate;
import org.ohdsi.webapi.test.entity.estimation.importing.TestEstimationImport;
import org.ohdsi.webapi.test.entity.incidencerate.copy.TestIRCopy;
import org.ohdsi.webapi.test.entity.incidencerate.create.TestIRCreate;
import org.ohdsi.webapi.test.entity.pathway.copy.TestPWCopy;
import org.ohdsi.webapi.test.entity.pathway.create.TestPWCreate;
import org.ohdsi.webapi.test.entity.pathway.importing.TestPWImport;
import org.ohdsi.webapi.test.entity.prediction.copy.TestPredictionCopy;
import org.ohdsi.webapi.test.entity.prediction.create.TestPredictionCreate;
import org.ohdsi.webapi.test.entity.prediction.importing.TestPredictionImport;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestCCCreate.class,
        TestCCImport.class,
        TestCCCopy.class,
        
        TestCSCreate.class,
        TestCSCopy.class,
        
        TestCDCreate.class,
        TestCDCopy.class,
        
        TestPWCreate.class,
        TestPWImport.class,
        TestPWCopy.class,
        
        TestIRCreate.class,
        TestIRCopy.class,

        TestEstimationCreate.class,
        TestEstimationImport.class,
        TestEstimationCopy.class,

        TestPredictionCreate.class,
        TestPredictionImport.class,
        TestPredictionCopy.class})
public class TestEntityIT extends AbstractShiroTest {

    private static EmbeddedPostgres pg;
    @BeforeClass
    public static void before() {
        try {
            if (pg == null){
                pg = EmbeddedPostgres.start();
            }
            Subject subjectUnderTest = Mockito.mock(Subject.class);
            SimplePrincipalCollection principalCollection = Mockito.mock(SimplePrincipalCollection.class);
            Mockito.when(subjectUnderTest.isAuthenticated()).thenReturn(true);
            Mockito.when(subjectUnderTest.getPrincipals()).thenReturn(principalCollection);
            Mockito.when(principalCollection.getPrimaryPrincipal()).thenReturn("admin@odysseusinc.com");

            //bind the subject to the current thread
            setSubject(subjectUnderTest);

            System.setProperty("datasource.url", getDatabaseMetadata().getURL());
            System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
            System.setProperty("flyway.datasource.driverClassName=", getDatabaseMetadata().getDriverName());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @AfterClass
    public static void tearDownSubject() {
        //unbind the subject from the current thread
        clearSubject();
        try {
            pg.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DatabaseMetaData getDatabaseMetadata() throws SQLException {

        return pg.getPostgresDatabase().getConnection().getMetaData();
    }
}
