package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.evidence.EvidenceSearch;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class EvidenceServiceTest extends AbstractServiceTest {

  @Autowired
  EvidenceService evidenceService;

  @Before
  public void before() {

    if (evidenceService == null) evidenceService = new EvidenceService();
  }

  @Test
  public void prepareGetDrugHoiEvidence() throws IOException {

    String drugId = "4444";
    String hoiId = "5555";
    String key = drugId + "-" + hoiId;

    PreparedStatementRenderer psr = evidenceService.prepareGetDrugHoiEvidence(key, getSource());
    assertSqlEquals("/evidence/sql/getDrugHoiEvidence-expected.sql", psr);
    assertTrue(psr.getOrderedParamsList().contains(4444));
    assertTrue(psr.getOrderedParamsList().contains(5555));

  }

  @Test
  public void prepareGetEvidenceSummaryBySource() throws IOException {

    String conditionId = "19";
    String drugId = "23";
    String evidenceGroup = "Literature";
    PreparedStatementRenderer psr = evidenceService.prepareGetEvidenceSummaryBySource(conditionId, drugId, evidenceGroup, getSource());
    assertSqlEquals("/evidence/sql/getEvidenceSummaryBySource-expected.sql", psr);
    assertEquals(19, psr.getOrderedParamsList().get(0));
    assertEquals(23, psr.getOrderedParamsList().get(1));
    assertEquals("%MEDLINE%", psr.getOrderedParamsList().get(2));
  }

  @Test
  public void prepareGetEvidenceDetails() throws IOException {

    Integer conditionId = 1;
    Integer drugId = 10;
    String evidenceType = "evidenceType1";

    PreparedStatementRenderer psr = evidenceService.prepareGetEvidenceDetails(conditionId, drugId, evidenceType, getSource());
    assertSqlEquals("/evidence/sql/getEvidenceDetails-expected.sql", psr);

    assertEquals(1, psr.getOrderedParamsList().get(0));
    assertEquals(10, psr.getOrderedParamsList().get(1));
    assertEquals("evidenceType1", psr.getOrderedParamsList().get(2));

  }

  @Test
  public void prepareGetSpontaneousReports() throws IOException {

    EvidenceSearch es = new EvidenceSearch();
    es.conditionConceptList = new String[]{"1", "2", "3", "4", "5"};
    es.ingredientConceptList = new String[]{"6", "7", "8", "9", "10"};

    PreparedStatementRenderer psr = evidenceService.prepareGetSpontaneousReports(es, getSource());
    assertSqlEquals("/evidence/sql/getSpontaneousReports-expected.sql", psr);

    assertEquals(1, psr.getOrderedParamsList().get(0));
    assertEquals(2, psr.getOrderedParamsList().get(1));
    assertEquals(3, psr.getOrderedParamsList().get(2));
    assertEquals(4, psr.getOrderedParamsList().get(3));
    assertEquals(5, psr.getOrderedParamsList().get(4));
    assertEquals(6, psr.getOrderedParamsList().get(5));
    assertEquals(7, psr.getOrderedParamsList().get(6));
    assertEquals(8, psr.getOrderedParamsList().get(7));
    assertEquals(9, psr.getOrderedParamsList().get(8));
    assertEquals(10, psr.getOrderedParamsList().get(9));
  }

  @Test
  public void prepareLabelEvidence() throws IOException {

    EvidenceSearch es = new EvidenceSearch();
    es.conditionConceptList = new String[]{"1", "2", "3"};
    es.evidenceTypeList = new String[]{"4", "5", "6"};
    es.ingredientConceptList = new String[]{"7", "8", "9"};

    PreparedStatementRenderer psr = evidenceService.prepareLabelEvidence(es, this.getSource());
    assertSqlEquals("/evidence/sql/getLabelEvidence-expected.sql", psr);

    assertEquals(7, psr.getOrderedParamsList().get(0));
    assertEquals(8, psr.getOrderedParamsList().get(1));
    assertEquals(9, psr.getOrderedParamsList().get(2));

    assertEquals(1, psr.getOrderedParamsList().get(3));
    assertEquals(2, psr.getOrderedParamsList().get(4));
    assertEquals(3, psr.getOrderedParamsList().get(5));

    assertEquals(7, psr.getOrderedParamsList().get(6));
    assertEquals(8, psr.getOrderedParamsList().get(7));
    assertEquals(9, psr.getOrderedParamsList().get(8));

    assertEquals("4", psr.getOrderedParamsList().get(9));
    assertEquals("5", psr.getOrderedParamsList().get(10));
    assertEquals("6", psr.getOrderedParamsList().get(11));
  }

  @Test
  public void prepareEvidenceSearch() throws IOException {

    EvidenceSearch es = new EvidenceSearch();
    es.conditionConceptList = new String[]{"1", "2"};
    es.evidenceTypeList = new String[]{"3", "4"};
    es.ingredientConceptList = new String[]{"5", "6"};

    PreparedStatementRenderer psr = evidenceService.prepareEvidenceSearch(es, this.getSource());
    assertSqlEquals("/evidence/sql/getEvidenceFromUniverse-expected.sql", psr);

    assertEquals(1, psr.getOrderedParamsList().get(0));
    assertEquals(2, psr.getOrderedParamsList().get(1));
    assertEquals(5, psr.getOrderedParamsList().get(2));
    assertEquals(6, psr.getOrderedParamsList().get(3));
    assertEquals("3", psr.getOrderedParamsList().get(4));
    assertEquals("4", psr.getOrderedParamsList().get(5));
  }

}
