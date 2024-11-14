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
import org.ohdsi.webapi.achilles.service.AchillesCacheService;
import org.ohdsi.webapi.cdmresults.service.CDMCacheService;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

public class CDMResultsServiceIT extends WebApiIT {
    private static final String CDM_RESULTS_FILE_PATH = "/database/cdm_results.sql";

    @Value("${cdmResultsService.endpoint.conceptRecordCount}")
    private String conceptRecordCountEndpoint;

    @Value("${cdmResultsService.endpoint.clearCache}")
    private String clearCacheEndpoint;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private AchillesCacheService achillesService;

    @Autowired
    private CDMCacheService cdmCacheService;

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
    public void requestConceptRecordCounts_firstTime_returnsResults() {

        // Arrange
        List<Integer> conceptIds = Arrays.asList(1);
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("sourceName", SOURCE_KEY);

        List<LinkedHashMap<String, List<Integer>>> list = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Class<List<LinkedHashMap<String, List<Integer>>>> returnClass = (Class<List<LinkedHashMap<String, List<Integer>>>>)list.getClass(); 

        // Act
        final ResponseEntity<List<LinkedHashMap<String, List<Integer>>>> entity = getRestTemplate().postForEntity(this.conceptRecordCountEndpoint, conceptIds, 
                returnClass, queryParameters );

        // Assert
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

    @Test
    public void achillesService_clearCache_nothingInCache_doesNothing() {

        // Arrange

        // Act
        achillesService.clearCache();

        // Assert
        String sql = "SELECT COUNT(*) FROM achilles_cache";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(0, count.intValue());
    }

    @Test
    public void achillesService_clearCache_somethingInCache_clearsAllRowsForSource() {

      // Arrange
      String insertSqlRow1 = "INSERT INTO achilles_cache (id, source_id, cache_name, cache) VALUES (1, 1, 'cache1', 'cache1')";  
      jdbcTemplate.execute(insertSqlRow1);
      String insertSqlRow2 = "INSERT INTO achilles_cache (id, source_id, cache_name, cache) VALUES (2, 1, 'cache2', 'cache2')";
      jdbcTemplate.execute(insertSqlRow2);

      // Act
      achillesService.clearCache();

      // Assert
      String sql = "SELECT COUNT(*) FROM achilles_cache";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
      assertEquals(0, count.intValue());
    }

    @Test
    public void cdmCacheService_clearCache_nothingInCache_doesNothing() {

      // Arrange

      // Act
      cdmCacheService.clearCache();

      // Assert
      String sql = "SELECT COUNT(*) FROM cdm_cache";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
      assertEquals(0, count.intValue());
    }

    @Test
    public void cdmCacheService_clearCache_somethingInCache_clearsAllRowsForSource() {

      // Arrange
      String insertSqlRow1 = "INSERT INTO cdm_cache (id, concept_id, source_id, record_count, descendant_record_count, person_count, descendant_person_count) VALUES (1, 1, 1, 100, 101, 102, 103)";
      jdbcTemplate.execute(insertSqlRow1);
      String insertSqlRow2 = "INSERT INTO cdm_cache (id, concept_id, source_id, record_count, descendant_record_count, person_count, descendant_person_count) VALUES (2, 2, 1, 200, 201, 202, 203)";
      jdbcTemplate.execute(insertSqlRow2);

      // Act
      cdmCacheService.clearCache();

      // Assert
      String sql = "SELECT COUNT(*) FROM cdm_cache";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
      assertEquals(0, count.intValue());
    }
  }
