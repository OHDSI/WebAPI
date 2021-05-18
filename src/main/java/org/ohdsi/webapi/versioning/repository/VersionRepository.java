package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.AssetVersionBase;
import org.ohdsi.webapi.versioning.domain.AssetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface VersionRepository<T extends AssetVersion> extends JpaRepository<T, Long> {
    @Query("SELECT max(v.version) from #{#entityName} v WHERE v.assetId = ?1")
    Integer getLatestVersion(int assetId);

    @Query("SELECT v.id AS id, v.assetId AS assetId, v.comment AS comment, " +
            "v.version AS version, uc AS createdBy, " +
            "v.createdDate AS createdDate, v.archived AS archived " +
            "FROM #{#entityName} v, UserEntity uc " +
            "WHERE v.assetId = ?1 AND uc = v.createdBy")
    List<AssetVersionBase> findAllVersions(int assetId);

    @Query("SELECT v from #{#entityName} v WHERE v.assetId = ?1")
    List<T> findAll(int assetId);
}
