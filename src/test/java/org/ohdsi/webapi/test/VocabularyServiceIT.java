package org.ohdsi.webapi.test;

import org.junit.Test;
import org.ohdsi.webapi.vocabulary.Domain;
import org.ohdsi.webapi.vocabulary.Vocabulary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

/**
 *
 */
public class VocabularyServiceIT extends WebApiIT {
    
    @Value("${vocabularyservice.endpoint.vocabularies}")
    private String endpointVocabularies;
    
    @Value("${vocabularyservice.endpoint.concept}")
    private String endpointConcept;
    
    @Value("${vocabularyservice.endpoint.domains}")
    private String endpointDomains;
    
    @Test
    public void concept() {
        log.info("Testing concept endpoint");
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointConcept, String.class);
        assertOK(entity);
    }
    
    @Test
    public void vocabularies() {
        log.info("Testing vocabulary endpoint");
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointVocabularies, String.class);
        assertOK(entity);
        //or
        Vocabulary[] vocabularies = getRestTemplate().getForObject(this.endpointVocabularies, Vocabulary[].class);
        for (Vocabulary v : vocabularies) {
            log.debug("Vocab: " + v.vocabularyName);
        }
    }
    
    @Test
    public void domains() {
        log.info("Testing domain endpoint");
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointDomains, String.class);
        assertOK(entity);
        //or
        Domain[] domains = getRestTemplate().getForObject(this.endpointDomains, Domain[].class);
        for (Domain d : domains) {
            log.debug("Domain:" + d.domainName);
        }
    }
}
