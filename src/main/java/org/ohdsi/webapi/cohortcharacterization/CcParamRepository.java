package org.ohdsi.webapi.cohortcharacterization;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcParamRepository extends JpaRepository<CcParamEntity, Long> {
    Set<CcParamEntity> findAllByCohortCharacterization(CohortCharacterizationEntity mainEntity);
}
