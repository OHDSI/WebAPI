package org.ohdsi.webapi.job;

import org.ohdsi.webapi.shiro.management.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import static org.ohdsi.webapi.Constants.Params.JOB_AUTHOR;
import static org.ohdsi.webapi.Constants.Params.JOB_START_TIME;
import static org.ohdsi.webapi.Constants.SYSTEM_USER;
import static org.ohdsi.webapi.Constants.WARM_CACHE;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import javax.sql.DataSource;

public class JobTemplate {

    private static final Logger log = LoggerFactory.getLogger(JobTemplate.class);

    private final JobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilders;
    private final StepBuilderFactory stepBuilders;
    private final Security security;

    @Autowired
    private PlatformTransactionManager transactionManager;
    
    public JobTemplate(JobLauncher jobLauncher, JobBuilderFactory jobBuilders,
                       StepBuilderFactory stepBuilders, Security security) {
        this.jobLauncher = jobLauncher;
        this.jobBuilders = jobBuilders;
        this.stepBuilders = stepBuilders;
        this.security = security;
    }

    public JobExecutionResource launch(Job job, JobParameters jobParameters) throws WebApplicationException {
    	
    	TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return transactionTemplate.execute(status -> {
	        JobExecution exec;
	        try {
	            JobParametersBuilder builder = new JobParametersBuilder(jobParameters);
	            builder.addLong(JOB_START_TIME, System.currentTimeMillis());
	            if (jobParameters.getString(JOB_AUTHOR) == null) {
	                builder.addString(JOB_AUTHOR, security.getSubject());
	            }
	            final JobParameters jobParams = builder.toJobParameters();
	            exec = this.jobLauncher.run(job, jobParams);
	            if (log.isDebugEnabled()) {
	                log.debug("JobExecution queued: {}", exec);
	            }
	        } catch (final JobExecutionAlreadyRunningException e) {
	            throw new WebApplicationException(e, Response.status(Status.CONFLICT).entity(whitelist(e)).build());
	        } catch (final Exception e) {
	            throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR).entity(whitelist(e)).build());
	        }
	        return JobUtils.toJobExecutionResource(exec);
        });
    }

    public JobExecutionResource launchTasklet(String jobName, String stepName, Tasklet tasklet,
                                              JobParameters jobParameters) {
        try {
            jobParameters = new JobParametersBuilder(jobParameters)
                    .addLong("jobStartTime", System.currentTimeMillis())
                    .addString("jobAuthor", getAuthorForTasklet(jobName))
                    .toJobParameters();
            Step step = this.stepBuilders.get(stepName).tasklet(tasklet).build();
            Job job = this.jobBuilders.get(jobName).start(step).build();
            JobExecution execution = this.jobLauncher.run(job, jobParameters);
            return JobUtils.toJobExecutionResource(execution);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
        }
    }

    private String getAuthorForTasklet(String jobName) {
        return "warmCache".equals(jobName) ? "systemUser" : security.getSubject();
    }
}
