package org.ohdsi.webapi;

import org.junit.Test;
import org.junit.BeforeClass;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = WebApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "security.provider=DisabledSecurity")
public class JerseyOnlyStartupTest extends AbstractDatabaseTest {
    @BeforeClass
    public static void avoidDuplicateUrlHandlerFactory() {
        // SqlRender installs a JVM-global factory; match the existing WebApiIT setup.
        TomcatURLStreamHandlerFactory.disable();
    }

    @Autowired
    private TestRestTemplate rest;

    @Value("${baseUri}")
    private String baseUri;

    @Test
    public void servesJerseyEndpointsWithoutMvc() {
        ResponseEntity<String> info = rest.getForEntity(baseUri + "/info", String.class);
        assertEquals(HttpStatus.OK, info.getStatusCode());
        assertNotNull(info.getBody());
        ResponseEntity<String> locale = rest.getForEntity(baseUri + "/i18n?lang=en", String.class);
        assertEquals(HttpStatus.OK, locale.getStatusCode());
        assertTrue(locale.getHeaders().getContentType().toString().contains("application/json"));
    }
}
