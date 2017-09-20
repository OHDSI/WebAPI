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

    String[] identifiers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    PreparedStatementRenderer psr = cdmResultsService.prepareGetConceptRecordCount(identifiers, getSource());
    assertSqlEquals("/cdmresults/sql/getConceptRecordCount-expected.sql", psr);
  }

  @Test
  public void prepareGetConditionOccurrenceTreemap() throws IOException {

    String[] identifiers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    PreparedStatementRenderer psr = cdmResultsService.prepareGetConditionOccurrenceTreemap(identifiers, getSource());
    assertSqlEquals("/cdmresults/sql/getConditionOccurrenceTreemap-expected.sql", psr);
    for (int i = 0; i < identifiers.length; i++) {
      assertEquals(Integer.valueOf(identifiers[i]), psr.getOrderedParamsList().get(i));
    }
  }

  @Test
  public void prepareGetDrugEraPrevalenceByGenderAgeYear() throws IOException {

    String conceptId = "586762";
    PreparedStatementRenderer psr = cdmResultsService.prepareGetDrugEraPrevalenceByGenderAgeYear(conceptId, getSource());
    assertSqlEquals("/cdmresults/sql/getDrugEraPrevalenceByGenderAgeYear-expected.sql", psr);
    for (Object param : psr.getOrderedParamsList()) {
      assertEquals(586762, param);
    }
  }

}
