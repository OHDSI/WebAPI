package org.ohdsi.webapi.cohortcharacterization;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

public class DropCohortTableListenerTest {

  private static final String DROP_TARGET = "DROP TABLE temp.cohort_abc";
  private static final String CLEANUP_SQL = "DROP TABLE IF EXISTS temp.cf_abc;\nDROP TABLE IF EXISTS temp.sc_abc_1;";

  private JdbcTemplate jdbcTemplate;
  private DropCohortTableListener listener;

  @Before
  public void setUp() {
    jdbcTemplate = mock(JdbcTemplate.class);
    SourceService sourceService = mock(SourceService.class);
    SourceAwareSqlRender sqlRender = mock(SourceAwareSqlRender.class);
    Source source = mock(Source.class);
    when(source.getSourceDialect()).thenReturn("postgresql");
    when(sourceService.findBySourceId(5)).thenReturn(source);
    when(sqlRender.renderSql(anyInt(), anyString(), eq(TARGET_TABLE), anyString())).thenReturn(DROP_TARGET);
    listener = new DropCohortTableListener(jdbcTemplate, sourceService, sqlRender);
  }

  private JobExecution jobExecution() {
    JobParameters params = new JobParametersBuilder()
            .addString(SOURCE_ID, "5")
            .addString(TARGET_TABLE, "cohort_abc")
            .toJobParameters();
    return new JobExecution(1L, params);
  }

  @Test
  public void afterJob_withoutCleanupSql_dropsOnlyTargetTable() {
    listener.afterJob(jobExecution());

    verify(jdbcTemplate, times(1)).execute(anyString());
    verify(jdbcTemplate).execute(DROP_TARGET);
  }

  @Test
  public void afterJob_runsEachCleanupStatementBeforeDroppingTargetTable() {
    JobExecution execution = jobExecution();
    execution.getExecutionContext().putString(DropCohortTableListener.CLEANUP_SQL, CLEANUP_SQL);

    listener.afterJob(execution);

    InOrder order = inOrder(jdbcTemplate);
    order.verify(jdbcTemplate).execute("DROP TABLE IF EXISTS temp.cf_abc");
    order.verify(jdbcTemplate).execute("DROP TABLE IF EXISTS temp.sc_abc_1");
    order.verify(jdbcTemplate).execute(DROP_TARGET);
  }

  @Test
  public void afterJob_failedCleanupStatementDoesNotStopRemainingDrops() {
    JobExecution execution = jobExecution();
    execution.getExecutionContext().putString(DropCohortTableListener.CLEANUP_SQL, CLEANUP_SQL);
    doThrow(new DataAccessResourceFailureException("boom")).when(jdbcTemplate).execute("DROP TABLE IF EXISTS temp.cf_abc");

    listener.afterJob(execution);

    verify(jdbcTemplate).execute("DROP TABLE IF EXISTS temp.sc_abc_1");
    verify(jdbcTemplate).execute(DROP_TARGET);
  }
}
