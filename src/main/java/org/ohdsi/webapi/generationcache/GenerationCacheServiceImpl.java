package org.ohdsi.webapi.generationcache;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.google.common.base.MoreObjects;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
public class GenerationCacheServiceImpl implements GenerationCacheService {

    private static final Logger log = LoggerFactory.getLogger(GenerationCacheServiceImpl.class);
    private static final String NO_PROVIDER_ERROR = "There is no generation cache provider which supports %s";
    private static final String CACHE_CREATED = "Cached results of {} with design hash = {}";
    private static final String CACHE_INVALID = "Actual results checksum doesn't match the original checksum for cache with id={}. Invalidating cache";

    private static final ConcurrentHashMap<CacheableTypeSource, Integer> maxRequestedResultIds = new ConcurrentHashMap<>();

    private final List<GenerationCacheProvider> generationCacheProviderList;
    private final GenerationCacheRepository generationCacheRepository;
    private final SourceRepository sourceRepository;

    public GenerationCacheServiceImpl(List<GenerationCacheProvider> generationCacheProviderList, GenerationCacheRepository generationCacheRepository, SourceRepository sourceRepository) {

        this.generationCacheProviderList = generationCacheProviderList;
        this.generationCacheRepository = generationCacheRepository;
        this.sourceRepository = sourceRepository;
    }

    @Override
    public String getDesignHash(CacheableGenerationType type, String design) {

        return getProvider(type).getDesignHash(design);
    }

    @Override
    public GenerationCache getCacheOrEraseInvalid(CacheableGenerationType type, String designHash, Integer sourceId) {

        Source source = sourceRepository.findBySourceId(sourceId);
        GenerationCache generationCache = generationCacheRepository.findByTypeAndAndDesignHashAndSource(type, designHash, source, EntityGraphUtils.fromAttributePaths("source"));
        GenerationCacheProvider provider = getProvider(type);
        if (generationCache != null) {
            String checksum = provider.getResultsChecksum(generationCache.getSource(), generationCache.getResultIdentifier());
            if (Objects.equals(generationCache.getResultChecksum(), checksum)) {
                return generationCache;
            } else {
                removeCache(generationCache.getType(), generationCache.getSource(), generationCache.getResultIdentifier());
                log.info(CACHE_INVALID, generationCache.getId());
            }
        }
        return null;
    }

    @Override
    public synchronized Integer getNextResultIdentifier(CacheableGenerationType type, Source source) {

        CacheableTypeSource resIdKey = new CacheableTypeSource(type, source.getSourceId());

        Integer maxIdInCacheTable = MoreObjects.firstNonNull(generationCacheRepository.findMaxResultIdentifier(type, source.getSourceId()), 0);
        Integer maxIdInResultSchema = MoreObjects.firstNonNull(getProvider(type).getMaxResultIdentifier(source), 0);
        Integer maxRequestedId = maxRequestedResultIds.getOrDefault(resIdKey, 0);

        Integer nextId = Stream.of(maxIdInCacheTable, maxIdInResultSchema, maxRequestedId)
            .max(Comparator.naturalOrder())
            .orElse(0) + 1;

        maxRequestedResultIds.put(resIdKey, nextId);

        return nextId;
    }

    @Override
    public String getResultsSql(GenerationCache cache) {

        return getProvider(cache.getType()).getResultsSql(cache.getResultIdentifier());
    }

    @Override
    public GenerationCache cacheResults(CacheableGenerationType type, String designHash, Integer sourceId, Integer resultIdentifier) {

        Source source = sourceRepository.findBySourceId(sourceId);
        String checksum = getProvider(type).getResultsChecksum(source, resultIdentifier);

        GenerationCache generationCache = new GenerationCache();
        generationCache.setType(type);
        generationCache.setDesignHash(designHash);
        generationCache.setSource(source);
        generationCache.setResultIdentifier(resultIdentifier);
        generationCache.setResultChecksum(checksum);
        generationCache.setCreatedDate(new Date());

        generationCache = generationCacheRepository.saveAndFlush(generationCache);

        log.info(CACHE_CREATED, type, designHash);

        return generationCache;
    }

    @Override
    public void removeCache(CacheableGenerationType type, Source source, Integer resultIdentifier) {

        // Cleanup cached results
        getProvider(type).remove(source, resultIdentifier);
        // Cleanup cache record
        GenerationCache generationCache = generationCacheRepository.findByTypeAndSourceAndResultIdentifier(type, source, resultIdentifier, EntityGraphUtils.fromAttributePaths("source"));
        if (generationCache != null) {
            generationCacheRepository.delete(generationCache);
        }
    }

    private GenerationCacheProvider getProvider(CacheableGenerationType type) {

        return generationCacheProviderList.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format(NO_PROVIDER_ERROR, type)));
    }

    private class CacheableTypeSource {

        private CacheableGenerationType type;
        private Integer sourceId;

        public CacheableTypeSource(CacheableGenerationType type, Integer sourceId) {

            this.type = type;
            this.sourceId = sourceId;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheableTypeSource that = (CacheableTypeSource) o;
            return type == that.type &&
                    Objects.equals(sourceId, that.sourceId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(type, sourceId);
        }
    }
}
