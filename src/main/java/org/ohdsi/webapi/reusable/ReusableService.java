package org.ohdsi.webapi.reusable;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.dto.ReusableVersionFullDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.versioning.domain.ReusableVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReusableService extends AbstractDaoService implements HasTags<Integer> {
    private final ReusableRepository reusableRepository;
    private final EntityManager entityManager;
    private final ConversionService conversionService;
    private final PermissionService permissionService;
    private final VersionService<ReusableVersion> versionService;

    @Autowired
    public ReusableService(
            ReusableRepository reusableRepository,
            EntityManager entityManager,
            ConversionService conversionService,
            PermissionService permissionService,
            VersionService<ReusableVersion> versionService) {
        this.reusableRepository = reusableRepository;
        this.entityManager = entityManager;
        this.conversionService = conversionService;
        this.permissionService = permissionService;
        this.versionService = versionService;
    }

    public ReusableDTO create(ReusableDTO dto) {
        Reusable reusable = conversionService.convert(dto, Reusable.class);
        Reusable saved = create(reusable);
        return conversionService.convert(saved, ReusableDTO.class);
    }

    public Reusable create(Reusable reusable) {
        reusable.setCreatedBy(getCurrentUser());
        reusable.setCreatedDate(new Date());
        reusable.setModifiedBy(null);
        reusable.setModifiedDate(null);

        return save(reusable);
    }

    public Reusable getById(Integer id) {
        return reusableRepository.findOne(id);
    }

    public ReusableDTO getDTOById(Integer id) {
        Reusable reusable = reusableRepository.findOne(id);
        return conversionService.convert(reusable, ReusableDTO.class);
    }

    public List<Reusable> list() {
        return reusableRepository.findAll();
    }

    public Page<ReusableDTO> page(final Pageable pageable) {
        return reusableRepository.findAll(pageable)
                .map(reusable -> {
                    final ReusableDTO dto = conversionService.convert(reusable, ReusableDTO.class);
                    permissionService.fillWriteAccess(reusable, dto);
                    return dto;
                });
    }

    public ReusableDTO update(Integer id, ReusableDTO entity) {
        Date currentTime = Calendar.getInstance().getTime();

        saveVersion(id);

        Reusable existing = reusableRepository.findOne(id);
        UserEntity modifier = userRepository.findByLogin(security.getSubject());

        existing.setName(entity.getName())
                .setDescription(entity.getDescription())
                .setData(entity.getData());
        existing.setModifiedBy(modifier);
        existing.setModifiedDate(currentTime);

        Reusable saved = save(existing);
        return conversionService.convert(saved, ReusableDTO.class);
    }

    public ReusableDTO copy(Integer id) {
        ReusableDTO def = getDTOById(id);
        def.setId(null);
        def.setTags(null);
        def.setName(NameUtils.getNameForCopy(def.getName(), this::getNamesLike, reusableRepository.findByName(def.getName())));

        return create(def);
    }

    public void assignTag(Integer id, int tagId) {
        Reusable entity = getById(id);
        assignTag(entity, tagId);
    }

    public void unassignTag(Integer id, int tagId) {
        Reusable entity = getById(id);
        unassignTag(entity, tagId);
    }

    public void delete(Integer id) {
        Reusable existing = reusableRepository.findOne(id);

        checkOwnerOrAdminOrModerator(existing.getCreatedBy());

        reusableRepository.delete(id);
    }

    public List<VersionDTO> getVersions(long id) {
        List<VersionBase> versions = versionService.getVersions(VersionType.REUSABLE, id);
        return versions.stream()
                .map(v -> conversionService.convert(v, VersionDTO.class))
                .collect(Collectors.toList());
    }

    public ReusableVersionFullDTO getVersion(int id, int version) {
        checkVersion(id, version, false);
        ReusableVersion reusableVersion = versionService.getById(VersionType.REUSABLE, id, version);

        return conversionService.convert(reusableVersion, ReusableVersionFullDTO.class);
    }

    public VersionDTO updateVersion(int id, int version, VersionUpdateDTO updateDTO) {
        checkVersion(id, version);
        updateDTO.setAssetId(id);
        updateDTO.setVersion(version);
        ReusableVersion updated = versionService.update(VersionType.REUSABLE, updateDTO);

        return conversionService.convert(updated, VersionDTO.class);
    }

    public void deleteVersion(int id, int version) {
        checkVersion(id, version);
        versionService.delete(VersionType.REUSABLE, id, version);
    }

    public ReusableDTO copyAssetFromVersion(int id, int version) {
        checkVersion(id, version, false);
        ReusableVersion reusableVersion = versionService.getById(VersionType.REUSABLE, id, version);
        ReusableVersionFullDTO fullDTO = conversionService.convert(reusableVersion, ReusableVersionFullDTO.class);
        ReusableDTO dto = conversionService.convert(fullDTO.getEntityDTO(), ReusableDTO.class);
        dto.setId(null);
        dto.setTags(null);
        dto.setName(NameUtils.getNameForCopy(dto.getName(), this::getNamesLike,
                reusableRepository.findByName(dto.getName())));
        return create(dto);
    }

    public List<ReusableDTO> listByTags(TagNameListRequestDTO requestDTO) {
        List<String> names = requestDTO.getNames().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<Reusable> entities = reusableRepository.findByTags(names);
        return listByTags(entities, names, ReusableDTO.class);
    }

    private void checkVersion(int id, int version) {
        checkVersion(id, version, true);
    }

    private void checkVersion(int id, int version, boolean checkOwnerShip) {
        Version reusableVersion = versionService.getById(VersionType.REUSABLE, id, version);
        ExceptionUtils.throwNotFoundExceptionIfNull(reusableVersion,
                String.format("There is no reusable version with id = %d.", version));

        Reusable entity = this.reusableRepository.findOne(id);
        if (checkOwnerShip) {
            checkOwnerOrAdminOrGranted(entity);
        }
    }

    public ReusableVersion saveVersion(int id) {
        Reusable def = this.reusableRepository.findOne(id);
        ReusableVersion version = conversionService.convert(def, ReusableVersion.class);

        UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
        Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
        version.setCreatedBy(user);
        version.setCreatedDate(versionDate);
        return versionService.create(VersionType.REUSABLE, version);
    }

    private Reusable save(Reusable reusable) {
        reusable = reusableRepository.saveAndFlush(reusable);
        entityManager.refresh(reusable);
        return reusableRepository.findOne(reusable.getId());
    }

    public boolean exists(final int id, final String name) {
        return reusableRepository.existsCount(id, name) > 0;
    }

    public List<String> getNamesLike(String copyName) {
        return reusableRepository.findAllByNameStartsWith(copyName).stream().map(Reusable::getName).collect(Collectors.toList());
    }
}
