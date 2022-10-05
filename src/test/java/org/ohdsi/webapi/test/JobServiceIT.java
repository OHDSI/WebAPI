package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.ohdsi.webapi.exampleapplication.ExampleApplicationWithJobService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobInstanceResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@DatabaseTearDown(value = "/database/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class JobServiceIT extends WebApiIT {
    
    @Value("${exampleservice.endpoint}")
    private String endpointExample;
    
    @Value("${jobservice.endpoint.job}")
    private String endpointJob;
    
    @Value("${jobservice.endpoint.jobexecution}")
    private String endpointJobExecution;
    
    @Value("${jobservice.endpoint.jobexecution.alt}")
    private String endpointJobExecutionAlternative;

    /* The test is ignored, because it is failing with current xstream version com.thoughtworks.xstream:xstream:1.4.19.
     *  see https://github.com/OHDSI/WebAPI/issues/2109 for details. */
    @Test
    @Ignore
    public void createAndFindJob() {
        //create/queue job
        final ResponseEntity<JobExecutionResource> postEntity = getRestTemplate().postForEntity(this.endpointExample, null,
            JobExecutionResource.class);
        assertOk(postEntity);
        final JobExecutionResource postExecution = postEntity.getBody();
        assertJobExecution(postExecution);
        
        //check on status of job
        
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("instanceId", postExecution.getJobInstanceResource().getInstanceId());
        params.put("executionId", postExecution.getExecutionId());
        
        //retry until asynchronous job is complete
        final RetryTemplate template = new RetryTemplate();
        
        final TimeoutRetryPolicy policy = new TimeoutRetryPolicy();
        policy.setTimeout(30000L);
        
        template.setRetryPolicy(policy);
        
        final ResponseEntity<JobExecutionResource> getEntityExecution = template
                .execute((RetryCallback<ResponseEntity<JobExecutionResource>, IllegalStateException>) context -> {
                    // Do stuff that might fail, e.g. webservice operation
                    final ResponseEntity<JobExecutionResource> getEntityExecution1 = getRestTemplate().getForEntity(
                        JobServiceIT.this.endpointJobExecution, JobExecutionResource.class, params);
                    final JobExecutionResource getExecution = getEntityExecution1.getBody();
                    assertJobExecution(getExecution);
                    if (!"COMPLETED".equals(getExecution.getStatus())) {
                        JobServiceIT.this.log.debug("Incomplete job, trying again...");
                        throw new IllegalStateException("Incomplete job");
                    }
                    return getEntityExecution1;
                });
        //end retry
        
        final JobExecutionResource getExecution = getEntityExecution.getBody();
        assertJobExecution(getExecution);
        
        final ResponseEntity<JobInstanceResource> getEntityInstance = getRestTemplate().getForEntity(this.endpointJob,
            JobInstanceResource.class, params);
        assertOk(getEntityInstance);
        assertJobInstance(getEntityInstance.getBody());

        assertEquals(postExecution.getExecutionId(), getExecution.getExecutionId());
        
        //Check alternate endpoint
        final ResponseEntity<JobExecutionResource> getEntityExecutionAlt = getRestTemplate().getForEntity(
            this.endpointJobExecutionAlternative, JobExecutionResource.class, params);
        final JobExecutionResource getExecutionAlt = getEntityExecutionAlt.getBody();
        assertJobExecution(getExecution);
        assertEquals(postExecution.getExecutionId(), getExecutionAlt.getExecutionId());
    }
    
    private void assertJobInstance(final JobInstanceResource instance) {
        Assert.assertNotNull(instance.getInstanceId());
        assertEquals(ExampleApplicationWithJobService.EXAMPLE_JOB_NAME, instance.getName());
    }
    
    private void assertOk(final ResponseEntity<?> entity) {
        assertEquals(entity.getStatusCode(), HttpStatus.OK);
    }
    
    private void assertJobExecution(final JobExecutionResource execution) {
        Assert.assertNotNull(execution);
        Assert.assertNotNull(execution.getExecutionId());
        Assert.assertNotNull(execution.getJobInstanceResource().getInstanceId());
    }
}
