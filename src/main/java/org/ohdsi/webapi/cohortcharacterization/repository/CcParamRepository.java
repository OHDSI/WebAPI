package org.ohdsi.webapi.cohortcharacterization.repository;

import java.util.Set;
import org.ohdsi.webapi.cohortcharacterization.domain.CcParamEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcParamRepository extends JpaRepository<CcParamEntity, Long> {
    Set<CcParamEntity> findAllByCohortCharacterization(CohortCharacterizationEntity mainEntity);
}
