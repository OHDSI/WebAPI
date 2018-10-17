package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobInstanceResource;
import org.ohdsi.webapi.job.JobUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.admin.service.SearchableJobInstanceDao;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
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

/**
 *
 */
@Path("/job/")
@Component
public class JobService extends AbstractDaoService {

  @Autowired
  private String batchTablePrefix;

  @Autowired
  private JobExplorer jobExplorer;

  @Autowired
  private JobLocator jobLocator;

  @Autowired
  private SearchableJobExecutionDao jobExecutionDao;

  @Autowired
  private SearchableJobInstanceDao jobInstanceDao;

  @Autowired
  private JobOperator jobOperator;

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

  @GET
  @Path("{jobId}/execution/{executionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public JobExecutionResource findJobExecution(@PathParam("jobId") final Long jobId,
          @PathParam("executionId") final Long executionId) {
    return service(jobId, executionId);
  }

  /**
   * Overloaded findJobExecution method.
   *
   * @param executionId
   * @return
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
   * @return
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
    try {
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
      jobOperator.stop(jobExecution.getJobId());
    } catch (NoSuchJobExecutionException | JobExecutionNotRunningException ignored) {
    }
  }
}
