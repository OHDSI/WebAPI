package org.ohdsi.webapi.cohortdefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CleanupCohortTaskletTest extends AbstractDatabaseTest {

  private static final String RESULTS_SCHEMA = "cleanup_cohort_results";
  private static final String[] COHORT_TABLES = {
    "cohort", "cohort_inclusion", "cohort_inclusion_result", "cohort_inclusion_stats",
    "cohort_summary_stats", "cohort_censor_stats", "heracles_heel_results",
    "heracles_results", "heracles_results_dist"
  };
  private static final String[] IR_TABLES = {"ir_analysis_dist", "ir_analysis_result"};

  @Autowired
  private CohortDefinitionService cohortDefinitionService;

  @Before
  public void createResultsSchema() {
    jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + RESULTS_SCHEMA);
    for (String table : COHORT_TABLES) {
      jdbcTemplate.execute(String.format("CREATE TABLE %s.%s (cohort_definition_id INT)", RESULTS_SCHEMA, table));
    }
    for (String table : IR_TABLES) {
      jdbcTemplate.execute(String.format("CREATE TABLE %s.%s (target_id INT, outcome_id INT)", RESULTS_SCHEMA, table));
    }
    jdbcTemplate.execute(String.format("INSERT INTO %s.cohort VALUES (1), (2)", RESULTS_SCHEMA));
  }

  @After
  public void dropResultsSchema() {
    jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + RESULTS_SCHEMA + " CASCADE");
  }

  @Test
  public void deletesResultsWhenCredentialsAreNotInConnectionString() throws Exception {

    // The connection string carries no user or password: they are stored separately on the source,
    // as they are for Redshift and Snowflake sources configured through the Atlas UI.
    Source source = new Source();
    source.setSourceId(-2334);
    source.setSourceKey("CLEANUP_TEST");
    source.setSourceName("Cleanup test source");
    source.setSourceDialect("postgresql");
    source.setSourceConnection(String.format("jdbc:postgresql://localhost:%d/postgres", pg.getEmbeddedPostgres().getPort()));
    source.setUsername("postgres");
    source.setPassword("postgres");
    SourceDaimon resultsDaimon = new SourceDaimon(source);
    resultsDaimon.setDaimonType(SourceDaimon.DaimonType.Results);
    resultsDaimon.setTableQualifier(RESULTS_SCHEMA);
    resultsDaimon.setPriority(0);
    source.setDaimons(Collections.singletonList(resultsDaimon));

    SourceRepository sourceRepository = mock(SourceRepository.class);
    when(sourceRepository.findAll()).thenReturn(Collections.singletonList(source));

    ChunkContext chunkContext = mock(ChunkContext.class, RETURNS_DEEP_STUBS);
    when(chunkContext.getStepContext().getJobParameters()).thenReturn(Map.of("cohort_definition_id", "1"));

    CleanupCohortTasklet tasklet = new CleanupCohortTasklet(
            cohortDefinitionService.getTransactionTemplateNoTransaction(), sourceRepository,
            cohortDefinitionService::getSourceJdbcTemplate);
    tasklet.execute(mock(StepContribution.class), chunkContext);

    assertThat(jdbcTemplate.queryForList(
            String.format("SELECT cohort_definition_id FROM %s.cohort", RESULTS_SCHEMA), Integer.class))
            .containsExactly(2);
  }
}
