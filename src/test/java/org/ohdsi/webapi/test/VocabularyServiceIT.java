package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ohdsi.webapi.vocabulary.Vocabulary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 */
public class VocabularyServiceIT extends WebApiIT {
    
    @Value("${vocabularyservice.endpoint.vocabularies}")
    private String endpoint;
    
    @Test
    public void vocabularies() {
        log.info("Testing vocabulary endpoint");
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpoint, String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        log.debug("Body:" + entity.getBody());
        //or
        Vocabulary[] vocabularies = getRestTemplate().getForObject(this.endpoint, Vocabulary[].class);
        for (Vocabulary v : vocabularies) {
            log.debug("Vocab: " + v.vocabularyName);
        }
    }
}
