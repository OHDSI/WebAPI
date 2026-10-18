package org.ohdsi.webapi.cdmresults.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.cdmresults.repository.CDMCacheRepository;
import org.ohdsi.webapi.source.Source;

@RunWith(MockitoJUnitRunner.class)
public class CDMCacheBatchServiceTest {

    private static final int SOURCE_ID = 1;
    private static final int CONCEPT_ID = 201826;

    @Mock
    private CDMCacheRepository cdmCacheRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Source source;

    @Captor
    private ArgumentCaptor<List<CDMCacheEntity>> savedCaptor;

    private CDMCacheBatchService cdmCacheBatchService;

    @Before
    public void setUp() {

        when(source.getSourceId()).thenReturn(SOURCE_ID);
        cdmCacheBatchService = new CDMCacheBatchService(cdmCacheRepository, entityManager);
    }

    private static CDMCacheEntity recordCountsOnly(long recordCount, long descendantRecordCount) {

        // cache entries built when cache.achilles.usePersonCount=false carry no person counts
        CDMCacheEntity entity = new CDMCacheEntity();
        entity.setConceptId(CONCEPT_ID);
        entity.setRecordCount(recordCount);
        entity.setDescendantRecordCount(descendantRecordCount);
        return entity;
    }

    private void givenCachedEntity(CDMCacheEntity cached) {

        cached.setSourceId(SOURCE_ID);
        when(cdmCacheRepository.findBySourceAndConceptIds(anyInt(), anyList()))
                .thenReturn(Collections.singletonList(cached));
    }

    @Test
    public void save_withoutPersonCounts_unchangedEntityIsNotSavedAgain() {

        givenCachedEntity(recordCountsOnly(100, 150));

        cdmCacheBatchService.save(source, Collections.singletonList(recordCountsOnly(100, 150)));

        verify(cdmCacheRepository, never()).saveAll(any());
    }

    @Test
    public void save_withoutPersonCounts_changedEntityIsUpdated() {

        givenCachedEntity(recordCountsOnly(100, 150));

        cdmCacheBatchService.save(source, Collections.singletonList(recordCountsOnly(120, 180)));

        verify(cdmCacheRepository).saveAll(savedCaptor.capture());
        List<CDMCacheEntity> saved = savedCaptor.getValue();
        assertEquals(1, saved.size());
        assertEquals(CONCEPT_ID, saved.get(0).getConceptId());
        assertEquals(Long.valueOf(120), saved.get(0).getRecordCount());
        assertEquals(Long.valueOf(180), saved.get(0).getDescendantRecordCount());
    }
}
