package org.ohdsi.webapi.versioning.repository;

import org.ohdsi.webapi.versioning.domain.AssetVersionBase;
import org.ohdsi.webapi.versioning.domain.AssetVersionFull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface VersionRepository<T extends AssetVersionFull> extends JpaRepository<T, Long> {
    @Query("SELECT max(v.version) from #{#entityName} v WHERE v.assetId = ?1")
    Integer getLatestVersion(int assetId);

    @Query("SELECT v.id AS id, v.assetId AS assetId, v.description AS description, " +
            "v.version AS version, uc AS createdBy, um AS modifiedBy, " +
            "v.createdDate AS createdDate, v.archived AS archived " +
            "FROM #{#entityName} v, UserEntity uc, UserEntity um " +
            "WHERE v.assetId = ?1 " +
            "AND uc = v.createdBy AND um = v.modifiedBy")
    List<AssetVersionBase> findAllVersions(int assetId);

    @Query("SELECT v from #{#entityName} v WHERE v.assetId = ?1")
    List<T> findAll(int assetId);
}
