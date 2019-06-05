package org.ohdsi.webapi.test.entity;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.ohdsi.webapi.test.entity.BaseTestEntity.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@ContextConfiguration(initializers = TestInitializer.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseTestEntity extends AbstractShiroTest {    
    @Autowired
    protected ConversionService conversionService;
    protected final static String COPY_PREFIX = "COPY OF: ";
    protected final static String NEW_TEST_ENTITY = "New test entity";
    protected final static String SOME_UNIQUE_TEST_NAME = "Some unique test name";

    @BeforeClass
    public static void before() {

        Subject subjectUnderTest = Mockito.mock(Subject.class);
        SimplePrincipalCollection principalCollection = Mockito.mock(SimplePrincipalCollection.class);
        Mockito.when(subjectUnderTest.isAuthenticated()).thenReturn(true);
        Mockito.when(subjectUnderTest.getPrincipals()).thenReturn(principalCollection);
        Mockito.when(principalCollection.getPrimaryPrincipal()).thenReturn("admin@odysseusinc.com");

        //bind the subject to the current thread
        setSubject(subjectUnderTest);
    }

    @AfterClass
    public static void tearDownSubject() {
        //unbind the subject from the current thread
        clearSubject();
    }

    public static class TestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            JdbcDatabaseContainer container = new PostgreSQLContainer()
                    .withDatabaseName("ohdsi_webapi")
                    .withUsername("ohdsi")
                    .withPassword("ohdsi");

//            container = new MSSQLServerContainer();
            container.start();           
            
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "datasource.url=" + container.getJdbcUrl(),
                    "flyway.datasource.url=" + container.getJdbcUrl(),
                    "flyway.datasource.driverClassName=" + container.getDriverClassName());
        }
    }
}
