package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterizationVersionRepository extends VersionRepository<CharacterizationVersion> {
}