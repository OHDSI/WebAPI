package org.ohdsi.webapi.cohortcharacterization.repository;

import org.ohdsi.webapi.cohortcharacterization.domain.CcStrataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcStrataRepository extends JpaRepository<CcStrataEntity, Long> {
}
