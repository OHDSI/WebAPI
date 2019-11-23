package org.ohdsi.webapi.generationcache;

import org.ohdsi.webapi.source.Source;

public interface GenerationCacheProvider {

    boolean supports(CacheableGenerationType type);
    String getDesignHash(String design);
    String getResultsChecksum(Source source, Integer resultIdentifier);
    String getResultsSql(Integer resultIdentifier);
    void remove(Source source, Integer resultIdentifier);
}
