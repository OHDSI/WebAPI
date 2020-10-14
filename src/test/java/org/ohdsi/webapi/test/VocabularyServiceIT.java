package org.ohdsi.webapi.test;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

@DatabaseSetup("/database/source.xml")
public class VocabularyServiceIT extends WebApiIT {

    @Value("${vocabularyservice.endpoint.vocabularies}")
    private String endpointVocabularies;

    @Value("${vocabularyservice.endpoint.concept}")
    private String endpointConcept;

    @Value("${vocabularyservice.endpoint.domains}")
    private String endpointDomains;

    @Test
    public void canGetConcept() {

        //Action
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointConcept, String.class);
        
        //Assertion
        assertOK(entity);
    }

    @Test
    public void canGetVocabularies() {

        //Action
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointVocabularies, String.class);
        
        //Assertion
        assertOK(entity);
    }

    @Test
    public void canGetDomains() {

        //Action
        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointDomains, String.class);

        //Assertion
        assertOK(entity);
    }
}
