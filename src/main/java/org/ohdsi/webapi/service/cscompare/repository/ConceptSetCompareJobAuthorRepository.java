package org.ohdsi.webapi.service.cscompare.repository;

import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobAuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptSetCompareJobAuthorRepository extends JpaRepository<ConceptSetCompareJobAuthorEntity, Integer> {
}