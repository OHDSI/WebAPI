package org.ohdsi.webapi.cohortdefinition;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortDefinitionDetailsRepository extends JpaRepository<CohortDefinitionDetailsEntity, Long> {
    List<CohortDefinitionDetailsEntity> findByHashCode(Integer hashCode);
}
