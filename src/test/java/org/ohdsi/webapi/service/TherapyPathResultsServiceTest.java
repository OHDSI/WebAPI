package org.ohdsi.webapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class TherapyPathResultsServiceTest extends AbstractServiceTest {

  @Autowired
  private TherapyPathResultsService therapyPathResultsService;

  @BeforeEach
  public void before() {

    if (therapyPathResultsService == null) {
      therapyPathResultsService = new TherapyPathResultsService();
    }
  }

  @Test
  public void prepareGetReports() throws IOException {

    PreparedStatementRenderer psr = therapyPathResultsService.prepareGetReports(getSource());
    assertSqlEquals("/therapypathresults/sql/getTherapyPathReports-expected.sql", psr);
  }

  @Test
  public void prepareGetTherapyVectors() throws IOException {

    String id = "5555";
    PreparedStatementRenderer psr = therapyPathResultsService.prepareGetTherapyVectors(id, getSource());
    assertSqlEquals("/therapypathresults/sql/getTherapyPathVectors-expected.sql", psr);
    assertEquals(5555, psr.getOrderedParamsList().get(0));
    assertEquals(1, psr.getOrderedParamsList().size());
  }

  @Test
  public void prepareGetSummaries() throws IOException {

    String[] identifiers = new String[]{"1111"};

    PreparedStatementRenderer psr = therapyPathResultsService.prepareGetSummaries(identifiers, getSource());
    assertSqlEquals("/therapypathresults/sql/getTherapySummaries-expected.sql", psr);
    assertNotNull(psr.getSetter());
    assertEquals(1111, psr.getOrderedParamsList().get(0));
  }

}
