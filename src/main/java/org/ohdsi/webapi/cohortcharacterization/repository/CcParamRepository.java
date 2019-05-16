package org.ohdsi.webapi.cohortcharacterization.repository;

import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface CcParamRepository extends JpaRepository<CcParamEntity, Long> {
    Set<CcParamEntity> findAllByCohortCharacterization(CohortCharacterizationEntity mainEntity);
}
