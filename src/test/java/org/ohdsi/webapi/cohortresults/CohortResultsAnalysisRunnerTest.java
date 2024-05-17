package org.ohdsi.webapi.cohortresults;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CohortResultsAnalysisRunnerTest {

  @Test
  public void testSaveIsFalseScenarios() {
      assertThrows(Exception.class, () -> {

          CohortResultsAnalysisRunner r = new CohortResultsAnalysisRunner(null, null, null);
          r.getCohortConditionDrilldown(null, -1, -1, 0, 0, null, false);
          r.getCohortDataDensity(null, 0, null, null, null, false);
          r.getCohortDeathData(null, 0, null, null, null, false);
          r.getCohortDrugDrilldown(null, 0, 0, null, null, null, false);
          r.getCohortMeasurementResults(null, 0, null, null, null, false);
          r.getCohortMeasurementResultsDrilldown(null, 0, 0, null, null, null, false);
          r.getCohortObservationPeriod(null, 0, null, null, null, false);
          r.getCohortObservationResults(null, 0, null, null, null, false);
          r.getCohortObservationResultsDrilldown(null, 0, 0, null, null, null, false);
          r.getCohortProcedureDrilldown(null, 0, 0, null, null, null, false);
          r.getCohortProceduresDrilldown(null, 0, 0, null, null, null, false);
          r.getCohortSpecificSummary(null, 0, null, null, null, false);
          r.getCohortSpecificTreemapResults(null, 0, null, null, null, false);
          r.getCohortVisitsDrilldown(null, 0, 0, null, null, null, false);
          r.getConditionEraDrilldown(null, 0, 0, null, null, null, false);
          r.getConditionEraTreemap(null, 0, null, null, null, false);
          r.getConditionResults(null, 0, 0, null, null, null, false);
          r.getConditionTreemap(null, 0, null, null, null, false);
          r.getDashboard(null, 0, null, null, null, false, false);
          r.getDrugEraResults(null, 0, 0, null, null, null, false);
          r.getDrugEraTreemap(null, 0, null, null, null, false);
          r.getDrugResults(null, 0, 0, null, null, null, false);
          r.getDrugTreemap(null, 0, null, null, null, false);
          r.getHeraclesHeel(null, 0, null, false);
          r.getPersonResults(null, 0, null, null, null, false);
          r.getProcedureTreemap(null, 0, null, null, null, false);
          r.getVisitTreemap(null, 0, null, null, null, false);

      });

  }

}
