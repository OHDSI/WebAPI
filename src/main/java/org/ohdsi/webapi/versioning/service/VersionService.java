package org.ohdsi.webapi.versioning.service;

import org.ohdsi.webapi.exception.AtlasException;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionPK;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.repository.CharacterizationVersionRepository;
import org.ohdsi.webapi.versioning.repository.CohortVersionRepository;
import org.ohdsi.webapi.versioning.repository.ConceptSetVersionRepository;
import org.ohdsi.webapi.versioning.repository.IrVersionRepository;
import org.ohdsi.webapi.versioning.repository.PathwayVersionRepository;
import org.ohdsi.webapi.versioning.repository.ReusableVersionRepository;
import org.ohdsi.webapi.versioning.repository.VersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class VersionService<T extends Version> extends AbstractDaoService {
    @Value("${versioning.maxAttempt}")
    private int maxAttempt;

    private static final Logger logger = LoggerFactory.getLogger(VersionService.class);
    private final EntityManager entityManager;
    private final Map<VersionType, VersionRepository<T>> repositoryMap;

    @Autowired
    private VersionService<T> versionService;

    @Autowired
    public VersionService(
            EntityManager entityManager,
            CohortVersionRepository cohortRepository,
            ConceptSetVersionRepository conceptSetVersionRepository,
            CharacterizationVersionRepository characterizationVersionRepository,
            IrVersionRepository irRepository,
            PathwayVersionRepository pathwayRepository,
            ReusableVersionRepository reusableRepository) {
        this.entityManager = entityManager;

        this.repositoryMap = new HashMap<>();
        this.repositoryMap.put(VersionType.COHORT, (VersionRepository<T>) cohortRepository);
        this.repositoryMap.put(VersionType.CONCEPT_SET, (VersionRepository<T>) conceptSetVersionRepository);
        this.repositoryMap.put(VersionType.CHARACTERIZATION, (VersionRepository<T>) characterizationVersionRepository);
        this.repositoryMap.put(VersionType.INCIDENCE_RATE, (VersionRepository<T>) irRepository);
        this.repositoryMap.put(VersionType.PATHWAY, (VersionRepository<T>) pathwayRepository);
        this.repositoryMap.put(VersionType.REUSABLE, (VersionRepository<T>) reusableRepository);
    }

    private VersionRepository<T> getRepository(VersionType type) {
        return repositoryMap.get(type);
    }

    public List<VersionBase> getVersions(VersionType type, long assetId) {
        return getRepository(type).findAllVersions(assetId);
    }

    public T create(VersionType type, T assetVersion) {
        int attemptsCounter = 0;
        boolean saved = false;
        // Trying to save current version. Current version is selected from database
        // If current version number is already used - get latest version from database again and try to save.
        while (!saved && attemptsCounter < maxAttempt) {
            attemptsCounter++;

            Integer latestVersion = getRepository(type).getLatestVersion(assetVersion.getAssetId());
            if (Objects.nonNull(latestVersion)) {
                assetVersion.setVersion(latestVersion + 1);
            } else {
                assetVersion.setVersion(1);
            }

            try {
                assetVersion = versionService.save(type, assetVersion);
                saved = true;
            } catch (PersistenceException e) {
                logger.warn("Error during saving version", e);
            }
        }
        if (!saved) {
            log.error("Error during saving version");
            throw new AtlasException("Error during saving version");
        }
        return assetVersion;
    }

    public T update(VersionType type, VersionUpdateDTO updateDTO) {
        T currentVersion = getRepository(type).findOne(updateDTO.getVersionPk());
        if (Objects.isNull(currentVersion)) {
            throw new NotFoundException("Version not found");
        }

        currentVersion.setComment(updateDTO.getComment());
        currentVersion.setArchived(updateDTO.isArchived());
        return save(type, currentVersion);
    }

    public void delete(VersionType type, long assetId, int version) {
        VersionPK pk = new VersionPK(assetId, version);
        T currentVersion = getRepository(type).getOne(pk);
        if (Objects.isNull(currentVersion)) {
            throw new NotFoundException("Version not found");
        }
        currentVersion.setArchived(true);
    }

    public T getById(VersionType type, long assetId, int version) {
        VersionPK pk = new VersionPK(assetId, version);
        return getRepository(type).findOne(pk);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public T save(VersionType type, T version) {
        version = getRepository(type).saveAndFlush(version);
        entityManager.refresh(version);
        return getRepository(type).getOne(version.getPk());
    }
}
