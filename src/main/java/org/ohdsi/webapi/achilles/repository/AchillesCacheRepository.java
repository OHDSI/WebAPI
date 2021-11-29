package org.ohdsi.webapi.achilles.repository;

import org.ohdsi.webapi.achilles.domain.AchillesCacheEntity;
import org.ohdsi.webapi.source.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchillesCacheRepository extends CrudRepository<AchillesCacheEntity, Long> {
    AchillesCacheEntity findBySourceAndCacheName(Source source, String type);

    @Query("select ac from AchillesCacheEntity ac where source = :source and cacheName in :names")
    List<AchillesCacheEntity> findBySourceAndNames(@Param("source") Source source, @Param("names") List<String> names);
}
