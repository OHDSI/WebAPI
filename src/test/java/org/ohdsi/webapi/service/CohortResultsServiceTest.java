package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.cohortresults.ExposureCohortSearch;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;


public class CohortResultsServiceTest extends AbstractServiceTest {

  @Autowired
  private CohortResultsService cohortResultsService;

  @Before
  public void before() {

    if (cohortResultsService == null) {
      cohortResultsService = new CohortResultsService();
    }
  }

  @Test(expected = Exception.class)
  public void getCohortSpecificResultsRefreshIsTrue() {

    cohortResultsService.getCohortSpecificResults(-1, null, null, null, true);
  }

  @Test(expected = Exception.class)
  public void getCohortSpecificResultsRefreshIsFalse() {

    cohortResultsService.getCohortSpecificResults(-1, null, null, null, false);
  }

  @Test(expected = Exception.class)
  public void getDashboardRefreshIsTrue() {

    cohortResultsService.getDashboard(-1, null, null, true, null, true);
  }

  @Test(expected = Exception.class)
  public void getDashboardRefreshIsFalse() {

    cohortResultsService.getDashboard(-1, null, null, true, null, false);
  }

  @Test
  public void prepareGetRawDistinctPersonCount() throws IOException {

    String id = "1230";
    PreparedStatementRenderer psr = cohortResultsService.prepareGetRawDistinctPersonCount(id, getSource());
    assertSqlEquals("/cohortresults/sql/raw/getTotalDistinctPeople-expected.sql", psr);
    assertEquals(Integer.parseInt(id), psr.getOrderedParamsList().get(0));
  }

  @Test
  public void prepareGetCompletedAnalyses() throws IOException {

    String id = "1230";
    PreparedStatementRenderer psr = cohortResultsService.prepareGetCompletedAnalysis(id, getSource());
    assertSqlEquals("/cohortresults/sql/raw/getCompletedAnalyses-expected.sql", psr);
    assertEquals(2, psr.getOrderedParamsList().size());
  }

  @Test
  public void prepareGetExposureOutcomeCohortPredictors() throws IOException {

    ExposureCohortSearch search = new ExposureCohortSearch();
    search.minCellCount = 5;
    search.exposureCohortList = new String[]{"1"};
    search.outcomeCohortList = new String[]{"2"};
    PreparedStatementRenderer psr = cohortResultsService.prepareGetExposureOutcomeCohortPredictors(search, getSource());
    assertSqlEquals("/cohortresults/sql/cohortSpecific/getExposureOutcomePredictors-expected.sql", psr);

  }

  @Test
  public void prepareGetTimeToEventDrilldown() throws IOException {

    ExposureCohortSearch search = new ExposureCohortSearch();
    search.exposureCohortList = new String[]{"1", "2"};
    search.outcomeCohortList = new String[]{"3", "4"};

    PreparedStatementRenderer psr = cohortResultsService.prepareGetTimeToEventDrilldown(search, getSource());
    assertSqlEquals("/cohortresults/sql/cohortSpecific/getTimeToEventDrilldown-expected.sql", psr);

    for (Object param : psr.getOrderedParamsList()) {
      assertTrue("1".equals(param) || "2".equals(param) || "3".equals(param) || "4".equals(param));
    }
  }

  @Test
  public void prepareGetExposureOutcomeCohortRates() throws IOException {

    ExposureCohortSearch search = new ExposureCohortSearch();
    search.exposureCohortList = new String[]{"1"};
    search.outcomeCohortList = new String[]{"1"};
    PreparedStatementRenderer psr = cohortResultsService.prepareGetExposureOutcomeCohortRates(search, getSource());
    assertSqlEquals("/cohortresults/sql/cohortSpecific/getExposureOutcomeCohortRates-expected.sql", psr);

    for (Object object : psr.getOrderedParamsList()) {
      assertTrue("1".equals(object));
    }
  }

  @Test
  public void prepareGetCohortMembers() throws IOException {

    int id = 111998;
    String gender = "M";
    String age = "15";
    String conditions = "2,3,4";
    String drugs = "45,61";
    int rows = 5;

    PreparedStatementRenderer psr = cohortResultsService.prepareGetCohortMembers(id, gender, age, conditions, drugs, rows, getSource());
    assertSqlEquals("/cohortresults/sql/raw/getCohortBreakdownPeople-expected.sql", psr);

    assertEquals(id, psr.getOrderedParamsList().get(0));
    assertEquals(gender, psr.getOrderedParamsList().get(1));
    assertEquals(age, psr.getOrderedParamsList().get(2));
    assertEquals(2, psr.getOrderedParamsList().get(3));
    assertEquals(3, psr.getOrderedParamsList().get(4));
    assertEquals(4, psr.getOrderedParamsList().get(5));
    assertEquals(45, psr.getOrderedParamsList().get(6));
    assertEquals(61, psr.getOrderedParamsList().get(7));

  }

}
