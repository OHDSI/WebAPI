package org.ohdsi.webapi.service;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.evidence.EvidenceSearch;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class EvidenceServiceTest extends AbstractServiceTest {

  @Autowired
  EvidenceService evidenceService;

  @BeforeEach
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
