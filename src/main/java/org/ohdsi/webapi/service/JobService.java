package org.ohdsi.webapi.service;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobInstanceResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.job.JobUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

@Path("/job/")
@Component
public class JobService {

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final JobTemplate jobTemplate;

    private final Map<Long, Job> jobMap = new HashMap<>();

    public JobService(JobExplorer jobExplorer, JobRepository jobRepository, JobTemplate jobTemplate) {
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobTemplate = jobTemplate;
    }

    @GET
    @Path("{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobInstanceResource findJob(@PathParam("jobId") final Long jobId) {
        JobInstance job = jobExplorer.getJobInstance(jobId);
        return job == null ? null : JobUtils.toJobInstanceResource(job);
    }

    @GET
    @Path("/type/{jobType}/name/{jobName}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource findJobByName(@PathParam("jobName") String jobName, @PathParam("jobType") String jobType) {
        Optional<JobExecution> jobExecution = jobExplorer.findRunningJobExecutions(jobType).stream()
            .filter(job -> jobName.equals(job.getJobParameters().getString(Constants.Params.JOB_NAME)))
            .findFirst();
        return jobExecution.map(JobUtils::toJobExecutionResource).orElse(null);
    }

    @GET
    @Path("{jobId}/execution/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource findJobExecution(@PathParam("jobId") Long jobId, @PathParam("executionId") Long executionId) {
        return service(jobId, executionId);
    }

    @GET
    @Path("/execution/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource findJobExecutionResource(@PathParam("executionId") Long executionId) {
        return service(null, executionId);
    }

    public JobExecution findJobExecution(@PathParam("executionId") Long executionId) {
        JobExecution exec = jobExplorer.getJobExecution(executionId);
        return exec;
    }


    private JobExecutionResource service(Long jobId, Long executionId) {
        JobExecution exec = jobExplorer.getJobExecution(executionId);
        if (exec == null || (jobId != null && !jobId.equals(exec.getJobId()))) {
            return null;
        }
        return JobUtils.toJobExecutionResource(exec);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> findJobNames() {
        return jobExplorer.getJobNames();
    }

    @GET
    @Path("/execution")
    @Produces(MediaType.APPLICATION_JSON)
    public Page<JobExecutionResource> list(
        @QueryParam("jobName") String jobName,
        @DefaultValue("0") @QueryParam("pageIndex") Integer pageIndex,
        @DefaultValue("20") @QueryParam("pageSize") Integer pageSize
    ) throws NoSuchJobException {
        List<JobExecutionResource> resources = new ArrayList<>();
        int offset = pageIndex * pageSize;

        if (jobName == null) {
            // Get all job names and fetch job instances and executions
            List<String> jobNames = jobExplorer.getJobNames();
            for (String name : jobNames) {
                List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName(name, 0, Integer.MAX_VALUE);
                for (JobInstance instance : jobInstances) {
                    resources.addAll(fetchJobExecutionResources(instance));
                }
            }
        } else {
            // Fetch job instances and executions for the given job name
            List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName(jobName, offset, pageSize);
            for (JobInstance instance : jobInstances) {
                resources.addAll(fetchJobExecutionResources(instance));
            }
        }

        // Create a paginated result
        int totalSize = resources.size();
        int endIndex = Math.min(offset + pageSize, totalSize);
        List<JobExecutionResource> paginatedResources = resources.subList(offset, endIndex);
        return new PageImpl<>(paginatedResources, PageRequest.of(pageIndex, pageSize), totalSize);
    }

    /**
     * Fetches job execution resources for a given job instance.
     */
    private List<JobExecutionResource> fetchJobExecutionResources(JobInstance jobInstance) {
        List<JobExecutionResource> resources = new ArrayList<>();
        List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
        for (JobExecution execution : executions) {
            resources.add(JobUtils.toJobExecutionResource(execution));
        }
        return resources;
    }


    public void stopJob(JobExecution jobExecution, Job job) {
        if (Objects.nonNull(job)) {
            jobExecution.getStepExecutions().stream()
                .filter(step -> step.getStatus().isRunning())
                .forEach(stepExec -> {
                    Step step = ((StepLocator) job).getStep(stepExec.getStepName());
                    if (step instanceof TaskletStep taskletStep) {
                        Tasklet tasklet = taskletStep.getTasklet();
                        if (tasklet instanceof StoppableTasklet stoppableTasklet) {
                            StepSynchronizationManager.register(stepExec);
                            stoppableTasklet.stop();
                            StepSynchronizationManager.release();
                        }
                    }
                });
        }
        if (jobExecution.getEndTime() == null) {
            jobExecution.setStatus(BatchStatus.STOPPING);
            jobRepository.update(jobExecution);
        }
    }

    public JobExecution getJobExecution(Long jobExecutionId) {
        return jobExplorer.getJobExecution(jobExecutionId);
    }

    public Job getRunningJob(Long jobExecutionId) {
        return jobMap.get(jobExecutionId);
    }

    public void removeJob(Long jobExecutionId) {
        jobMap.remove(jobExecutionId);
    }

    public JobExecutionResource runJob(Job job, JobParameters jobParameters) {
        JobExecutionResource jobExecution = jobTemplate.launch(job, jobParameters);
        jobMap.put(jobExecution.getExecutionId(), job);
        return jobExecution;
    }

    @Transactional
    public void cancelJobExecution(Predicate<? super JobExecution> filterPredicate) {
        jobExplorer.getJobNames().stream()
            .flatMap(jobName -> jobExplorer.findRunningJobExecutions(jobName).stream())
            .filter(filterPredicate)
            .findFirst()
            .ifPresent(jobExecution -> {
                Job job = getRunningJob(jobExecution.getJobId());
                if (Objects.nonNull(job)) {
                    stopJob(jobExecution, job);
                }
            });
    }

}
