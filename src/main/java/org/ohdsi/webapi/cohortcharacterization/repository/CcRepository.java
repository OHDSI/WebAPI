package org.ohdsi.webapi.cohortcharacterization.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CcRepository extends EntityGraphJpaRepository<CohortCharacterizationEntity, Long>, JpaSpecificationExecutor {
    Optional<CohortCharacterizationEntity> findById(final Long id);
}
