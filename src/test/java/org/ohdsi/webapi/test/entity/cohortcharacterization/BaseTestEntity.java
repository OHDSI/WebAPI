package org.ohdsi.webapi.test.entity.cohortcharacterization;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.test.entity.AbstractShiroTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.ohdsi.webapi.test.entity.cohortcharacterization.BaseTestEntity.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@ContextConfiguration(initializers = TestInitializer.class)  //?? why it doesn't work
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseTestEntity extends AbstractShiroTest {
    @Autowired
    protected CcController ccController;
    @Autowired
    protected CcRepository ccRepository;
    @Autowired
    protected ConversionService conversionService;
    protected CohortCharacterizationDTO firstIncomingDTO;
    protected CohortCharacterizationDTO firstSavedDTO;
    private static PostgreSQLContainer postgres;
    protected static String COPY_PREFIX = "COPY OF: ";
    protected static String NEW_TEST_ENTITY = "New test entity";
    protected static String SOME_UNIQUE_TEST_NAME = "Some unique test name";

    @BeforeClass
    public static void before() {

        postgres = new PostgreSQLContainer()
                .withDatabaseName("ohdsi_webapi")
                .withUsername("ohdsi")
                .withPassword("ohdsi");
        postgres.start();

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

    @Before
    public void setupDB() {
        firstIncomingDTO = new CohortCharacterizationDTO();
        firstIncomingDTO.setName(NEW_TEST_ENTITY);
        firstSavedDTO = ccController.create(firstIncomingDTO);
    }

    @After
    public void tearDownDB() {
        ccRepository.deleteAll();
    }

    public static class TestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "datasource.url=" + postgres.getJdbcUrl(),
                    "flyway.datasource.url=" + postgres.getJdbcUrl(),
                    "flyway.datasource.driverClassName=" + postgres.getDriverClassName());
        }
    }
}
