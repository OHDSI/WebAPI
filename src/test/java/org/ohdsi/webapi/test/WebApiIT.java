package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@ActiveProfiles("test")
@Ignore//do not run this test
public abstract class WebApiIT {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    @Value("${baseUri}")
    private String baseUri;
    
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    
    /**
     * @return the restTemplate
     */
    public RestTemplate getRestTemplate() {
        return this.restTemplate.getRestTemplate();
    }
    
    /**
     * @return the baseUri
     */
    public String getBaseUri() {
        return this.baseUri;
    }
    
    /**
     * @param baseUri the baseUri to set
     */
    public void setBaseUri(final String baseUri) {
        this.baseUri = baseUri;
    }
    
    public void assertOK(ResponseEntity entity){
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        log.debug("Body:" + entity.getBody());
    }
}
