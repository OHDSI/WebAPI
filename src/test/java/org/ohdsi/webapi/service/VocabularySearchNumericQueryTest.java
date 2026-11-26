package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

/**
 * Runs the vocabulary search query against the embedded Postgres instance with numeric search
 * terms. A numeric term is also matched against concept_id, which is an INT, so a term longer
 * than an INT (for example an 11-digit NDC code) must still be searched by name and code.
 */
public class VocabularySearchNumericQueryTest extends AbstractDatabaseTest {

  private static final String VOCAB_SCHEMA = "vocab_search";

  private VocabularyService vocabularyService;
  private Source source;

  @Before
  public void setUp() {

    vocabularyService = new VocabularyService();

    source = mock(Source.class);
    when(source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn(VOCAB_SCHEMA);
    when(source.getSourceDialect()).thenReturn("postgresql");

    jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %1$s CASCADE; CREATE SCHEMA %1$s;", VOCAB_SCHEMA));
    jdbcTemplate.execute(String.format("CREATE TABLE %s.concept (concept_id INT, concept_name VARCHAR(255), "
        + "domain_id VARCHAR(20), vocabulary_id VARCHAR(20), concept_class_id VARCHAR(20), "
        + "standard_concept VARCHAR(1), concept_code VARCHAR(50), valid_start_date DATE, valid_end_date DATE, "
        + "invalid_reason VARCHAR(1))", VOCAB_SCHEMA));

    insertConcept(45118733, "Atorvastatin 10 MG Oral Tablet", "NDC", "50090347201");
    insertConcept(201826, "Type 2 diabetes mellitus", "SNOMED", "44054006");
  }

  @After
  public void tearDown() {

    jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", VOCAB_SCHEMA));
  }

  private void insertConcept(int conceptId, String name, String vocabularyId, String code) {

    jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name, domain_id, vocabulary_id, "
        + "concept_class_id, concept_code, valid_start_date, valid_end_date) "
        + "VALUES (?, ?, 'Drug', ?, 'Clinical Drug', ?, DATE '1970-01-01', DATE '2099-12-31')", VOCAB_SCHEMA),
        conceptId, name, vocabularyId, code);
  }

  private List<Long> searchConceptIds(String query) {

    PreparedStatementRenderer psr = vocabularyService.prepareExecuteSearchWithQuery(query, source);
    return jdbcTemplate.query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> rs.getLong("concept_id"));
  }

  @Test
  public void searchFindsConceptByCodeLongerThanAnInt() {

    assertEquals(List.of(45118733L), searchConceptIds("50090347201"));
  }

  @Test
  public void searchWithTwentyDigitQueryReturnsNoRows() {

    assertEquals(List.of(), searchConceptIds("12345678901234567890"));
  }

  @Test
  public void searchStillMatchesNumericQueryAgainstConceptId() {

    assertEquals(List.of(201826L), searchConceptIds("201826"));
    assertEquals(List.of(201826L), searchConceptIds("44054006"));
  }
}
