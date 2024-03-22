package org.ohdsi.webapi.cohortcharacterization.repository;

import org.ohdsi.webapi.cohortcharacterization.domain.CcFeAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcFeAnalysisRepository extends JpaRepository<CcFeAnalysisEntity, Long> {
}
