package org.ohdsi.webapi.cohortdefinition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CohortDefinitionDetailsRepository extends JpaRepository<CohortDefinitionDetails, Long> {
    List<CohortDefinitionDetails> findByHashCode(Integer hashCode);
}
