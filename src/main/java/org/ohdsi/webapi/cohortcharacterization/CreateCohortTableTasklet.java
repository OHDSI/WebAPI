package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.*;

public class CreateCohortTableTasklet implements Tasklet {

  private final String CREATE_COHORT_SQL = ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/createCohortTable.sql");

  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final SourceService sourceService;

  public CreateCohortTableTasklet(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, SourceService sourceService) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.sourceService = sourceService;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

    transactionTemplate.execute(transactionStatus -> doTask(chunkContext));
    return RepeatStatus.FINISHED;
  }

  private Object doTask(ChunkContext chunkContext) {

    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    Source source = sourceService.findBySourceId(Integer.valueOf(jobParameters.get(SOURCE_ID).toString()));
    String targetTable = jobParameters.get(TARGET_TABLE).toString();

    String sql = SqlRender.renderSql(CREATE_COHORT_SQL,
            new String[]{ RESULTS_DATABASE_SCHEMA, TARGET_TABLE },
            new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results), targetTable });
    String translatedSql = SqlTranslate.translateSql(sql, source.getSourceDialect());
    jdbcTemplate.execute(translatedSql);

    return null;
  }
}
