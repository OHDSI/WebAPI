package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface CohortVersionRepository extends VersionRepository<CohortVersion> {
}