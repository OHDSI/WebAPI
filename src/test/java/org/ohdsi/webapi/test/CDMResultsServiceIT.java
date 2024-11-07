package org.ohdsi.webapi.test;

import com.odysseusinc.arachne.commons.types.DBMSType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

public class CDMResultsServiceIT extends WebApiIT {
    private static final String CDM_RESULTS_FILE_PATH = "/database/cdm_results.sql";

    @Value("${cdmService.endpoint.results}")
    private String cdmResultsEndpoint;

    @Autowired
    private SourceRepository sourceRepository;

    @Before
    public void init() throws Exception {
        truncateTable(String.format("%s.%s", "public", "source"));
        resetSequence(String.format("%s.%s", "public", "source_sequence"));
        sourceRepository.saveAndFlush(getCdmSource());
        prepareCdmSchema();
        prepareResultSchema();
        addCDMResults();
    }

    private void addCDMResults() {
        String resultSql = SqlRender.renderSql(ResourceHelper.GetResourceAsString(
                CDM_RESULTS_FILE_PATH),
                new String[] { "results_schema" }, new String[] { RESULT_SCHEMA_NAME });
        String sql = SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());
        jdbcTemplate.execute(sql);
    }

    @Test
    public void requestCDMResultsForConcept_firstTime_returnsResults() {

        // Arrange
        List<Integer> conceptIds = Arrays.asList(1);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("sourceName", SOURCE_KEY);

        List<LinkedHashMap<String, List<Integer>>> list = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Class<List<LinkedHashMap<String, List<Integer>>>> returnClass = (Class<List<LinkedHashMap<String, List<Integer>>>>)list.getClass(); 

        // Act
        final ResponseEntity<List<LinkedHashMap<String, List<Integer>>>> entity = getRestTemplate().postForEntity(this.cdmResultsEndpoint, conceptIds, 
                returnClass, queryParameters );

        // Assertion
        assertOK(entity);
        List<LinkedHashMap<String, List<Integer>>> results = entity.getBody();
        assertEquals(1, results.size());
        LinkedHashMap<String, List<Integer>> resultHashMap = results.get(0);
        assertEquals(1, resultHashMap.size());
        assertTrue(resultHashMap.containsKey("1"));
        List<Integer> counts = resultHashMap.get("1");
        assertEquals(100, counts.get(0).intValue());
        assertEquals(101, counts.get(1).intValue());
        assertEquals(102, counts.get(2).intValue());
        assertEquals(103, counts.get(3).intValue());
    }
}
