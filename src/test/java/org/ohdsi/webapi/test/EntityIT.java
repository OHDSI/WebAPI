package org.ohdsi.webapi.test;

import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mockito;
import org.ohdsi.webapi.test.entity.AbstractShiroTest;
import org.ohdsi.webapi.test.entity.CCEntity;
import org.ohdsi.webapi.test.entity.CohortDefinitionEntity;
import org.ohdsi.webapi.test.entity.ConceptSetEntity;
import org.ohdsi.webapi.test.entity.EstimationEntity;
import org.ohdsi.webapi.test.entity.IREntity;
import org.ohdsi.webapi.test.entity.PathwayEntity;
import org.ohdsi.webapi.test.entity.PredictionEntity;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        
//        ConceptSetEntity.class,
//        CohortDefinitionEntity.class,
        CCEntity.class
//        IREntity.class,
//        PathwayEntity.class,
//        EstimationEntity.class,
//        PredictionEntity.class
})
public class EntityIT extends AbstractShiroTest {

    @ClassRule
    public static SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance();
    
    @BeforeClass
    public static void before() {
        try {
            System.setProperty("datasource.url", pg.getEmbeddedPostgres().getPostgresDatabase().getConnection().getMetaData().getURL());
            System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
            
            //set up shiro
            Subject subjectUnderTest = Mockito.mock(Subject.class);
            SimplePrincipalCollection principalCollection = Mockito.mock(SimplePrincipalCollection.class);
            Mockito.when(subjectUnderTest.isAuthenticated()).thenReturn(true);
            Mockito.when(subjectUnderTest.getPrincipals()).thenReturn(principalCollection);
            Mockito.when(principalCollection.getPrimaryPrincipal()).thenReturn("admin@odysseusinc.com");

            //bind the subject to the current thread
            setSubject(subjectUnderTest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @AfterClass
    public static void tearDownSubject() {
        //unbind the subject from the current thread
        clearSubject();
    }
}
