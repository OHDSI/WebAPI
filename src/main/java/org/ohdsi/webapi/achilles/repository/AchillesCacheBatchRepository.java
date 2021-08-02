package org.ohdsi.webapi.achilles.repository;

import org.ohdsi.webapi.achilles.domain.AchillesCacheEntity;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class AchillesCacheBatchRepository extends SimpleJpaRepository<AchillesCacheEntity, Long> {
    private final EntityManager entityManager;

    public AchillesCacheBatchRepository(EntityManager entityManager) {
        super(AchillesCacheEntity.class, entityManager);
        this.entityManager = entityManager;
    }

    public List<AchillesCacheEntity> save(List<AchillesCacheEntity> entities) {
        entities.forEach(entityManager::persist);
        return entities;
    }
}
