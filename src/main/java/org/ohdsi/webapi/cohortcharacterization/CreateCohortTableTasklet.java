package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.TARGET_TABLE;

public class CreateCohortTableTasklet implements Tasklet {

  private final String CREATE_COHORT_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/createCohortTable.sql");

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final SourceService sourceService;
  private final SourceAwareSqlRender sourceAwareSqlRender;

  public CreateCohortTableTasklet(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, SourceService sourceService, SourceAwareSqlRender sourceAwareSqlRender) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.sourceService = sourceService;
    this.sourceAwareSqlRender = sourceAwareSqlRender;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

    transactionTemplate.execute(transactionStatus -> doTask(chunkContext));
    return RepeatStatus.FINISHED;
  }

  private Object doTask(ChunkContext chunkContext) {

    final Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    final Integer sourceId = Integer.valueOf(jobParameters.get(SOURCE_ID).toString());
    final String targetTable = jobParameters.get(TARGET_TABLE).toString();
    final String sql = sourceAwareSqlRender.renderSql(sourceId, CREATE_COHORT_SQL, TARGET_TABLE, targetTable );

    final Source source = sourceService.findBySourceId(sourceId);
    final String resultsQualifier = SourceUtils.getResultsQualifier(source);
    final String tempQualifier = SourceUtils.getTempQualifier(source, resultsQualifier);
    final String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect(), null, tempQualifier);
    Arrays.stream(SqlSplit.splitSql(translatedSql)).forEach(jdbcTemplate::execute);

    return null;
  }
}
