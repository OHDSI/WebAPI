package org.ohdsi.webapi.cohortcharacterization.repository;

import java.util.Optional;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcRepository extends JpaRepository<CohortCharacterizationEntity, Long> {
    Optional<CohortCharacterizationEntity> findById(final Long id);
}
