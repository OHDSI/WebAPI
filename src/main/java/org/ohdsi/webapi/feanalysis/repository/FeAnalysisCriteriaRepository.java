package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeAnalysisCriteriaRepository extends JpaRepository<FeAnalysisCriteriaEntity, Long> {
}
