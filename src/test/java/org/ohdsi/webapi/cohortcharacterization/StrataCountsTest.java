package org.ohdsi.webapi.cohortcharacterization;

import org.junit.Test;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StrataCountsTest {

  private static StrataCounts.Candidate c(long cohortId, long strataId, long n, long rows) {
    return new StrataCounts.Candidate(cohortId, strataId, n, rows);
  }

  @Test
  public void resolve_groupsByCohortAndStrata() {
    Map<String, Map<String, Long>> result = StrataCounts.resolve(Arrays.asList(
            c(97, 0, 170, 500), c(97, 7, 26, 120), c(97, 8, 1, 3), c(98, 0, 40, 200)));

    assertEquals(Long.valueOf(170), result.get("97").get("0"));
    assertEquals(Long.valueOf(26), result.get("97").get("7"));
    assertEquals(Long.valueOf(1), result.get("97").get("8"));
    assertEquals(Long.valueOf(40), result.get("98").get("0"));
    assertEquals(3, result.get("97").size());
  }

  @Test
  public void resolve_whenRowsDisagree_mostSupportedValueWins() {
    Map<String, Map<String, Long>> result = StrataCounts.resolve(Arrays.asList(
            c(97, 0, 171, 2), c(97, 0, 170, 498)));

    assertEquals(Long.valueOf(170), result.get("97").get("0"));
  }

  @Test
  public void resolve_tieGoesToLargerValue() {
    Map<String, Map<String, Long>> result = StrataCounts.resolve(Arrays.asList(
            c(97, 7, 25, 4), c(97, 7, 26, 4)));

    assertEquals(Long.valueOf(26), result.get("97").get("7"));
  }

  @Test
  public void resolve_noCandidates_returnsEmptyMap() {
    assertTrue(StrataCounts.resolve(Collections.emptyList()).isEmpty());
  }

  @Test
  public void queryTranslatesForSupportedDialects() {
    String sql = SqlRender.renderSql(
            ResourceHelper.GetResourceAsString("/resources/cohortcharacterizations/sql/queryStrataCounts.sql"),
            new String[]{"results_database_schema", "cohort_characterization_generation_id"},
            new String[]{"results", "12"});
    assertFalse(sql.contains("@"));
    for (String dialect : List.of("postgresql", "sql server", "oracle", "redshift", "snowflake", "spark", "bigquery")) {
      String translated = SqlTranslate.translateSql(sql, dialect);
      assertTrue(dialect, translated.toLowerCase().contains("round("));
    }
  }
}
