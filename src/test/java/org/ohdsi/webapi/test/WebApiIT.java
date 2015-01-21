package org.ohdsi.webapi.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebApi.class)
@WebAppConfiguration
@IntegrationTest
@ActiveProfiles("test")
public class WebApiIT {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    @Value("${baseUri}")
    private String baseUri;
    
    private final RestTemplate restTemplate = new TestRestTemplate();
    
    /**
     * @return the restTemplate
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
    
    /**
     * @return the baseUri
     */
    public String getBaseUri() {
        return baseUri;
    }
    
    /**
     * @param baseUri the baseUri to set
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}
