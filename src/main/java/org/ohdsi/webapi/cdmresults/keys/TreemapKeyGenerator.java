package org.ohdsi.webapi.cdmresults.keys;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import java.lang.annotation.Annotation;

public class TreemapKeyGenerator implements CacheKeyGenerator {
    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {

        CacheInvocationParameter[] parameters = cacheKeyInvocationContext.getKeyParameters();
        if (parameters.length == 2) {
            return new TreemapKey((String)parameters[0].getValue(), (String)parameters[1].getValue());
        }
        throw new IllegalArgumentException("Failed to generate treemap key, there should be 2 params");
    }
}
