package org.ohdsi.webapi.report;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

/**
 * Runs the Data Sources condition drilldown queries against the embedded Postgres instance and checks
 * the "by type" results for condition type concepts from both the deprecated 'Condition Type' vocabulary
 * and the current 'Type Concept' vocabulary.
 */
public class CDMResultsConditionByTypeTest extends AbstractDatabaseTest {

    private static final String RESULTS_SCHEMA = "condition_by_type_results";

    private static final int CONDITION_CONCEPT_ID = 201826;

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
        jdbcTemplate.execute(String.format("CREATE TABLE %s.achilles_results_dist (analysis_id INT, "
                + "stratum_1 VARCHAR(255), stratum_2 VARCHAR(255), stratum_3 VARCHAR(255), stratum_4 VARCHAR(255), "
                + "stratum_5 VARCHAR(255), count_value BIGINT, min_value FLOAT, max_value FLOAT, avg_value FLOAT, "
                + "stdev_value FLOAT, median_value FLOAT, p10_value FLOAT, p25_value FLOAT, p75_value FLOAT, "
                + "p90_value FLOAT)", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.concept (concept_id INT, concept_name VARCHAR(255), "
                + "domain_id VARCHAR(20), vocabulary_id VARCHAR(20), concept_class_id VARCHAR(20), "
                + "standard_concept VARCHAR(1), concept_code VARCHAR(50))", RESULTS_SCHEMA));

        insertConcept(CONDITION_CONCEPT_ID, "Type 2 diabetes mellitus", "Condition", "SNOMED", "Clinical Finding", "S");
        // condition type concepts from the vocabulary that was deprecated in 2020
        insertConcept(38000200, "Inpatient header - 1st position", "Type Concept", "Condition Type", "Condition Type", null);
        insertConcept(38000201, "Inpatient header - 2nd position", "Type Concept", "Condition Type", "Condition Type", null);
        // condition type concepts from the current 'Type Concept' vocabulary
        insertConcept(32817, "EHR", "Type Concept", "Type Concept", "Type Concept", "S");
        insertConcept(32810, "Claim", "Type Concept", "Type Concept", "Type Concept", "S");
        insertConcept(32855, "Inpatient claim header", "Type Concept", "Type Concept", "Type Concept", "S");

        // number of condition occurrence records by condition_concept_id and condition_type_concept_id
        insertResult(405, CONDITION_CONCEPT_ID, 38000200, 5);
        insertResult(405, CONDITION_CONCEPT_ID, 38000201, 3);
        insertResult(405, CONDITION_CONCEPT_ID, 32817, 7);
        insertResult(405, CONDITION_CONCEPT_ID, 32810, 11);
        insertResult(405, CONDITION_CONCEPT_ID, 32855, 13);
    }

    @After
    public void tearDown() {

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", RESULTS_SCHEMA));
    }

    private void insertConcept(int conceptId, String conceptName, String domainId, String vocabularyId,
                               String conceptClassId, String standardConcept) {

        jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name, domain_id, vocabulary_id, "
                + "concept_class_id, standard_concept, concept_code) VALUES (?, ?, ?, ?, ?, ?, ?)", RESULTS_SCHEMA),
                conceptId, conceptName, domainId, vocabularyId, conceptClassId, standardConcept,
                String.valueOf(conceptId));
    }

    private void insertResult(int analysisId, int stratum1, int stratum2, long countValue) {

        jdbcTemplate.update(String.format("INSERT INTO %s.achilles_results (analysis_id, stratum_1, stratum_2, "
                + "count_value) VALUES (?, ?, ?, ?)", RESULTS_SCHEMA),
                analysisId, String.valueOf(stratum1), String.valueOf(stratum2), countValue);
    }

    private static Map<String, Long> countsByTypeName(JsonNode byType) {

        Map<String, Long> counts = new HashMap<>();
        for (JsonNode row : byType) {
            assertEquals(CONDITION_CONCEPT_ID, row.get("conditionConceptId").asInt());
            counts.put(row.get("conceptName").asText(), row.get("countValue").asLong());
        }
        return counts;
    }

    private static Map<String, Long> expectedCounts() {

        Map<String, Long> expected = new HashMap<>();
        expected.put("Claim- Inpatient: Primary diagnosis", 5L);
        expected.put("Claim- Inpatient: Secondary diagnosis", 3L);
        expected.put("EHR", 7L);
        expected.put("Claim", 11L);
        // the inpatient/outpatient grouping only applies to the deprecated 'Condition Type' concepts
        expected.put("Inpatient claim header", 13L);
        return expected;
    }

    @Test
    public void getDrilldown_byTypeIncludesTypeConceptVocabulary() {

        JsonNode drilldown = runner.getDrilldown(jdbcTemplate, "condition", CONDITION_CONCEPT_ID, source);

        assertEquals(expectedCounts(), countsByTypeName(drilldown.get("byType")));
    }

    @Test
    public void getDrilldownSummary_byTypeIncludesTypeConceptVocabulary() {

        JsonNode drilldown = runner.getDrilldown(jdbcTemplate, "condition", null, source);

        assertEquals(expectedCounts(), countsByTypeName(drilldown.get("byType")));
    }
}
