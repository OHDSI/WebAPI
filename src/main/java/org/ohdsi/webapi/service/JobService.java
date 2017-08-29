package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobInstanceResource;
import org.ohdsi.webapi.job.JobUtils;
import org.springframework.batch.admin.service.SearchableJobExecutionDao;
import org.springframework.batch.admin.service.SearchableJobInstanceDao;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
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
      String sql_statement = ResourceHelper.GetResourceAsString("/resources/job/sql/jobExecutions.sql");
      sql_statement = SqlRender.renderSql(sql_statement, new String[]{"ohdsi_schema"},
              new String[]{this.getOhdsiSchema()});
      sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
      log.debug("Translated sql:" + sql_statement);

      resources = getJdbcTemplate().query(sql_statement, new ResultSetExtractor<List<JobExecutionResource>>() {

        @Override
        public List<JobExecutionResource> extractData(ResultSet rs) throws SQLException, DataAccessException {
          return JobUtils.toJobExecutionResource(rs);
        }
      });

      return new PageImpl<JobExecutionResource>(resources, new PageRequest(0, pageSize), resources.size());
    } else {
      resources = new ArrayList<JobExecutionResource>();
      for (final JobExecution jobExecution : (jobName == null ? this.jobExecutionDao.getJobExecutions(pageIndex,
              pageSize) : this.jobExecutionDao.getJobExecutions(jobName, pageIndex, pageSize))) {
        resources.add(JobUtils.toJobExecutionResource(jobExecution));
      }
      return new PageImpl<JobExecutionResource>(resources, new PageRequest(pageIndex, pageSize),
              this.jobExecutionDao.countJobExecutions());
    }

  }
}
