package org.ohdsi.webapi.service;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

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
}
