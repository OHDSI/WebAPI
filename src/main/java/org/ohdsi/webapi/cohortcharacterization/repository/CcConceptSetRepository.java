package org.ohdsi.webapi.cohortcharacterization.repository;

import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataConceptSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcConceptSetRepository extends JpaRepository<CcStrataConceptSetEntity, Long> {
}
