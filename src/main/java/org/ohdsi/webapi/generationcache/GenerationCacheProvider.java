package org.ohdsi.webapi.generationcache;

import org.ohdsi.webapi.source.Source;

public interface GenerationCacheProvider {

    boolean supports(CacheableGenerationType type);
    Integer getDesignHash(String design);
    String getResultsChecksum(Source source, Integer designHash);
    String getResultsSql(Integer designHash);
    void remove(Source source, Integer designHash);
}
