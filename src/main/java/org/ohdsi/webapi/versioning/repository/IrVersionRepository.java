package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.IrVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface IrVersionRepository extends VersionRepository<IrVersion> {
}