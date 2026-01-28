package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.cache.CacheInfo;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.util.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Cache management controller.
 * Provides endpoints for viewing and clearing application caches.
 */
@RestController
@RequestMapping("/cache")
public class CacheMvcController extends AbstractMvcController {

    public static class ClearCacheResult {
        public List<CacheInfo> clearedCaches;

        private ClearCacheResult() {
            this.clearedCaches = new ArrayList<>();
        }
    }

    private final CacheManager cacheManager;

    @Autowired(required = false)
    public CacheMvcController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get list of all caches with statistics.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CacheInfo>> getCacheInfoList() {
        List<CacheInfo> caches = new ArrayList<>();

        if (cacheManager == null) {
            return ok(caches); // caching is disabled
        }

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            CacheInfo info = new CacheInfo();
            info.cacheName = cacheName;
            info.entries = StreamSupport.stream(cache.spliterator(), false).count();
            info.cacheStatistics = CacheHelper.getCacheStats(cacheManager, cacheName);
            caches.add(info);
        }
        return ok(caches);
    }

    /**
     * Clear all caches.
     */
    @GetMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClearCacheResult> clearAll() {
        ClearCacheResult result = new ClearCacheResult();

        if (cacheManager == null) {
            return ok(result);
        }

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            CacheInfo info = new CacheInfo();
            info.cacheName = cacheName;
            info.entries = StreamSupport.stream(cache.spliterator(), false).count();
            result.clearedCaches.add(info);
            cache.clear();
        }
        return ok(result);
    }
}
