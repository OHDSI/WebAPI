package org.ohdsi.webapi.test;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.util.Assert;

/**
 *
 */
public class JobServiceIT extends WebApiIT {
    
    @Value("${exampleservice.endpoint}")
    private String endpointExample;
    
    @Value("${jobservice.endpoint.job}")
    private String endpointJob;
    
    @Value("${jobservice.endpoint.jobexecution}")
    private String endpointJobExecution;
    
    @Value("${jobservice.endpoint.jobexecution.alt}")
    private String endpointJobExecutionAlternative;
    
    @Test
    public void createAndFindJob() {
        //create/queue job
        final ResponseEntity<JobExecutionResource> postEntity = getRestTemplate().postForEntity(this.endpointExample, null,
            JobExecutionResource.class);//TODO 409 or other errors prevent deserialization...
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
                .execute(new RetryCallback<ResponseEntity<JobExecutionResource>, IllegalStateException>() {
                    
                    @Override
                    public ResponseEntity<JobExecutionResource> doWithRetry(final RetryContext context) {
                        // Do stuff that might fail, e.g. webservice operation
                        final ResponseEntity<JobExecutionResource> getEntityExecution = getRestTemplate().getForEntity(
                            JobServiceIT.this.endpointJobExecution, JobExecutionResource.class, params);
                        final JobExecutionResource getExecution = getEntityExecution.getBody();
                        assertJobExecution(getExecution);
                        if (!"COMPLETED".equals(getExecution.getStatus())) {
                            JobServiceIT.this.log.debug("Incomplete job, trying again...");
                            throw new IllegalStateException("Incomplete job");
                        }
                        return getEntityExecution;
                    }
                    
                });
        //end retry
        
        final JobExecutionResource getExecution = getEntityExecution.getBody();
        assertJobExecution(getExecution);
        
        final ResponseEntity<JobInstanceResource> getEntityInstance = getRestTemplate().getForEntity(this.endpointJob,
            JobInstanceResource.class, params);
        assertOk(getEntityInstance);
        assertJobInstance(getEntityInstance.getBody());
        
        Assert.state(postExecution.getExecutionId().equals(getExecution.getExecutionId()));
        
        //Check alternate endpoint
        final ResponseEntity<JobExecutionResource> getEntityExecutionAlt = getRestTemplate().getForEntity(
            this.endpointJobExecutionAlternative, JobExecutionResource.class, params);
        final JobExecutionResource getExecutionAlt = getEntityExecutionAlt.getBody();
        assertJobExecution(getExecution);
        Assert.state(postExecution.getExecutionId().equals(getExecutionAlt.getExecutionId()));
    }
    
    private void assertJobInstance(final JobInstanceResource instance) {
        Assert.state(instance.getInstanceId() != null);
        Assert.state(ExampleApplicationWithJobService.EXAMPLE_JOB_NAME.equals(instance.getName()));
    }
    
    private void assertOk(final ResponseEntity<?> entity) {
        Assert.state(entity.getStatusCode() == HttpStatus.OK);
    }
    
    private void assertJobExecution(final JobExecutionResource execution) {
        Assert.state(execution != null && execution.getExecutionId() != null
                && execution.getJobInstanceResource().getInstanceId() != null);
    }
}
