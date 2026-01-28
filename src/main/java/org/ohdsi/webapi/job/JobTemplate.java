package org.ohdsi.webapi.job;

import org.ohdsi.webapi.shiro.management.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import static org.ohdsi.webapi.Constants.Params.JOB_AUTHOR;
import static org.ohdsi.webapi.Constants.Params.JOB_START_TIME;
import static org.ohdsi.webapi.Constants.SYSTEM_USER;
import static org.ohdsi.webapi.Constants.WARM_CACHE;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

/**
 * Spring Batch 5.x template - JobBuilderFactory and StepBuilderFactory removed
 */
public class JobTemplate {

    private static final Logger log = LoggerFactory.getLogger(JobTemplate.class);

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final Security security;
    private final PlatformTransactionManager transactionManager;

    public JobTemplate(final JobLauncher jobLauncher, final JobRepository jobRepository, final Security security, final PlatformTransactionManager transactionManager) {
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
        this.security = security;
        this.transactionManager = transactionManager;
    }

    public JobExecutionResource launch(final Job job, JobParameters jobParameters) throws ResponseStatusException {
        JobExecution exec;
        try {
            JobParametersBuilder builder = new JobParametersBuilder(jobParameters);
            builder.addLong(JOB_START_TIME, System.currentTimeMillis());
            if (jobParameters.getString(JOB_AUTHOR) == null) {
                builder.addString(JOB_AUTHOR, security.getSubject());
            }
            jobParameters = builder.toJobParameters();
            exec = this.jobLauncher.run(job, jobParameters);
            if (log.isDebugEnabled()) {
                log.debug("JobExecution queued: {}", exec);
            }
        } catch (final JobExecutionAlreadyRunningException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, whitelist(e), e);
        } catch (final Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, whitelist(e), e);
        }
        return JobUtils.toJobExecutionResource(exec);
    }

    public JobExecutionResource launchTasklet(final String jobName, final String stepName, final Tasklet tasklet,
                                              JobParameters jobParameters) throws ResponseStatusException {
        JobExecution exec;
        try {
            jobParameters = new JobParametersBuilder(jobParameters)
                    .addLong(JOB_START_TIME, System.currentTimeMillis())
                    .addString(JOB_AUTHOR, getAuthorForTasklet(jobName))
                    .toJobParameters();
            
            // Spring Batch 5: Use JobBuilder and StepBuilder directly with JobRepository
            final Step step = new StepBuilder(stepName, jobRepository)
                    .tasklet(tasklet, transactionManager)
                    .allowStartIfComplete(true)
                    .build();
                    
            final Job job = new JobBuilder(jobName, jobRepository)
                    .start(step)
                    .build();
                    
            exec = this.jobLauncher.run(job, jobParameters);
        } catch (final JobExecutionAlreadyRunningException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, whitelist(e.getMessage()), e);
        } catch (final JobInstanceAlreadyCompleteException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, whitelist(e.getMessage()), e);
        } catch (final Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, whitelist(e.getMessage()), e);
        }
        return JobUtils.toJobExecutionResource(exec);
    }

    private String getAuthorForTasklet(final String jobName) {
        return WARM_CACHE.equals(jobName) ? SYSTEM_USER : security.getSubject();
    }
}
