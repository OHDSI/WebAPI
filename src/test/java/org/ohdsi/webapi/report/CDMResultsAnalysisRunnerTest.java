package org.ohdsi.webapi.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Regression tests for https://github.com/OHDSI/WebAPI/issues/2208:
 * a SQL failure during an Achilles drilldown used to be swallowed and the empty
 * result persisted in the CDM cache (via the @AchillesCache aspect), leaving users
 * with empty drilldown reports that could not be refreshed.
 */
public class CDMResultsAnalysisRunnerTest {

    private CDMResultsAnalysisRunner runner;
    private JdbcTemplate jdbcTemplate;
    private Source source;

    @Before
    public void setUp() {

        runner = new CDMResultsAnalysisRunner();
        runner.init("postgresql", new ObjectMapper());

        jdbcTemplate = mock(JdbcTemplate.class);
        source = mock(Source.class);
        when(source.getTableQualifier(SourceDaimon.DaimonType.Results)).thenReturn("results");
        when(source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn("vocab");
        when(source.getTableQualifier(SourceDaimon.DaimonType.CDM)).thenReturn("cdm");
        when(source.getSourceDialect()).thenReturn("postgresql");
    }

    @Test
    public void getDrilldown_propagatesQueryFailureInsteadOfReturningEmptyResult() {

        // Simulate a SQL failure during the drilldown (e.g. unreachable results schema)
        when(jdbcTemplate.query(anyString(), any(), any(RowMapper.class)))
                .thenThrow(new DataAccessResourceFailureException("simulated SQL failure"));

        try {
            JsonNode result = runner.getDrilldown(jdbcTemplate, "condition", null, source);
            fail("Expected getDrilldown to propagate the SQL failure instead of returning " + result);
        } catch (RuntimeException expected) {
            // The failure must reach the caller so the @AchillesCache aspect does not cache an
            // empty report; the original SQL exception must be preserved as the cause.
            assertEquals("simulated SQL failure", expected.getCause().getMessage());
        }
    }
}
