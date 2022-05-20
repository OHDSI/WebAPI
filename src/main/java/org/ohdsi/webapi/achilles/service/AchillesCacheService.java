package org.ohdsi.webapi.achilles.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ohdsi.webapi.achilles.domain.AchillesCacheEntity;
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
    private static final Logger LOG = LoggerFactory.getLogger(AchillesCacheService.class);

    private final AchillesCacheRepository cacheRepository;

    private final ObjectMapper objectMapper;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    public AchillesCacheService(AchillesCacheRepository cacheRepository,
                                ObjectMapper objectMapper) {
        this.cacheRepository = cacheRepository;
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
        if (conceptNodes.isEmpty()) { // nothing to cache
            LOG.warn("Cannot cache drilldown reports for {}, domain {}. Check if result schema contains achilles results tables.",
                    source.getSourceKey(), domain);
            return;
        }

        Map<String, ObjectNode> nodes = new HashMap<>(batchSize);
        for (Map.Entry<Integer, ObjectNode> entry : conceptNodes.entrySet()) {
            if (nodes.size() >= batchSize) {
                createCacheEntities(source, nodes);
                nodes.clear();
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
        cacheRepository.save(cacheEntities);
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
        return String.format("drilldown_%s_%d", domain, conceptId);
    }
}
