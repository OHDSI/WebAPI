package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
public class VocabularyServiceIT extends WebApiIT {
    
    @Value("${vocabulary.service.endpoint}")
    private String endpoint;
    
    private final RestTemplate restTemplate = new TestRestTemplate();
    
    @Test
    public void concept() {
        log.info("Testing concept endpoint");
        final ResponseEntity<String> entity = this.restTemplate.getForEntity(this.endpoint + "/concept/1", String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        log.debug("Body:" + entity.getBody());
    }
}
