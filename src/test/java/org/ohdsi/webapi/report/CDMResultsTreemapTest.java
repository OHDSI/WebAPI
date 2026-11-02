package org.ohdsi.webapi.report;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

/**
 * Runs the Data Sources condition treemap query against the embedded Postgres instance and checks
 * the concept path built from the concept_hierarchy levels.
 */
public class CDMResultsTreemapTest extends AbstractDatabaseTest {

    private static final String RESULTS_SCHEMA = "treemap_results";

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
        jdbcTemplate.execute(String.format("CREATE TABLE %s.concept_hierarchy (concept_id INT, concept_name VARCHAR(400), "
                + "treemap VARCHAR(20), concept_hierarchy_type VARCHAR(20), level1_concept_name VARCHAR(255), "
                + "level2_concept_name VARCHAR(255), level3_concept_name VARCHAR(255), level4_concept_name VARCHAR(255))",
                RESULTS_SCHEMA));

        // number of persons in the CDM
        insertResult(1, null, 100);
        // condition occurrence person (400) and record (401) counts for one SNOMED concept
        insertResult(400, "201826", 10);
        insertResult(401, "201826", 25);
        // MedDRA hierarchy of that concept: level1 = PT, level2 = HLT, level3 = HLGT, level4 = SOC
        jdbcTemplate.update(String.format("INSERT INTO %s.concept_hierarchy (concept_id, concept_name, treemap, "
                + "level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)", RESULTS_SCHEMA),
                201826, "Type 2 diabetes mellitus", "Condition",
                "Type 2 diabetes mellitus (PT)", "Diabetes mellitus (HLT)",
                "Glucose metabolism disorders (HLGT)", "Metabolism and nutrition disorders (SOC)");
    }

    @After
    public void tearDown() {

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", RESULTS_SCHEMA));
    }

    private void insertResult(int analysisId, String stratum1, long countValue) {

        jdbcTemplate.update(String.format("INSERT INTO %s.achilles_results (analysis_id, stratum_1, count_value) "
                + "VALUES (?, ?, ?)", RESULTS_SCHEMA), analysisId, stratum1, countValue);
    }

    @Test
    public void getTreemap_conditionPathIncludesAllHierarchyLevels() {

        ArrayNode treemap = runner.getTreemap(jdbcTemplate, "condition", source);

        assertEquals(1, treemap.size());
        JsonNode row = treemap.get(0);
        assertEquals(201826, row.get("conceptId").asInt());
        assertEquals("Metabolism and nutrition disorders (SOC)||Glucose metabolism disorders (HLGT)||"
                + "Diabetes mellitus (HLT)||Type 2 diabetes mellitus (PT)||Type 2 diabetes mellitus",
                row.get("conceptPath").asText());
    }
}
