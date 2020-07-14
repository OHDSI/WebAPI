package org.ohdsi.webapi.cdmresults.eviction;

import org.ehcache.config.EvictionAdvisor;

public interface CDMEvictionAdvisor<K, V> extends EvictionAdvisor<K, V> {
    V getActualValue(K key, V current);
}
