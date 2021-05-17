package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.CohortCharacterizationVersion;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterizationVersionRepository extends VersionRepository<CohortCharacterizationVersion> {
}