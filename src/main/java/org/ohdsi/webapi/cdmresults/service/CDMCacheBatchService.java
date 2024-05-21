package org.ohdsi.webapi.cdmresults.service;

import java.util.ArrayList;
import java.util.Arrays;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.cdmresults.repository.CDMCacheRepository;
import org.ohdsi.webapi.source.Source;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CDMCacheBatchService {
    private final CDMCacheRepository cdmCacheRepository;
    
    private final EntityManager entityManager;

    public CDMCacheBatchService(CDMCacheRepository cdmCacheRepository, EntityManager entityManager) {
        this.cdmCacheRepository = cdmCacheRepository;
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<CDMCacheEntity> save(Source source, List<CDMCacheEntity> entities) {
        List<Integer> conceptIds = entities.stream()
                .map(CDMCacheEntity::getConceptId)
                .collect(Collectors.toList());
        Map<Integer,CDMCacheEntity> cacheEntities = cdmCacheRepository.findBySourceAndConceptIds(source.getSourceId(), conceptIds)
                .stream()
                .collect(Collectors.toMap(CDMCacheEntity::getConceptId, Function.identity()));
        List<CDMCacheEntity> modified = new ArrayList<>();
        entities.forEach(entity -> {
            // check if the entity with given cache name already exists
            CDMCacheEntity processedEntity;
            if (cacheEntities.containsKey(entity.getConceptId())) {
                processedEntity = cacheEntities.get(entity.getConceptId());
                if (Arrays.equals(new long[] { entity.getPersonCount(),entity.getDescendantPersonCount(),entity.getRecordCount(),entity.getDescendantRecordCount()},
                    new long[] {processedEntity.getPersonCount(),processedEntity.getDescendantPersonCount(),processedEntity.getRecordCount(),processedEntity.getDescendantRecordCount()})) {
                  return;  // data hasn't changed, so move to next in forEach
                }
            } else {
                // if cache entity does not exist - create new one
                processedEntity = new CDMCacheEntity();
                processedEntity.setConceptId(entity.getConceptId());
                processedEntity.setSourceId(source.getSourceId());
                cacheEntities.put(processedEntity.getConceptId(), processedEntity);
            }
            processedEntity.setPersonCount(entity.getPersonCount());
            processedEntity.setDescendantPersonCount(entity.getDescendantPersonCount());
            processedEntity.setRecordCount(entity.getRecordCount());
            processedEntity.setDescendantRecordCount(entity.getDescendantRecordCount());
            modified.add(processedEntity);
        });
        if (!modified.isEmpty()) {
          cdmCacheRepository.save(modified);
        }
        return new ArrayList<>( cacheEntities.values());
    }
}
