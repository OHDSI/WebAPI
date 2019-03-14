package org.ohdsi.webapi.cohortcharacterization.repository;

import java.util.Optional;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;

public interface CcRepository extends EntityGraphJpaRepository<CohortCharacterizationEntity, Long> {
    Optional<CohortCharacterizationEntity> findById(final Long id);
    int countByNameStartsWith(String pattern);
    Optional<CohortCharacterizationEntity> findByName(String name);
}
