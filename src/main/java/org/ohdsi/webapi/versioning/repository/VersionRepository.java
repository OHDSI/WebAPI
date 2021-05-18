package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface VersionRepository<T extends Version> extends JpaRepository<T, Long> {
    @Query("SELECT max(v.version) from #{#entityName} v WHERE v.assetId = ?1")
    Integer getLatestVersion(int assetId);

    @Query("SELECT new org.ohdsi.webapi.versioning.domain.VersionBase(v.id, v.assetId, v.comment, " +
            "v.version, uc, v.createdDate, v.archived) " +
            "FROM #{#entityName} v, UserEntity uc " +
            "WHERE v.assetId = ?1 AND uc = v.createdBy")
    List<VersionBase> findAllVersions(int assetId);

    @Query("SELECT v from #{#entityName} v WHERE v.assetId = ?1")
    List<T> findAll(int assetId);
}
