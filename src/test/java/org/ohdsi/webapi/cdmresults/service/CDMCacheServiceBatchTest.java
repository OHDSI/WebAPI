package org.ohdsi.webapi.cdmresults.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

/**
 * Checks the query that splits achilles_result_concept_count into the concept id ranges the CDM
 * cache is warmed with (getConceptsForBatch.sql). Each range must hold a full batch of concepts,
 * on every dialect: Snowflake, Spark and BigQuery do not perform integer division, so the group id
 * has to be floored explicitly or every concept ends up in a range of its own.
 */
public class CDMCacheServiceBatchTest extends AbstractDatabaseTest {

    private static final String RESULTS_SCHEMA = "cache_results";
    private static final String CONCEPT_SQL = "/resources/cdmresults/sql/getConceptsForBatch.sql";

    private static final int BATCH_SIZE = 100_000;
    private static final int CONCEPT_COUNT = 250_000;
    private static final int CONCEPT_ID_STEP = 3;

    private Source source;

    @Before
    public void setUp() {

        source = mock(Source.class);
        when(source.getTableQualifier(SourceDaimon.DaimonType.Results)).thenReturn(RESULTS_SCHEMA);
        when(source.getSourceDialect()).thenReturn("postgresql");

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %1$s CASCADE; CREATE SCHEMA %1$s;", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.achilles_result_concept_count (concept_id INT, "
                + "record_count BIGINT, descendant_record_count BIGINT, person_count BIGINT, "
                + "descendant_person_count BIGINT)", RESULTS_SCHEMA));
        // concept ids with gaps, so that the ranges have to follow the row order rather than the id values
        jdbcTemplate.execute(String.format("INSERT INTO %s.achilles_result_concept_count "
                + "SELECT n * %d, n, n, n, n FROM generate_series(1, %d) AS n", RESULTS_SCHEMA, CONCEPT_ID_STEP, CONCEPT_COUNT));
    }

    @After
    public void tearDown() {

        jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", RESULTS_SCHEMA));
    }

    private PreparedStatementRenderer conceptBatchRenderer() {

        return new PreparedStatementRenderer(source, CONCEPT_SQL, new String[]{"resultTableQualifier"},
                new String[]{RESULTS_SCHEMA}, new String[]{"batch_size"}, new Object[]{BATCH_SIZE});
    }

    @Test
    public void conceptBatches_coverAllConceptsInFullBatches() {

        PreparedStatementRenderer psr = conceptBatchRenderer();
        List<Pair<Integer, Integer>> ranges = jdbcTemplate.query(psr.getSql(), psr.getSetter(),
                (rs, rowNum) -> new ImmutablePair<>(rs.getInt("min_concept"), rs.getInt("max_concept")));
        ranges.sort(Comparator.comparing(Pair::getLeft));

        // 250,000 concepts in batches of 100,000 give three ranges, not one range per concept
        assertEquals(3, ranges.size());
        assertEquals(CONCEPT_ID_STEP, (int) ranges.get(0).getLeft());
        assertEquals(CONCEPT_COUNT * CONCEPT_ID_STEP, (int) ranges.get(2).getRight());
        for (int i = 1; i < ranges.size(); i++) {
            assertEquals("ranges must be contiguous", ranges.get(i - 1).getRight() + CONCEPT_ID_STEP,
                    (int) ranges.get(i).getLeft());
        }
        // every full batch spans exactly BATCH_SIZE consecutive rows (the first one starts at ordinal 1)
        assertEquals((BATCH_SIZE - 1) * CONCEPT_ID_STEP, ranges.get(0).getRight() - ranges.get(0).getLeft() + CONCEPT_ID_STEP);
        assertEquals(BATCH_SIZE * CONCEPT_ID_STEP, ranges.get(1).getRight() - ranges.get(1).getLeft() + CONCEPT_ID_STEP);
    }

    @Test
    public void conceptBatches_groupIdStaysIntegralOnDialectsWithoutIntegerDivision() {

        String sql = conceptBatchRenderer().getSql();
        for (String dialect : new String[]{"snowflake", "spark", "bigquery"}) {
            String translated = SqlTranslate.translateSql(sql, dialect).toLowerCase();
            assertTrue(dialect + " must floor the batch group id: " + translated,
                    translated.contains("floor(ordinal / ?)"));
        }
    }
}
