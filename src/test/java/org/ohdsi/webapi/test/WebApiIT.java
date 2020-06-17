package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import java.io.IOException;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DbUnitConfiguration(databaseConnection = {"primaryDataSource"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public class WebApiIT {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${baseUri}")
    private String baseUri;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    
    @BeforeClass
    public static void before() throws IOException {
        TomcatURLStreamHandlerFactory.disable();
        ITStarter.before();
    }

    @AfterClass
    public static void after() {
        ITStarter.tearDownSubject();
    }

    public TestRestTemplate getRestTemplate() {

        return this.restTemplate;
    }

    public String getBaseUri() {

        return this.baseUri;
    }

    public void setBaseUri(final String baseUri) {

        this.baseUri = baseUri;
    }

    public void assertOK(ResponseEntity<?> entity) {

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        if (log.isDebugEnabled()) {
            log.debug("Body: {}", entity.getBody());
        }
    }
}
