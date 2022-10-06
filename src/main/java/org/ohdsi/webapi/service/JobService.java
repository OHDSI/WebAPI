package org.ohdsi.webapi.service;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobInstanceResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.job.JobUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * REST Services related to working with the Spring Batch jobs
 * 
 * @summary Jobs
 */
@Path("/job/")
@Component
public class JobService extends AbstractDaoService {

  private final JobExplorer jobExplorer;

  private final SearchableJobExecutionDao jobExecutionDao;

  private final JobRepository jobRepository;

  private final JobTemplate jobTemplate;

  private Map<Long, Job> jobMap = new HashMap<>();

  public JobService(JobExplorer jobExplorer, SearchableJobExecutionDao jobExecutionDao, JobRepository jobRepository, JobTemplate jobTemplate) {

    this.jobExplorer = jobExplorer;
    this.jobExecutionDao = jobExecutionDao;
    this.jobRepository = jobRepository;
    this.jobTemplate = jobTemplate;
  }

  /**
   * Get the job information by job ID
   * 
   * @summary Get job by ID
   * @param jobId The job ID
   * @return The job information
   */
  @GET
  @Path("{jobId}")
  @Produces(MediaType.APPLICATION_JSON)
  public JobInstanceResource findJob(@PathParam("jobId") final Long jobId) {
    final JobInstance job = this.jobExplorer.getJobInstance(jobId);
    if (job == null) {
      return null;//TODO #8 conventions under review
    }
    return JobUtils.toJobInstanceResource(job);
  }

  /**
   * Get the job execution information by job type and name
   * 
   * @summary Get job by name and type
   * @param jobName The job name
   * @param jobType The job type
   * @return JobExecutionResource
   */
    @GET
    @Path("/type/{jobType}/name/{jobName}")
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource findJobByName(@PathParam("jobName") final String jobName, @PathParam("jobType") final String jobType) {
            final Optional<JobExecution> jobExecution = jobExplorer.findRunningJobExecutions(jobType).stream()
                    .filter(job -> jobName.equals(job.getJobParameters().getString(Constants.Params.JOB_NAME)))
                    .findFirst();
            return jobExecution.isPresent() ? JobUtils.toJobExecutionResource(jobExecution.get()) : null;
    }

    /**
     * Get the job execution information by execution ID and job ID
     * 
     * @summary Get job by job ID and execution ID
     * @param jobId The job ID
     * @param executionId The execution ID
     * @return JobExecutionResource
     */
  @GET
  @Path("{jobId}/execution/{executionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public JobExecutionResource findJobExecution(@PathParam("jobId") final Long jobId,
          @PathParam("executionId") final Long executionId) {
    return service(jobId, executionId);
  }

  /**
   * Find job execution by execution ID
   *
   * @summary Get job by execution ID
   * @param executionId The job execution ID
   * @return JobExecutionResource
   */
  @GET
  @Path("/execution/{executionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public JobExecutionResource findJobExecution(@PathParam("executionId") final Long executionId) {
    return service(null, executionId);
  }

  private JobExecutionResource service(final Long jobId, final Long executionId) {
    final JobExecution exec = this.jobExplorer.getJobExecution(executionId);
    if ((exec == null) || ((jobId != null) && !jobId.equals(exec.getJobId()))) {
      return null;//TODO #8 conventions under review
    }
    return JobUtils.toJobExecutionResource(exec);
  }

  /**
   * Get job names (unique names). Note: this path (GET /job) should really
   * return pages of job instances. This could be implemented should the need
   * arise. See {@link JobService#list(String, Integer, Integer)} to obtain
   * executions and filter by job name.
   *
   * @summary Get list of jobs
   * @return A list of jobs
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> findJobNames() {
    return this.jobExplorer.getJobNames();
  }

  /**
   * <i>Variation of spring-batch-admin support:
   * org.springframework.batch.admin.web.BatchJobExecutionsController</i>.
   * <p>
   * Return a paged collection of job executions. Filter for a given job.
   * Returned in pages.
   *
   * @summary Get job executions with filters
   * @param jobName name of the job
   * @param pageIndex start index for the job execution list
   * @param pageSize page size for the list
   * @param comprehensivePage boolean if true returns a comprehensive resultset
   * as a page (i.e. pageRequest(0,resultset.size()))
   * @return collection of JobExecutionInfo
   * @throws NoSuchJobException
   */
  @GET
  @Path("/execution")
  @Produces(MediaType.APPLICATION_JSON)
  public Page<JobExecutionResource> list(@QueryParam("jobName") final String jobName,
          @DefaultValue("0") @QueryParam("pageIndex") final Integer pageIndex,
          @DefaultValue("20") @QueryParam("pageSize") final Integer pageSize,
          @QueryParam("comprehensivePage") boolean comprehensivePage)
          throws NoSuchJobException {

    List<JobExecutionResource> resources = null;

    if (comprehensivePage) {
      String sqlPath = "/resources/job/sql/jobExecutions.sql";
      String tqName = "ohdsi_schema";
      String tqValue = getOhdsiSchema();
      PreparedStatementRenderer psr = new PreparedStatementRenderer(null, sqlPath, tqName, tqValue);
      resources = getJdbcTemplate().query(psr.getSql(), psr.getSetter(), new ResultSetExtractor<List<JobExecutionResource>>() {
        @Override
        public List<JobExecutionResource> extractData(ResultSet rs) throws SQLException, DataAccessException {

          return JobUtils.toJobExecutionResource(rs);
        }
      });
      return new PageImpl<>(resources, new PageRequest(0, pageSize), resources.size());
    } else {
      resources = new ArrayList<>();
      for (final JobExecution jobExecution : (jobName == null ? this.jobExecutionDao.getJobExecutions(pageIndex,
              pageSize) : this.jobExecutionDao.getJobExecutions(jobName, pageIndex, pageSize))) {
        resources.add(JobUtils.toJobExecutionResource(jobExecution));
      }
      return new PageImpl<>(resources, new PageRequest(pageIndex, pageSize),
              this.jobExecutionDao.countJobExecutions());
    }

  }

  public void stopJob(JobExecution jobExecution, Job job) {

    if (Objects.nonNull(job)) {
        jobExecution.getStepExecutions().stream()
          .filter(step -> step.getStatus().isRunning())
          .forEach(stepExec -> {
            Step step = ((StepLocator) job).getStep(stepExec.getStepName());
            if (step instanceof TaskletStep) {
              Tasklet tasklet = ((TaskletStep) step).getTasklet();
              if (tasklet instanceof StoppableTasklet) {
                StepSynchronizationManager.register(stepExec);
                ((StoppableTasklet) tasklet).stop();
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

    JobExecutionResource jobExecution = this.jobTemplate.launch(job, jobParameters);
    jobMap.put(jobExecution.getExecutionId(), job);
    return jobExecution;
  }

  @Transactional
  public void cancelJobExecution(Predicate<? super JobExecution> filterPredicate) {
      jobExecutionDao.getRunningJobExecutions().stream()
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
