package org.ohdsi.webapi;

import javax.cache.CacheManager;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy(false)
@EnableCaching
public class CacheConfig {

    @Value("${cache.heap.smallEntries:50}")
    private long smallEntries;

    @Value("${cache.heap.mediumEntries:500}")
    private long mediumEntries;

    @Bean
    public JCacheManagerCustomizer webApiCacheManagerCustomizer() {
        return cacheManager -> {
            createHeapCache(cacheManager, "cohortDefinitionList", smallEntries);
            createHeapCache(cacheManager, "conceptSetList", smallEntries);
            createHeapCache(cacheManager, "authorizationInfo", smallEntries);
            createHeapCache(cacheManager, "conceptDetail", mediumEntries);
            createHeapCache(cacheManager, "conceptRelated", mediumEntries);
            createHeapCache(cacheManager, "conceptHierarchy", mediumEntries);
            createHeapCache(cacheManager, "sourceList", smallEntries);
        };
    }

    private static void createHeapCache(CacheManager cacheManager, String name, long heapEntries) {
        if (cacheManager.getCache(name) != null) {
            return;
        }
        org.ehcache.config.CacheConfiguration<Object, Object> ehConfig =
                CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(Object.class, Object.class,
                                ResourcePoolsBuilder.heap(heapEntries))
                        .build();
        cacheManager.createCache(name, Eh107Configuration.fromEhcacheCacheConfiguration(ehConfig));
    }

}
