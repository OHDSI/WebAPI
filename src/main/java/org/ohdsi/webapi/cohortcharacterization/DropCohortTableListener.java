package org.ohdsi.webapi.cohortcharacterization;

import com.odysseusinc.arachne.commons.types.DBMSType;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

public class DropCohortTableListener extends JobExecutionListenerSupport {

  private final String DROP_TABLE_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/dropCohortTable.sql");
  
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final SourceService sourceService;
  private final SourceAwareSqlRender sourceAwareSqlRender;

  public DropCohortTableListener(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, SourceService sourceService, SourceAwareSqlRender sourceAwareSqlRender) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.sourceService = sourceService;
    this.sourceAwareSqlRender = sourceAwareSqlRender;
  }

  private Object doTask(JobParameters parameters) {

    final Map<String, JobParameter> jobParameters = parameters.getParameters();
    final Integer sourceId = Integer.valueOf(jobParameters.get(SOURCE_ID).toString());
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

  @Override
  public void afterJob(JobExecution jobExecution) {

    transactionTemplate.execute(transactionStatus -> doTask(jobExecution.getJobParameters()));
  }
}
