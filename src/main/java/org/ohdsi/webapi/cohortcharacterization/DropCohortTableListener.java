package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.arachne.commons.types.DBMSType;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

public class DropCohortTableListener extends JobExecutionListenerSupport {

  /** Job execution context key holding translated SQL that drops analysis-specific intermediate tables. */
  public static final String CLEANUP_SQL = "analysisCleanupSql";

  private static final Logger LOGGER = LoggerFactory.getLogger(DropCohortTableListener.class);

  private final String DROP_TABLE_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/dropCohortTable.sql");
  
  private final JdbcTemplate jdbcTemplate;
  private final SourceService sourceService;
  private final SourceAwareSqlRender sourceAwareSqlRender;

  public DropCohortTableListener(JdbcTemplate jdbcTemplate, SourceService sourceService, SourceAwareSqlRender sourceAwareSqlRender) {
    this.jdbcTemplate = jdbcTemplate;
    this.sourceService = sourceService;
    this.sourceAwareSqlRender = sourceAwareSqlRender;
  }

  private Object doTask(JobParameters parameters) {

    final Map<String, JobParameter<?>> jobParameters = parameters.getParameters();
    final Integer sourceId = Integer.valueOf(jobParameters.get(SOURCE_ID).getValue().toString());
    final String targetTable = jobParameters.get(TARGET_TABLE).getValue().toString();
    final String sql = sourceAwareSqlRender.renderSql(sourceId, DROP_TABLE_SQL, TARGET_TABLE, targetTable );

    final Source source = sourceService.findBySourceId(sourceId);
    final String resultsQualifier = SourceUtils.getResultsQualifier(source);
    final String tempQualifier = SourceUtils.getTempQualifier(source, resultsQualifier);
    String toRemove = SqlTranslate.translateSql(sql, source.getSourceDialect(), null, tempQualifier);

    if (Objects.equals(DBMSType.SPARK.getOhdsiDB(), source.getSourceDialect()) ||
            Objects.equals(DBMSType.HIVE.getOhdsiDB(), source.getSourceDialect())) {
      toRemove = StringUtils.remove(toRemove, ';');
    }
    jdbcTemplate.execute(toRemove);
    return null;
  }

  private void dropAnalysisTables(JobExecution jobExecution) {

    final ExecutionContext context = jobExecution.getExecutionContext();
    if (!context.containsKey(CLEANUP_SQL)) {
      return;
    }
    final Integer sourceId = Integer.valueOf(jobExecution.getJobParameters().getParameters().get(SOURCE_ID).getValue().toString());
    final Source source = sourceService.findBySourceId(sourceId);
    final boolean stripSemicolon = Objects.equals(DBMSType.SPARK.getOhdsiDB(), source.getSourceDialect()) ||
            Objects.equals(DBMSType.HIVE.getOhdsiDB(), source.getSourceDialect());
    for (String statement : SqlSplit.splitSql(context.getString(CLEANUP_SQL))) {
      try {
        jdbcTemplate.execute(stripSemicolon ? StringUtils.remove(statement, ';') : statement);
      } catch (RuntimeException e) {
        LOGGER.warn("Failed to drop intermediate table for job {}: {}", jobExecution.getId(), statement, e);
      }
    }
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    // No transaction wrapper needed - sourceService.findBySourceId() has @Transactional
    // and DROP TABLE is DDL that auto-commits
    dropAnalysisTables(jobExecution);
    doTask(jobExecution.getJobParameters());
  }
}
