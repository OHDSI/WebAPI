package org.ohdsi.webapi.achilles.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.javafx.binding.StringFormatter;
import org.ohdsi.webapi.achilles.domain.AchillesCacheEntity;
import org.ohdsi.webapi.achilles.repository.AchillesCacheBatchRepository;
import org.ohdsi.webapi.achilles.repository.AchillesCacheRepository;
import org.ohdsi.webapi.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AchillesCacheService {
    private final AchillesCacheRepository cacheRepository;

    private final AchillesCacheBatchRepository batchRepository;

    private final ObjectMapper objectMapper;

    @Value("${cache.achilles.batchSize:100}")
    private int cacheBatchSize;

    public AchillesCacheService(AchillesCacheRepository cacheRepository,
                                AchillesCacheBatchRepository batchRepository,
                                ObjectMapper objectMapper) {
        this.cacheRepository = cacheRepository;
        this.batchRepository = batchRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AchillesCacheEntity getCache(Source source, String cacheName) {
        return cacheRepository.findBySourceAndCacheName(source, cacheName);
    }

    @Transactional(readOnly = true)
    public List<AchillesCacheEntity> findBySourceAndNames(Source source, List<String> names) {
        return cacheRepository.findBySourceAndNames(source, names);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AchillesCacheEntity createCache(Source source, String cacheName, Object result) throws JsonProcessingException {
        AchillesCacheEntity cacheEntity = getCache(source, cacheName);
        String cache = objectMapper.writeValueAsString(result);
        if (Objects.nonNull(cacheEntity)) {
            cacheEntity.setCache(cache);
        } else {
            cacheEntity = new AchillesCacheEntity();
            cacheEntity.setSource(source);
            cacheEntity.setCacheName(cacheName);
            cacheEntity.setCache(cache);
        }

        return cacheRepository.save(cacheEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDrilldownCacheMap(Source source, String domain, Map<Integer, ObjectNode> conceptNodes) {
        int batchCount = 0;
        Map<String, ObjectNode> nodes = new HashMap<>(cacheBatchSize);
        for (Map.Entry<Integer, ObjectNode> entry : conceptNodes.entrySet()) {
            if (batchCount++ >= cacheBatchSize) {
                createCacheEntities(source, nodes);
                nodes.clear();
                batchCount = 0;
            }
            Integer key = entry.getKey();
            String cacheName = getCacheName(domain, key);
            nodes.put(cacheName, entry.getValue());
        }
        createCacheEntities(source, nodes);
        nodes.clear();
    }

    private void createCacheEntities(Source source, Map<String, ObjectNode> nodes) {
        List<AchillesCacheEntity> cacheEntities = getEntities(source, nodes);
        batchRepository.save(cacheEntities);
    }

    private List<AchillesCacheEntity> getEntities(Source source, Map<String, ObjectNode> nodes) {
        List<String> cacheNames = new ArrayList<>(nodes.keySet());
        List<AchillesCacheEntity> cacheEntities = findBySourceAndNames(source, cacheNames);
        nodes.forEach((key, value) -> {
            // check if the entity with given cache name already exists
            Optional<AchillesCacheEntity> cacheEntity = cacheEntities.stream()
                    .filter(entity -> entity.getCacheName().equals(key))
                    .findAny();
            try {
                String newValue = objectMapper.writeValueAsString(value);
                if (cacheEntity.isPresent()) {
                    // if cache entity already exists update its value
                    cacheEntity.get().setCache(newValue);
                } else {
                    // if cache entity does not exist - create new one
                    AchillesCacheEntity newEntity = new AchillesCacheEntity();
                    newEntity.setCacheName(key);
                    newEntity.setSource(source);
                    newEntity.setCache(newValue);
                    cacheEntities.add(newEntity);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return cacheEntities;
    }

    private String getCacheName(String domain, int conceptId) {
        return StringFormatter.format("drilldown_%s_%d", domain, conceptId).getValue();
    }
}
