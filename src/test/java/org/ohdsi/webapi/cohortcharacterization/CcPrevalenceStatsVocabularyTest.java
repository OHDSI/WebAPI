package org.ohdsi.webapi.cohortcharacterization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.sqlrender.SourceAwareSqlRender;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

/**
 * Explores a characterization covariate (ancestors and descendants of its concept) on a source whose
 * vocabulary tables live in a different schema than the CDM tables.
 */
public class CcPrevalenceStatsVocabularyTest extends AbstractDatabaseTest {

  private static final String CDM_SCHEMA = "cc_explore_cdm";
  private static final String VOCABULARY_SCHEMA = "cc_explore_vocab";
  private static final String RESULTS_SCHEMA = "cc_explore_results";
  private static final int SOURCE_ID = 9201;
  private static final long GENERATION_ID = 1;
  private static final long ANALYSIS_ID = 210;
  private static final long COHORT_ID = 5;
  private static final long COVARIATE_ID = 201826210;

  @Autowired
  private ConversionService conversionService;

  @Before
  public void createSchemas() {
    dropSchemas();
    for (String schema : new String[]{CDM_SCHEMA, VOCABULARY_SCHEMA, RESULTS_SCHEMA}) {
      jdbcTemplate.execute("CREATE SCHEMA " + schema);
    }
    // the CDM schema holds clinical tables only; concept and concept_ancestor are in the vocabulary schema
    jdbcTemplate.execute("CREATE TABLE " + CDM_SCHEMA + ".person (person_id BIGINT)");
    jdbcTemplate.execute("CREATE TABLE " + VOCABULARY_SCHEMA + ".concept (concept_id INT, concept_name VARCHAR(255))");
    jdbcTemplate.execute("CREATE TABLE " + VOCABULARY_SCHEMA + ".concept_ancestor (ancestor_concept_id INT, "
            + "descendant_concept_id INT, min_levels_of_separation INT, max_levels_of_separation INT)");
    jdbcTemplate.execute("CREATE TABLE " + RESULTS_SCHEMA + ".cc_results (type VARCHAR(255), fa_type VARCHAR(255), "
            + "cc_generation_id BIGINT, analysis_id INT, analysis_name VARCHAR(1000), covariate_id BIGINT, "
            + "covariate_name VARCHAR(1000), strata_id BIGINT, strata_name VARCHAR(1000), concept_id INT, "
            + "count_value BIGINT, avg_value DOUBLE PRECISION, cohort_definition_id BIGINT)");

    jdbcTemplate.update("INSERT INTO " + VOCABULARY_SCHEMA + ".concept VALUES (201820, 'Diabetes mellitus'), "
            + "(201826, 'Type 2 diabetes mellitus'), (443732, 'Disorder due to type 2 diabetes mellitus')");
    jdbcTemplate.update("INSERT INTO " + VOCABULARY_SCHEMA + ".concept_ancestor VALUES (201820, 201820, 0, 0), "
            + "(201820, 201826, 1, 1), (201820, 443732, 2, 2), (201826, 201826, 0, 0), (201826, 443732, 1, 1), "
            + "(443732, 443732, 0, 0)");
    String result = "INSERT INTO " + RESULTS_SCHEMA + ".cc_results VALUES ('PREVALENCE', 'PRESET', 1, 210, "
            + "'ConditionGroupEraLongTerm', ?, ?, 0, 'All stratas', ?, ?, ?, 5)";
    jdbcTemplate.update(result, 201820210L, "Diabetes mellitus", 201820, 40, 0.4);
    jdbcTemplate.update(result, 201826210L, "Type 2 diabetes mellitus", 201826, 30, 0.3);
    jdbcTemplate.update(result, 443732210L, "Disorder due to type 2 diabetes mellitus", 443732, 10, 0.1);
  }

  /** Builds the service for a source whose daimons point at the given schemas (null = no such daimon). */
  private CcServiceImpl serviceFor(String cdmSchema, String vocabularySchema) {
    Source source = new Source();
    source.setSourceId(SOURCE_ID);
    source.setSourceKey("ccexplore");
    source.setSourceDialect("postgresql");
    List<SourceDaimon> daimons = new ArrayList<>();
    daimons.add(daimon(source, SourceDaimon.DaimonType.CDM, cdmSchema));
    if (vocabularySchema != null) {
      daimons.add(daimon(source, SourceDaimon.DaimonType.Vocabulary, vocabularySchema));
    }
    daimons.add(daimon(source, SourceDaimon.DaimonType.Results, RESULTS_SCHEMA));
    source.setDaimons(daimons);

    CcGenerationEntity generation = mock(CcGenerationEntity.class);
    when(generation.getSource()).thenReturn(source);
    CcGenerationEntityRepository generationRepository = mock(CcGenerationEntityRepository.class);
    when(generationRepository.findById(GENERATION_ID)).thenReturn(Optional.of(generation));
    SourceService sourceService = mock(SourceService.class);
    when(sourceService.findBySourceId(SOURCE_ID)).thenReturn(source);

    CcServiceImpl ccService = spy(new CcServiceImpl(null, null, null, null, null, generationRepository, null,
            conversionService, null, null, null, sourceService, null, new SourceAwareSqlRender(sourceService), null,
            null, null, null, null, null, null, null, null, null));
    doReturn(new CancelableJdbcTemplate(getDataSource())).when(ccService).getSourceJdbcTemplate(source);
    return ccService;
  }

  @After
  public void dropSchemas() {
    for (String schema : new String[]{CDM_SCHEMA, VOCABULARY_SCHEMA, RESULTS_SCHEMA}) {
      jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
    }
  }

  private static SourceDaimon daimon(Source source, SourceDaimon.DaimonType type, String qualifier) {
    SourceDaimon daimon = new SourceDaimon(source);
    daimon.setDaimonType(type);
    daimon.setTableQualifier(qualifier);
    return daimon;
  }

  private static void assertExploredHierarchy(List<CcPrevalenceStat> stats) {
    assertThat(stats)
            .extracting(CcPrevalenceStat::getConceptId, CcPrevalenceStat::getConceptName, CcPrevalenceStat::getDistance)
            .containsExactlyInAnyOrder(
                    tuple(201820L, "Diabetes mellitus", 1L),
                    tuple(201826L, "Type 2 diabetes mellitus", 0L),
                    tuple(443732L, "Disorder due to type 2 diabetes mellitus", -1L));
  }

  @Test
  public void exploreReadsTheConceptHierarchyFromTheVocabularySchema() {
    CcServiceImpl ccService = serviceFor(CDM_SCHEMA, VOCABULARY_SCHEMA);

    assertExploredHierarchy(ccService.getPrevalenceStatsByGenerationId(GENERATION_ID, ANALYSIS_ID, COHORT_ID,
            COVARIATE_ID));
  }

  @Test
  public void exploreFallsBackToTheCdmSchemaWithoutAVocabularyDaimon() {
    // a single schema holding both the CDM and the vocabulary tables, with no Vocabulary daimon configured
    CcServiceImpl ccService = serviceFor(VOCABULARY_SCHEMA, null);

    assertExploredHierarchy(ccService.getPrevalenceStatsByGenerationId(GENERATION_ID, ANALYSIS_ID, COHORT_ID,
            COVARIATE_ID));
  }
}
