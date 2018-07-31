package org.ohdsi.webapi.cohortcharacterization;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcRepository extends JpaRepository<CohortCharacterizationEntity, Long> {
    Optional<CohortCharacterizationEntity> findById(final Long id);
}
