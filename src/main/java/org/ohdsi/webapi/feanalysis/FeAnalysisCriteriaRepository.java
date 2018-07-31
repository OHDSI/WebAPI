package org.ohdsi.webapi.feanalysis;

import java.util.Set;
import org.ohdsi.webapi.cohortcharacterization.CohortCharacterizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeAnalysisCriteriaRepository extends JpaRepository<FeAnalysisCriteriaEntity, Long> {
}
