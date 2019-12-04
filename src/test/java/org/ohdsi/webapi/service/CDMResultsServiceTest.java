package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;

public class CDMResultsServiceTest extends AbstractServiceTest {

  @Autowired
  private CDMResultsService cdmResultsService;

  @Before
  public void before() {

    if (cdmResultsService == null) cdmResultsService = new CDMResultsService();
  }

  @Test
  public void prepareGetConceptRecordCount() throws IOException {

    Integer[] identifiers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    PreparedStatementRenderer psr = cdmResultsService.prepareGetConceptRecordCount(identifiers, getSource());
    assertSqlEquals("/cdmresults/sql/getConceptRecordCount-expected.sql", psr);
  }
}
