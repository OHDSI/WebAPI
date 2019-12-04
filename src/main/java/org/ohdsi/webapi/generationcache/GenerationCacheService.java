package org.ohdsi.webapi.generationcache;

import org.ohdsi.webapi.source.Source;

public interface GenerationCacheService {

    Integer getDesignHash(CacheableGenerationType type, String design);
    GenerationCache getCacheOrEraseInvalid(CacheableGenerationType type, Integer designHash, Integer sourceId);
    String getResultsSql(GenerationCache cache);
    GenerationCache cacheResults(CacheableGenerationType type, Integer designHash, Integer sourceId);
    void removeCache(CacheableGenerationType type, Source source, Integer designHash);
}
