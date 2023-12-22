package org.ohdsi.webapi.shiny;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShinyPublishedRepository extends JpaRepository<ShinyPublishedEntity, Long> {
    Optional<ShinyPublishedEntity> findByAnalysisIdAAndSourceKey(Long id, String sourceKey);
}
