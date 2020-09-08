package org.ohdsi.webapi.cdmresults.keys;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import java.lang.annotation.Annotation;

public class RefreshableSourceKeyGenerator implements CacheKeyGenerator {
    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {

        CacheInvocationParameter[] params = cacheKeyInvocationContext.getKeyParameters();
        if (params.length == 2) {
            return new RefreshableSourceKey((String) params[0].getValue(), (Boolean)params[1].getValue());
        }
        throw new IllegalArgumentException("Failed to generate CDMPersonSummary key, should be 2 parameters");
    }
}
