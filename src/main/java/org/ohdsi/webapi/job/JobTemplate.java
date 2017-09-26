package org.ohdsi.webapi.job;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.step.tasklet.Tasklet;

/**
 *
 */
public class JobTemplate {
    
    private static final Log log = LogFactory.getLog(JobTemplate.class);
    
    private final JobLauncher jobLauncher;
    
    private final JobBuilderFactory jobBuilders;
    
    private final StepBuilderFactory stepBuilders;
    
    public JobTemplate(final JobLauncher jobLauncher, final JobBuilderFactory jobBuilders,
        final StepBuilderFactory stepBuilders) {
        this.jobLauncher = jobLauncher;
        this.jobBuilders = jobBuilders;
        this.stepBuilders = stepBuilders;
    }
    
    public JobExecutionResource launch(final Job job, JobParameters jobParameters) throws WebApplicationException {
        JobExecution exec = null;
        try {
            jobParameters = new JobParametersBuilder(jobParameters).addLong("time", System.currentTimeMillis())
                .toJobParameters();          
          exec = this.jobLauncher.run(job, jobParameters);
          if (log.isDebugEnabled()) {
              log.debug("JobExecution queued: " + exec);
          }
        } catch (final JobExecutionAlreadyRunningException e) {
            throw new WebApplicationException(Response.status(Status.CONFLICT).entity(whitelist(e)).build());
        } catch (final Exception e) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(whitelist(e)).build());
        }
        return JobUtils.toJobExecutionResource(exec);
    }
    
    public JobExecutionResource launchTasklet(final String jobName, final String stepName, final Tasklet tasklet,
                                              JobParameters jobParameters) throws WebApplicationException {
        JobExecution exec;
        try {
            //TODO Consider JobParametersIncrementer
            jobParameters = new JobParametersBuilder(jobParameters).addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            //TODO Consider our own check (since adding unique JobParameter) to see if related-job is running and throw "already running"
            final Step step = this.stepBuilders.get(stepName).tasklet(tasklet).allowStartIfComplete(true).build();
            final Job job = this.jobBuilders.get(jobName).start(step).build();
            exec = this.jobLauncher.run(job, jobParameters);
        } catch (final JobExecutionAlreadyRunningException e) {
            throw new WebApplicationException(Response.status(Status.CONFLICT).entity(whitelist(e.getMessage())).build());
        } catch (final JobInstanceAlreadyCompleteException e) {
            throw new WebApplicationException(Response.status(Status.CONFLICT).entity(whitelist(e.getMessage())).build());
        } catch (final Exception e) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(whitelist(e.getMessage())).build());
        }
        return JobUtils.toJobExecutionResource(exec);
    }
}
