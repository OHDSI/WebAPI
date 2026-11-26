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
import org.ohdsi.webapi.vocabulary.ConceptSearch;

/**
 * Runs the vocabulary search query against the embedded Postgres instance with a search term that
 * contains a square bracket, as RxNorm branded drug names do ("... Oral Tablet [Lipitor]"). Only
 * SQL Server reads [ as the start of a character class in LIKE, so only SQL Server needs it escaped.
 */
public class VocabularySearchBracketTest extends AbstractDatabaseTest {

  private static final String VOCAB_SCHEMA = "vocab_search_brackets";

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
    jdbcTemplate.execute(String.format("CREATE TABLE %s.concept_synonym (concept_id INT, "
        + "concept_synonym_name VARCHAR(1000), language_concept_id INT)", VOCAB_SCHEMA));
    jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name, domain_id, vocabulary_id, "
        + "concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date) "
        + "VALUES (?, ?, 'Drug', 'RxNorm', 'Branded Drug', 'S', ?, DATE '1970-01-01', DATE '2099-12-31')",
        VOCAB_SCHEMA), 1545999, "atorvastatin 10 MG Oral Tablet [Lipitor]", "617314");
  }

  @After
  public void tearDown() {

    jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", VOCAB_SCHEMA));
  }

  private List<Long> search(String query, boolean lexical) {

    ConceptSearch search = new ConceptSearch();
    search.query = query;
    search.isLexical = lexical;
    PreparedStatementRenderer psr = vocabularyService.prepareExecuteSearch(search, source);
    return jdbcTemplate.query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> rs.getLong("concept_id"));
  }

  @Test
  public void searchFindsConceptNameWithBracket() {

    assertEquals(List.of(1545999L), search("tablet [lipitor]", false));
  }

  @Test
  public void lexicalSearchFindsConceptNameWithBracket() {

    assertEquals(List.of(1545999L), search("[lipitor]", true));
  }

  @Test
  public void sqlServerSearchStillEscapesBracket() {

    Source sqlServerSource = mock(Source.class);
    when(sqlServerSource.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn(VOCAB_SCHEMA);
    when(sqlServerSource.getSourceDialect()).thenReturn("sql server");
    ConceptSearch search = new ConceptSearch();
    search.query = "tablet [lipitor]";

    PreparedStatementRenderer psr = vocabularyService.prepareExecuteSearch(search, sqlServerSource);

    assertEquals("%tablet [[]lipitor]%", psr.getOrderedParamsList().get(0));
  }
}
