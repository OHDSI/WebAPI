package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface IrVersionRepository extends VersionRepository<IRVersion> {
}