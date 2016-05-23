package org.ohdsi.webapi.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import java.util.Arrays;

/**
 * Test related to characterization services
 */
public class CharacterizationServiceIT {

    @Value("${characterization.endpoint.job}")
    private String endpointCohortAnalysis;

    @Value("${vocabularyservice.endpoint.vocabularies}")
    private String endpointVocabularies;

    @Value("${vocabularyservice.endpoint.concept}")
    private String endpointConcept;

    @Value("${vocabularyservice.endpoint.domains}")
    private String endpointDomains;

//    @Test //may not want to always run analyses. TODO inject criteria from properties
//    public void createAnalysis() throws Exception {
//        CohortAnalysisTask task = new CohortAnalysisTask();
//        //set attributes
//        task.setAnalysisIds(Arrays.asList("0"));
//        task.setCohortDefinitionIds(Arrays.asList("1"));
//        final ResponseEntity<JobExecutionResource> postEntity = getRestTemplate().postForEntity(this.endpointCohortAnalysis,
//                task, JobExecutionResource.class);//TODO 409 or other errors prevent deserialization...
//        assertOk(postEntity);
//        Thread.sleep(10000);
//        final JobExecutionResource postExecution = postEntity.getBody();
//
//    }

    private void assertOk(final ResponseEntity<?> entity) {
        Assert.state(entity.getStatusCode() == HttpStatus.OK);
    }

//    private void assertJobExecution(final JobExecutionResource execution) {
//        Assert.state(execution != null);
//        Assert.state(execution.getExecutionId() != null);
//        Assert.state(execution.getJobInstanceResource().getInstanceId() != null);
//    }



//    @Test
//    public void concept() {
//        log.info("Testing concept endpoint");
//        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointConcept, String.class);
//    }

//    @Test
//    public void vocabularies() {
//        log.info("Testing vocabulary endpoint");
//        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointVocabularies, String.class);
//        assertOK(entity);
//        //or
//        Vocabulary[] vocabularies = getRestTemplate().getForObject(this.endpointVocabularies, Vocabulary[].class);
//        for (Vocabulary v : vocabularies) {
//            log.debug("Vocab: " + v.vocabularyName);
//        }
//    }

//    @Test
//    public void domains() {
//        log.info("Testing domain endpoint");
//        final ResponseEntity<String> entity = getRestTemplate().getForEntity(this.endpointDomains, String.class);
//        assertOK(entity);
//        //or
//        Domain[] domains = getRestTemplate().getForObject(this.endpointDomains, Domain[].class);
//        for (Domain d : domains) {
//            log.debug("Domain:" + d.domainName);
//        }
//    }

}
