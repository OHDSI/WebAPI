package org.ohdsi.webapi.generationcache;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import org.ohdsi.webapi.util.EntityUtils;

@Component
public class CleanupScheduler {

    private final GenerationCacheService generationCacheService;
    private final GenerationCacheRepository generationCacheRepository;

    @Value("${cache.generation.invalidAfterDays}")
    private Integer invalidateAfterDays;

    public CleanupScheduler(GenerationCacheService generationCacheService, GenerationCacheRepository generationCacheRepository) {

        this.generationCacheService = generationCacheService;
        this.generationCacheRepository = generationCacheRepository;
    }

    @Scheduled(fixedDelayString = "${cache.generation.cleanupInterval}")
    public void removeOldCache() {

        List<GenerationCache> caches = generationCacheRepository.findAllByCreatedDateBefore(
            DateUtils.addDays(new Date(), -1 * invalidateAfterDays),
            EntityUtils.fromAttributePaths("source", "source.daimons")
        );
        caches.forEach(gc -> generationCacheService.removeCache(gc.getType(), gc.getSource(), gc.getDesignHash()));
    }
}
