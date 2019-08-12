package org.ohdsi.webapi.cdmresults.cache;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class SimpleEvictableConcurrentSetTest {

    private SimpleEvictableConcurrentSet simpleEvictableConcurrentSet = new SimpleEvictableConcurrentSet<>(5, 3);

    @Test
    public void addAll_evict() {

        List<Integer> ids = IntStream.range(0, 12).boxed().collect(Collectors.toList());

        simpleEvictableConcurrentSet.addAll(ids);

        IntStream.range(0, 8)
                .forEach(id -> assertFalse(simpleEvictableConcurrentSet.contains(id)));
        IntStream.range(9, 3)
                .forEach(id -> assertFalse(simpleEvictableConcurrentSet.contains(id)));
    }

    @Test
    public void add_evict() {

        List<Integer> ids = IntStream.range(0, 12).boxed().collect(Collectors.toList());

        ids.forEach(simpleEvictableConcurrentSet::add);

        IntStream.range(0, 8)
                .forEach(id -> assertFalse(simpleEvictableConcurrentSet.contains(id)));
        IntStream.range(9, 3)
                .forEach(id -> assertFalse(simpleEvictableConcurrentSet.contains(id)));
    }



}