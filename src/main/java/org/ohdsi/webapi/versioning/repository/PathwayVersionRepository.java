package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.PathwayVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface PathwayVersionRepository extends VersionRepository<PathwayVersion> {
}