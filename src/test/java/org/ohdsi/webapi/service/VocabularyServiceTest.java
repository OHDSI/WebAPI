package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.DescendentOfAncestorSearch;
import org.ohdsi.webapi.vocabulary.RelatedConceptSearch;
import org.springframework.beans.factory.annotation.Autowired;

public class VocabularyServiceTest extends AbstractServiceTest {

  @Autowired
  private VocabularyService vocabularyService;

  @Before
  public void before() {

    if (vocabularyService == null) {
      vocabularyService = new VocabularyService();
    }
  }

  @Test
  public void prepareExecuteSearch() throws IOException {

    Source mockSource = mock(Source.class);
    when(mockSource.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn("omop_v5");
    when(mockSource.getSourceDialect()).thenReturn("sql server");

    ConceptSearch search = new ConceptSearch();
    search.query = "conceptABC";

    int domainIdCount = 5;
    search.domainId = new String[domainIdCount];
    for (int i = 0; i < domainIdCount; i++) {
      search.domainId[i] = "domainId" + i;
    }

    int vocabularyIdCount = 10;
    search.vocabularyId = new String[vocabularyIdCount];
    for (int i = 0; i < vocabularyIdCount; i++) {
      search.vocabularyId[i] = "vocabularyId" + i;
    }

    int conceptClassIdCount = 15;
    search.conceptClassId = new String[conceptClassIdCount];
    for (int i = 0; i < conceptClassIdCount; i++) {
      search.conceptClassId[i] = "conceptClassId" + i;
    }

    search.invalidReason = "V";
    search.standardConcept = "Y";

    PreparedStatementRenderer psr = vocabularyService.prepareExecuteSearch(search, mockSource);
    assertSqlEquals("/vocabulary/sql/search-expected-2.sql", psr);
  }

  @Test
  public void prepareExecuteSearchWithQuery() throws IOException {

    String query = "ConceptXYZ";
    Source mockSource = mock(Source.class);
    when(mockSource.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn("omop_v5");
    when(mockSource.getSourceDialect()).thenReturn("sql server");

    PreparedStatementRenderer psr = vocabularyService.prepareExecuteSearchWithQuery(query, mockSource);

    assertSqlEquals("/vocabulary/sql/search-expected.sql", psr);
    assertEquals("%" + query.toLowerCase() + "%", psr.getOrderedParamsList().get(0));
    assertEquals("%" + query.toLowerCase() + "%", psr.getOrderedParamsList().get(1));
    assertEquals(query.toLowerCase(), psr.getOrderedParamsList().get(2));
  }

  @Test
  public void prepareGetRelatedConcepts2() throws IOException {

    RelatedConceptSearch search = new RelatedConceptSearch();
    search.vocabularyId = null; // new String[]{"0","1","2"};//null; was
    search.conceptClassId = new String[]{"4", "5"};
    search.conceptId = new long[]{(long) 0};

    PreparedStatementRenderer psr = vocabularyService.prepareGetRelatedConcepts(search, getSource());
    assertSqlEquals("/vocabulary/sql/getRelatedConceptsFiltered-expected-2.sql", psr);
    assertEquals(6, psr.getOrderedParamsList().size());
    /// the first 4 arguments are the conceptId repeated
    for (int i = 0; i < 4; i++) {
      long expectedValue = search.conceptId[0];
      assertEquals(expectedValue, psr.getOrderedParamsList().get(i));
    }

    /// the next two arguments are the conceptClassId id
    for (int i = 0; i < 2; i++) {
      String expectedValue = "" + (i + 4);
      assertEquals(expectedValue, psr.getOrderedParamsList().get(i + 4));
    }
  }

  @Test
  public void prepareGetRelatedConcepts() throws IOException {

    RelatedConceptSearch search = new RelatedConceptSearch();
    search.vocabularyId = new String[]{"0", "1", "2"};
    search.conceptClassId = new String[]{"4", "5", "6"};
    search.conceptId = new long[]{0, 1, 2, 3, 4};

    PreparedStatementRenderer psr = vocabularyService.prepareGetRelatedConcepts(search, getSource());
    assertSqlEquals("/vocabulary/sql/getRelatedConceptsFiltered-expected-1.sql", psr);
    assertEquals(26, psr.getOrderedParamsList().size());

    /// the first 20 arguments are the conceptId repeated
    for (int i = 0; i < 20; i++) {
      int expectedIndex = i % 5;
      long expectedValue = search.conceptId[expectedIndex];
      assertEquals(expectedValue, psr.getOrderedParamsList().get(i));
    }

    /// the next three arguments are the vocabulary id
    for (int i = 20; i < 23; i++) {
      int expectedIndex = i - 20;
      String expectedValue = search.vocabularyId[expectedIndex];
      assertEquals(expectedValue, psr.getOrderedParamsList().get(i));
    }

    /// the next three arguments are the vocabulary id
    for (int i = 23; i < 26; i++) {
      int expectedIndex = i - 23;
      String expectedValue = search.conceptClassId[expectedIndex];
      assertEquals(expectedValue, psr.getOrderedParamsList().get(i));
    }
  }

  @Test
  public void prepareGetCommonAncestors() throws IOException {

    Object[] identifiers = {"1"};
    PreparedStatementRenderer psr = vocabularyService.prepareGetCommonAncestors(identifiers, getSource());
    assertSqlEquals("/vocabulary/sql/getCommonAncestors-expected.sql", psr);
    assertEquals(identifiers[0], psr.getOrderedParamsList().get(0));
  }


  @Test
  public void prepareExecuteMappedLookup() throws IOException {

    long[] identifiers = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L};
    PreparedStatementRenderer psr = vocabularyService.prepareExecuteMappedLookup(identifiers, getSource());
    assertSqlEquals("/vocabulary/sql/getMappedSourcecodes-expected.sql", psr);
    for (int i = 0; i < identifiers.length * 3; i++) {
      assertEquals((long) (i % 7), psr.getOrderedParamsList().get(i));
    }
  }

  @Test
  public void prepareExecuteSourcecodeLookup() throws IOException {

    String[] sourcecodes = new String[]{"a", "b", "c", "d", "e"};
    PreparedStatementRenderer psr = vocabularyService.prepareExecuteSourcecodeLookup(sourcecodes, getSource());
    assertSqlEquals("/vocabulary/sql/lookupSourcecodes-expected.sql", psr);
    assertEquals("a", psr.getOrderedParamsList().get(0));
    assertEquals("b", psr.getOrderedParamsList().get(1));
    assertEquals("c", psr.getOrderedParamsList().get(2));
    assertEquals("d", psr.getOrderedParamsList().get(3));
    assertEquals("e", psr.getOrderedParamsList().get(4));
  }

  @Test
  public void prepareExecuteIdentifierLookup() throws IOException {

    long[] identifiers = {(long) 11, (long) 5, (long) 8};
    PreparedStatementRenderer psr = vocabularyService.prepareExecuteIdentifierLookup(identifiers, getSource());
    assertSqlEquals("/vocabulary/sql/lookupIdentifiers-expected.sql", psr);
    assertEquals(11L, psr.getOrderedParamsList().get(0));
    assertEquals(5L, psr.getOrderedParamsList().get(1));
    assertEquals(8L, psr.getOrderedParamsList().get(2));
  }

  @Test
  public void prepareGetDescendantOfAncestorConcepts() throws IOException {

    DescendentOfAncestorSearch search = new DescendentOfAncestorSearch();
    search.conceptId = "1";
    search.ancestorVocabularyId = "2";
    search.ancestorClassId = "3";
    search.siblingVocabularyId = "4";
    search.siblingClassId = "5";
    PreparedStatementRenderer psr = vocabularyService.prepareGetDescendantOfAncestorConcepts(search, getSource());
    assertSqlEquals("/vocabulary/sql/getDescendentOfAncestorConcepts-expected.sql", psr);

    assertEquals(1, psr.getOrderedParamsList().get(0));
    assertEquals("2", psr.getOrderedParamsList().get(1));
    assertEquals("3", psr.getOrderedParamsList().get(2));
    assertEquals("4", psr.getOrderedParamsList().get(3));
    assertEquals("5", psr.getOrderedParamsList().get(4));

  }

  @Test
  public void prepareGetDescendantConceptsByList() throws IOException {

    String[] conceptList = new String[]{"0", "1", "2", "3", "4"};

    PreparedStatementRenderer psr = vocabularyService.prepareGetDescendantConceptsByList(conceptList, getSource());
    assertSqlEquals("/vocabulary/sql/getDescendantConceptsMultipleConcepts-expected.sql", psr);

    /// verify that the params are ordered correctly
    for (int i = 0; i < psr.getOrderedParamsList().size(); i++) {
      assertEquals(i, psr.getOrderedParamsList().get(i));
    }
  }

}
