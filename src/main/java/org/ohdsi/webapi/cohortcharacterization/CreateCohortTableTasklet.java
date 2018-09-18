package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
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

  public CreateCohortTableTasklet(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

    transactionTemplate.execute(transactionStatus -> doTask(chunkContext));
    return RepeatStatus.FINISHED;
  }

  private Object doTask(ChunkContext chunkContext) {

    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    String targetDialect = jobParameters.get(TARGET_DIALECT).toString();
    String targetTable = jobParameters.get(TARGET_TABLE).toString();
    String resultsSchema = jobParameters.get(RESULTS_DATABASE_SCHEMA).toString();

    String sql = SqlRender.renderSql(CREATE_COHORT_SQL,
            new String[]{ RESULTS_DATABASE_SCHEMA, TARGET_TABLE },
            new String[] { resultsSchema, targetTable });
    String translatedSql = SqlTranslate.translateSql(sql, targetDialect);
    jdbcTemplate.execute(translatedSql);

    return null;
  }
}
