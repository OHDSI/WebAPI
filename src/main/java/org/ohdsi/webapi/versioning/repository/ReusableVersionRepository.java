package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.ReusableVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface ReusableVersionRepository extends VersionRepository<ReusableVersion> {
}
