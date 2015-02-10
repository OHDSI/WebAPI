package org.ohdsi.webapi.exampleapplication;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Path("/example")
public class ExampleApplicationWithJobService extends AbstractDaoService {
    
    @Autowired
    JobTemplate jobTemplate;
    
    @Autowired
    private Job testJob;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource queueJob() throws Exception {
        //Allow unique combinations of JobParameters to run in parallel.  An empty JobParameters() would only allow a JobInstance to run at a time.
        final JobParameters jobParameters = new JobParametersBuilder().addString("param", "parameter with 250 char limit")
                .toJobParameters();
        return this.jobTemplate.launch(this.testJob, jobParameters);
    }
}
