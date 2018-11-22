package org.ohdsi.webapi.cohortcharacterization.repository;

import org.ohdsi.webapi.cohortcharacterization.domain.CcConceptSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcConceptSetRepository extends JpaRepository<CcConceptSetEntity, Long> {
}
