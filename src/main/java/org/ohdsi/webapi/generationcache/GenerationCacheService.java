package org.ohdsi.webapi.generationcache;

import org.ohdsi.webapi.source.Source;

public interface GenerationCacheService {

    String getDesignHash(CacheableGenerationType type, String design);
    GenerationCache getCacheOrEraseInvalid(CacheableGenerationType type, String designHash, Integer sourceId);
    Integer getNextResultIdentifier(CacheableGenerationType type, Source source);
    String getResultsSql(GenerationCache cache);
    GenerationCache cacheResults(CacheableGenerationType type, String designHash, Integer sourceId, Integer resultIdentifier);
    void removeCache(CacheableGenerationType type, Source source, Integer resultIdentifier);
}
