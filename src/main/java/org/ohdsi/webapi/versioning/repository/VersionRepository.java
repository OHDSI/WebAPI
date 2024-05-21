package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface VersionRepository<T extends Version> extends JpaRepository<T, VersionPK> {
    @Query("SELECT max(v.pk.version) from #{#entityName} v WHERE v.pk.assetId = ?1")
    Integer getLatestVersion(long assetId);

    @Query("SELECT new org.ohdsi.webapi.versioning.domain.VersionBase(v.pk.assetId, v.comment, " +
            "v.pk.version, uc, v.createdDate, v.archived) " +
            "FROM #{#entityName} v " +
            "LEFT JOIN UserEntity uc " +
            "ON uc = v.createdBy " +
            "WHERE v.pk.assetId = ?1")
    List<VersionBase> findAllVersions(long assetId);

    @Query("SELECT v from #{#entityName} v WHERE v.pk.assetId = ?1")
    List<T> findAll(int assetId);
}
