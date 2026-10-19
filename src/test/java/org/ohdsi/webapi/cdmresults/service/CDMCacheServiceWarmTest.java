package org.ohdsi.webapi.cdmresults.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.cdmresults.repository.CDMCacheRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs the full (batch) CDM cache warming queries against the embedded Postgres instance, with and
 * without person counts.
 */
public class CDMCacheServiceWarmTest extends AbstractDatabaseTest {

    private static final String RESULTS_SCHEMA = "cdm_cache_results";
    private static final String VOCABULARY_SCHEMA = "cdm_cache_vocabulary";

    @Autowired
    @Qualifier("conversionService")
    private ConversionService conversionService;

    private CDMCacheService cdmCacheService;
    private Source source;
    private final List<CDMCacheEntity> cachedEntities = new ArrayList<>();

    @Before
    public void setUp() {

        for (String schema : new String[]{RESULTS_SCHEMA, VOCABULARY_SCHEMA}) {
            jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %1$s CASCADE; CREATE SCHEMA %1$s;", schema));
        }
        jdbcTemplate.execute(String.format("CREATE TABLE %s.achilles_result_concept_count (concept_id INT, "
                + "record_count BIGINT, descendant_record_count BIGINT, person_count BIGINT, "
                + "descendant_person_count BIGINT)", RESULTS_SCHEMA));
        jdbcTemplate.execute(String.format("CREATE TABLE %s.concept (concept_id INT, concept_name VARCHAR(255))",
                VOCABULARY_SCHEMA));
        insertConceptCount(201826, 100, 150, 10, 15);
        insertConceptCount(1112807, 200, 250, 20, 25);
        jdbcTemplate.update(String.format("INSERT INTO %s.concept (concept_id, concept_name) VALUES (?, ?), (?, ?)",
                VOCABULARY_SCHEMA), 201826, "Type 2 diabetes mellitus", 1112807, "Aspirin");

        source = mock(Source.class);
        when(source.getTableQualifier(SourceDaimon.DaimonType.Results)).thenReturn(RESULTS_SCHEMA);
        when(source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary)).thenReturn(VOCABULARY_SCHEMA);
        when(source.getSourceDialect()).thenReturn("postgresql");

        // collect what the warming job would persist into the WebAPI cdm_cache table
        CDMCacheBatchService cdmCacheBatchService = mock(CDMCacheBatchService.class);
        when(cdmCacheBatchService.save(any(), anyList())).thenAnswer(invocation -> {
            List<CDMCacheEntity> entities = invocation.getArgument(1);
            cachedEntities.addAll(entities);
            return entities;
        });

        cdmCacheService = spy(new CDMCacheService(cdmCacheBatchService, conversionService, mock(CDMCacheRepository.class)));
        doReturn(new CancelableJdbcTemplate(getDataSource())).when(cdmCacheService).getSourceJdbcTemplate(source);
        doReturn(new TransactionTemplate(new DataSourceTransactionManager(getDataSource())))
                .when(cdmCacheService).getTransactionTemplateRequiresNew();
    }

    @After
    public void tearDown() {

        for (String schema : new String[]{RESULTS_SCHEMA, VOCABULARY_SCHEMA}) {
            jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schema));
        }
    }

    private void insertConceptCount(int conceptId, long recordCount, long descendantRecordCount, long personCount,
                                    long descendantPersonCount) {

        jdbcTemplate.update(String.format("INSERT INTO %s.achilles_result_concept_count (concept_id, record_count, "
                + "descendant_record_count, person_count, descendant_person_count) VALUES (?, ?, ?, ?, ?)",
                RESULTS_SCHEMA), conceptId, recordCount, descendantRecordCount, personCount, descendantPersonCount);
    }

    private List<CDMCacheEntity> warm(boolean usePersonCount) {

        ReflectionTestUtils.setField(cdmCacheService, "usePersonCount", usePersonCount);
        cdmCacheService.warm(source);
        cachedEntities.sort(Comparator.comparingInt(CDMCacheEntity::getConceptId));
        return cachedEntities;
    }

    @Test
    public void warm_withoutPersonCount_cachesRecordCounts() {

        List<CDMCacheEntity> cached = warm(false);

        assertEquals(2, cached.size());
        assertEquals(201826, cached.get(0).getConceptId());
        assertEquals(Long.valueOf(100), cached.get(0).getRecordCount());
        assertEquals(Long.valueOf(150), cached.get(0).getDescendantRecordCount());
        assertNull(cached.get(0).getPersonCount());
        assertEquals(1112807, cached.get(1).getConceptId());
        assertEquals(Long.valueOf(200), cached.get(1).getRecordCount());
        assertEquals(Long.valueOf(250), cached.get(1).getDescendantRecordCount());
    }

    @Test
    public void warm_withPersonCount_cachesRecordAndPersonCounts() {

        List<CDMCacheEntity> cached = warm(true);

        assertEquals(2, cached.size());
        assertEquals(201826, cached.get(0).getConceptId());
        assertEquals(Long.valueOf(100), cached.get(0).getRecordCount());
        assertEquals(Long.valueOf(150), cached.get(0).getDescendantRecordCount());
        assertEquals(Long.valueOf(10), cached.get(0).getPersonCount());
        assertEquals(Long.valueOf(15), cached.get(0).getDescendantPersonCount());
        assertEquals(1112807, cached.get(1).getConceptId());
        assertEquals(Long.valueOf(20), cached.get(1).getPersonCount());
        assertEquals(Long.valueOf(25), cached.get(1).getDescendantPersonCount());
    }
}
