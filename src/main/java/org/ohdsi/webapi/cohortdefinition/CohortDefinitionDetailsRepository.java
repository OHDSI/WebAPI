package org.ohdsi.webapi.cohortdefinition;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortDefinitionDetailsRepository extends JpaRepository<CohortDefinitionDetails, Long> {
    List<CohortDefinitionDetails> findByHashCode(Integer hashCode);
}
