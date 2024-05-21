package org.ohdsi.webapi.cdmresults.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.cdmresults.DescendantRecordCount;

@RunWith(MockitoJUnitRunner.class)
public class CDMResultsCacheTest {

    private CDMResultsCache cache = new CDMResultsCache();

    @Mock
    private Function<List<Integer>, List<DescendantRecordCount>> function;

    @Captor
    private ArgumentCaptor<List<Integer>> idForRequestCaptor;

    @Test
    public void warm() {

        assertFalse(cache.isWarm());
        cache.warm();
        assertTrue(cache.isWarm());
    }

    @Test
    public void findAndCache_nothingCached() {

        List<Integer> ids = Arrays.asList(1, 2, 3, 4);

        when(function.apply(any())).thenReturn(
                ids.stream().map(CDMResultsCacheTest.this::createDescendantRecordCount).collect(Collectors.toList())
        );

        Collection<DescendantRecordCount> records = cache.findAndCache(ids, function);
        assertEquals(4, records.size());
        assertEquals(ids, records.stream().map(DescendantRecordCount::getId).collect(Collectors.toList()));
    }

    @Test
    public void findAndCache_getAllDataFromCache() {

        List<Integer> ids = Arrays.asList(1, 2, 3, 4);

        when(function.apply(any())).thenReturn(
                ids.stream().map(CDMResultsCacheTest.this::createDescendantRecordCount).collect(Collectors.toList())
        );

        cache.findAndCache(ids, function);
        cache.findAndCache(ids, function);
        Collection<DescendantRecordCount> records  = cache.findAndCache(ids, function);

        verify(function, only()).apply(any());
        assertEquals(ids, records.stream().map(DescendantRecordCount::getId).collect(Collectors.toList()));

    }


    @Test
    public void findAndCache_getOnlySomeDataFromCache() {

        List<Integer> ids1 = Arrays.asList(1, 2);
        List<Integer> ids2 = Arrays.asList(1, 2, 3, 4);

        when(function.apply(any())).then(invocation -> {
                    List<Integer> ids = invocation.getArgumentAt(0, List.class);
                    return ids.stream().map(CDMResultsCacheTest.this::createDescendantRecordCount).collect(Collectors.toList());
                }

        );

        cache.findAndCache(ids1, function);
        cache.findAndCache(ids2, function);
        verify(function, times(2)).apply(idForRequestCaptor.capture());

        assertEquals(Arrays.asList(1, 2), idForRequestCaptor.getAllValues().get(0));
        assertEquals(Arrays.asList(3, 4), idForRequestCaptor.getAllValues().get(1));
    }

    @Test
    public void findAndCache_idFromRequestThatDoesNotPresentInStorage() {

        List<Integer> ids1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> ids2 = Arrays.asList(1, 2);

        when(function.apply(any())).then(invocation -> {
                    List<Integer> ids = invocation.getArgumentAt(0, List.class);
                    return ids2.stream().map(CDMResultsCacheTest.this::createDescendantRecordCount).collect(Collectors.toList());
                }

        );

        cache.findAndCache(ids1, function);
        cache.findAndCache(ids1, function);
        verify(function, only()).apply(idForRequestCaptor.capture());

        assertEquals(ids1, idForRequestCaptor.getAllValues().get(0));
        assertNotNull(cache.get(1));
        assertNotNull(cache.get(2));
        assertNull(cache.get(3));
        assertNull(cache.get(4));
    }


    @Test
    public void addValue() {

        cache.cacheValue(createDescendantRecordCount(1));
        cache.cacheValue(createDescendantRecordCount(2));

        assertEquals(new Long(1_001L), cache.get(1).getRecordCount());
        assertEquals(new Long(1_000_001L), cache.get(1).getDescendantRecordCount());
        assertEquals(new Long(1_002L), cache.get(2).getRecordCount());
        assertEquals(new Long(1_000_002L), cache.get(2).getDescendantRecordCount());

    }

    @Test
    public void addValues() {

        final List<DescendantRecordCount> records = IntStream.range(1, 10).boxed()
                .map(this::createDescendantRecordCount)
                .collect(Collectors.toList());
        cache.cacheValues(records);

        records.forEach(record ->
                assertNotNull(cache.get(record.getId()))
        );
    }

    @Test
    public void get() {

        cache.cacheValue(createDescendantRecordCount(1));
        assertEquals(new Long(1_001L), cache.get(1).getRecordCount());
        assertEquals(new Long(1_000_001L), cache.get(1).getDescendantRecordCount());
    }

    @Test
    public void cacheRequestedIds() {

        final List<Integer> ids = IntStream.range(1, 5).boxed()
                .collect(Collectors.toList());
        cache.cacheRequestedIds(ids);
        ids.forEach(id ->
                assertTrue(cache.isRequested(id))
        );
    }

    @Test
    public void cacheRequestedId() {

        cache.cacheRequestedId(1);
        cache.cacheRequestedId(2);
        assertTrue(cache.isRequested(1));
        assertTrue(cache.isRequested(2));
        assertFalse(cache.isRequested(3));
    }

    @Test
    public void isRequested() {

        cache.cacheRequestedId(1);
        assertTrue(cache.isRequested(1));
    }

    @Test
    public void notRequested() {

        cache.cacheRequestedId(1);
        assertFalse(cache.isRequested(2));
    }

    @Test
    public void isWarm() {

        cache.warm();
        assertTrue(cache.isWarm());
        assertFalse(cache.notWarm());
    }



    private DescendantRecordCount createDescendantRecordCount(int id) {

        long recordCount = 1_000 + id;
        long descendantRecordCount = 1_000_000 + id;
        DescendantRecordCount value = new DescendantRecordCount();
        value.setId(id);
        value.setRecordCount(recordCount);
        value.setDescendantRecordCount(descendantRecordCount);
        return value;
    }


}
