package org.ohdsi.webapi.cdmresults.cache;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 *
 * This is really straightforward implementation of evictable set.
 * Data is evicted as soon as the size of the collection is reached cacheSizeForRequestedValues.
 * the evicted amount can be configured by evictionSizeForRequestedValues variable
 * @author ymolodkov
 */
public class SimpleEvictableConcurrentSet<E extends Comparable<?>> extends SetWrapper<E> {

    private static final int DEFAULT_CACHE_SIZE_FOR_REQUEST = 1_000_000; //~20mb for 8 char values.
    private static final int DEFAULT_EVICTION_SIZE_FOR_REQUEST = 200_000;

    private int cacheSizeForRequestedValues;
    private int evictionSizeForRequestedValues;

    public SimpleEvictableConcurrentSet(int cacheSizeForRequestedValues, int evictionSizeForRequestedValues) {

        super(new ConcurrentSkipListSet<>());
        this.cacheSizeForRequestedValues = cacheSizeForRequestedValues;
        this.evictionSizeForRequestedValues = evictionSizeForRequestedValues;
    }

    public SimpleEvictableConcurrentSet() {
        this(DEFAULT_CACHE_SIZE_FOR_REQUEST, DEFAULT_EVICTION_SIZE_FOR_REQUEST);
    }

    @Override
    public boolean add(E e) {
        evictIfNecessary();
        return this.values.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        collection.forEach(this::add);
        return true;
    }

    private void evictIfNecessary() {
        if (values.size() < cacheSizeForRequestedValues) {
            return;
        }
        values = values.stream()
                .skip(evictionSizeForRequestedValues)
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new));
    }


}
