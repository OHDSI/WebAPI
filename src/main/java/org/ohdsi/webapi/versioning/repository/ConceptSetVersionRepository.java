package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface ConceptSetVersionRepository extends VersionRepository<ConceptSetVersion> {
}