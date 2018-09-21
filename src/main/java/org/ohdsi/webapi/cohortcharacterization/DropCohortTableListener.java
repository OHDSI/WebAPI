package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.*;

public class DropCohortTableListener extends JobExecutionListenerSupport {

  private final String DROP_TABLE_SQL = "DROP TABLE @results_database_schema.@target_table;";
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final SourceService sourceService;

  public DropCohortTableListener(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, SourceService sourceService) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.sourceService = sourceService;
  }

  private Object doTask(JobParameters parameters) {

    Map<String, JobParameter> jobParameters = parameters.getParameters();
    Source source = sourceService.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
    String targetTable = jobParameters.get(TARGET_TABLE).getValue().toString();
    String targetDialect = source.getSourceDialect();
    String resultsSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    String sql = SqlRender.renderSql(DROP_TABLE_SQL, new String[] { RESULTS_DATABASE_SCHEMA, TARGET_TABLE },
            new String[] { resultsSchema, targetTable });
    String translatedSql = SqlTranslate.translateSql(sql, targetDialect);
    jdbcTemplate.execute(translatedSql);

    return null;
  }

  @Override
  public void afterJob(JobExecution jobExecution) {

    transactionTemplate.execute(transactionStatus -> doTask(jobExecution.getJobParameters()));
  }
}
