package org.ohdsi.webapi.service;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.AbstractDatabaseTest;

/**
 * Runs the concept_hierarchy DDL and initialization scripts against the embedded Postgres instance
 * and checks the hierarchy built from a small vocabulary.
 */
public class DDLServiceTest extends AbstractDatabaseTest {

    private static final String VOCAB_SCHEMA = "ddl_vocab";
    private static final String RESULTS_SCHEMA = "ddl_results";

    private static final int ACETAMINOPHEN = 1125315;
    private static final int ACETAMINOPHEN_TABLET = 19020053;
    private static final int COLD_AND_COUGH_CLASS = 44361362;
    private static final int ATC_4TH = 21604303;
    private static final int ATC_2ND = 21604253;
    private static final int ATC_1ST = 21604180;

    @Before
    public void setUp() {

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %1$s CASCADE; CREATE SCHEMA %1$s;", VOCAB_SCHEMA));
        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %1$s CASCADE; CREATE SCHEMA %1$s;", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.concept (concept_id INT, concept_name VARCHAR(255), "
                + "domain_id VARCHAR(20), vocabulary_id VARCHAR(20), concept_class_id VARCHAR(20), "
                + "standard_concept VARCHAR(1), concept_code VARCHAR(50))", VOCAB_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.vocabulary (vocabulary_id VARCHAR(20), "
                + "vocabulary_name VARCHAR(255))", VOCAB_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.concept_ancestor (ancestor_concept_id INT, "
                + "descendant_concept_id INT, min_levels_of_separation INT, max_levels_of_separation INT)", VOCAB_SCHEMA));

        insertConcept(ACETAMINOPHEN, "acetaminophen", "RxNorm", "Ingredient", "S");
        insertConcept(ACETAMINOPHEN_TABLET, "acetaminophen 500 MG Oral Tablet", "RxNorm", "Clinical Drug", "S");
        // classification concept that also carries the Ingredient concept class
        insertConcept(COLD_AND_COUGH_CLASS, "ALKA-SELTZER PLUS COLD AND COUGH", "SPL", "Ingredient", "C");
        insertConcept(ATC_4TH, "Anilides", "ATC", "ATC 4th", "C");
        insertConcept(ATC_2ND, "ANALGESICS", "ATC", "ATC 2nd", "C");
        insertConcept(ATC_1ST, "NERVOUS SYSTEM", "ATC", "ATC 1st", "C");

        insertAncestor(ACETAMINOPHEN, ACETAMINOPHEN, 0);
        insertAncestor(ACETAMINOPHEN_TABLET, ACETAMINOPHEN_TABLET, 0);
        insertAncestor(ACETAMINOPHEN, ACETAMINOPHEN_TABLET, 1);
        insertAncestor(COLD_AND_COUGH_CLASS, ACETAMINOPHEN, 1);
        insertAncestor(COLD_AND_COUGH_CLASS, ACETAMINOPHEN_TABLET, 2);
        insertAncestor(ATC_4TH, ACETAMINOPHEN, 1);
        insertAncestor(ATC_4TH, ACETAMINOPHEN_TABLET, 2);
        insertAncestor(ATC_2ND, ATC_4TH, 2);
        insertAncestor(ATC_1ST, ATC_2ND, 1);
    }

    @After
    public void tearDown() {

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", VOCAB_SCHEMA));
    }

    private void insertConcept(int conceptId, String name, String vocabularyId, String conceptClassId, String standard) {

        jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name, domain_id, vocabulary_id, "
                + "concept_class_id, standard_concept) VALUES (?, ?, 'Drug', ?, ?, ?)", VOCAB_SCHEMA),
                conceptId, name, vocabularyId, conceptClassId, standard);
    }

    private void insertAncestor(int ancestorId, int descendantId, int levels) {

        jdbcTemplate.update(String.format("INSERT INTO %s.concept_ancestor (ancestor_concept_id, descendant_concept_id, "
                + "min_levels_of_separation, max_levels_of_separation) VALUES (?, ?, ?, ?)", VOCAB_SCHEMA),
                ancestorId, descendantId, levels, levels);
    }

    @Test
    public void initConceptHierarchy_drugHierarchyUsesStandardIngredientsOnly() {

        for (String path : DDLService.INIT_CONCEPT_HIERARCHY_FILE_PATHS) {
            String sql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(path),
                    new String[]{DDLService.VOCAB_SCHEMA, DDLService.RESULTS_SCHEMA},
                    new String[]{VOCAB_SCHEMA, RESULTS_SCHEMA});
            jdbcTemplate.execute(SqlTranslate.translateSql(sql, "postgresql"));
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(String.format("SELECT concept_id, "
                + "level1_concept_name, level2_concept_name, level3_concept_name, level4_concept_name "
                + "FROM %s.concept_hierarchy WHERE treemap = 'Drug' ORDER BY concept_id", RESULTS_SCHEMA));

        assertEquals(2, rows.size());
        for (Map<String, Object> row : rows) {
            assertEquals("acetaminophen", row.get("level1_concept_name"));
            assertEquals("Anilides", row.get("level2_concept_name"));
            assertEquals("ANALGESICS", row.get("level3_concept_name"));
            assertEquals("NERVOUS SYSTEM", row.get("level4_concept_name"));
        }
        assertEquals(ACETAMINOPHEN, rows.get(0).get("concept_id"));
        assertEquals(ACETAMINOPHEN_TABLET, rows.get(1).get("concept_id"));
    }
}
