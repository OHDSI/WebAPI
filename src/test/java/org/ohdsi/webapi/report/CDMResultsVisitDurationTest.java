package org.ohdsi.webapi.report;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

/**
 * Runs the Data Sources visit drilldown queries against the embedded Postgres instance and checks
 * that the length of stay distribution is read from Achilles analysis 213 (Achilles 1.7 and later)
 * as well as from analysis 211, which held that distribution before Achilles 1.7.
 */
public class CDMResultsVisitDurationTest extends AbstractDatabaseTest {

    private static final String RESULTS_SCHEMA = "visit_results";

    private static final int INPATIENT_VISIT = 9201;
    private static final int OUTPATIENT_VISIT = 9202;

    private CDMResultsAnalysisRunner runner;
    private Source source;

    @Before
    public void setUp() {

        runner = new CDMResultsAnalysisRunner();
        runner.init("postgresql", new ObjectMapper());

        source = mock(Source.class);
        when(source.getTableQualifier(SourceDaimon.DaimonType.Results)).thenReturn(RESULTS_SCHEMA);
        when(source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn(RESULTS_SCHEMA);
        when(source.getTableQualifier(SourceDaimon.DaimonType.CDM)).thenReturn(RESULTS_SCHEMA);
        when(source.getSourceDialect()).thenReturn("postgresql");

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %1$s CASCADE; CREATE SCHEMA %1$s;", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.achilles_results (analysis_id INT, stratum_1 VARCHAR(255), "
                + "stratum_2 VARCHAR(255), stratum_3 VARCHAR(255), stratum_4 VARCHAR(255), stratum_5 VARCHAR(255), "
                + "count_value BIGINT)", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.achilles_results_dist (analysis_id INT, stratum_1 VARCHAR(255), "
                + "stratum_2 VARCHAR(255), stratum_3 VARCHAR(255), stratum_4 VARCHAR(255), stratum_5 VARCHAR(255), "
                + "count_value BIGINT, min_value FLOAT, max_value FLOAT, avg_value FLOAT, stdev_value FLOAT, "
                + "median_value FLOAT, p10_value FLOAT, p25_value FLOAT, p75_value FLOAT, p90_value FLOAT)",
                RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.concept (concept_id INT, concept_name VARCHAR(255))",
                RESULTS_SCHEMA));

        jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name) VALUES (?, ?)", RESULTS_SCHEMA),
                INPATIENT_VISIT, "Inpatient Visit");
        jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name) VALUES (?, ?)", RESULTS_SCHEMA),
                OUTPATIENT_VISIT, "Outpatient Visit");

        // Achilles 1.7+: 213 is the length of stay distribution and 211 is a plain record count
        insertDistribution(213, INPATIENT_VISIT, 1, 1, 2, 4, 7, 12, 60);
        jdbcTemplate.update(String.format("INSERT INTO %s.achilles_results (analysis_id, count_value) VALUES (?, ?)",
                RESULTS_SCHEMA), 211, 3);
        // results produced before Achilles 1.7 stored the same distribution under 211
        insertDistribution(211, OUTPATIENT_VISIT, 0, 0, 0, 0, 1, 1, 5);
    }

    @After
    public void tearDown() {

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", RESULTS_SCHEMA));
    }

    private void insertDistribution(int analysisId, int visitConceptId, double min, double p10, double p25,
                                    double median, double p75, double p90, double max) {

        jdbcTemplate.update(String.format("INSERT INTO %s.achilles_results_dist (analysis_id, stratum_1, count_value, "
                + "min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", RESULTS_SCHEMA),
                analysisId, String.valueOf(visitConceptId), 100L, min, p10, p25, median, p75, p90, max);
    }

    @Test
    public void getDrilldown_visitDurationReadsLengthOfStayFromAnalysis213() {

        JsonNode drilldown = runner.getDrilldown(jdbcTemplate, "visit", INPATIENT_VISIT, source);

        JsonNode duration = drilldown.get("visitDurationByType");
        assertEquals(1, duration.size());
        JsonNode row = duration.get(0);
        assertEquals(INPATIENT_VISIT, row.get("conceptId").asInt());
        assertEquals("Length of stay", row.get("category").asText());
        assertEquals(1, row.get("minValue").asDouble(), 0);
        assertEquals(4, row.get("medianValue").asDouble(), 0);
        assertEquals(12, row.get("p90Value").asDouble(), 0);
        assertEquals(60, row.get("maxValue").asDouble(), 0);
    }

    @Test
    public void getDrilldown_visitDurationStillReadsLegacyAnalysis211() {

        JsonNode drilldown = runner.getDrilldown(jdbcTemplate, "visit", OUTPATIENT_VISIT, source);

        JsonNode duration = drilldown.get("visitDurationByType");
        assertEquals(1, duration.size());
        assertEquals(OUTPATIENT_VISIT, duration.get(0).get("conceptId").asInt());
        assertEquals(5, duration.get(0).get("maxValue").asDouble(), 0);
    }

    @Test
    public void getDrilldownSummary_visitDurationIncludesAllVisitConcepts() {

        JsonNode summary = runner.getDrilldown(jdbcTemplate, "visit", null, source);

        JsonNode duration = summary.get("visitDurationByType");
        assertEquals(2, duration.size());
    }
}
