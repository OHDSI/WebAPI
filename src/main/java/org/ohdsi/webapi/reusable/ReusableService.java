package org.ohdsi.webapi.reusable;

import org.ohdsi.webapi.Pagination;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.dto.ReusableVersionFullDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.service.AbstractDaoService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

@RestController
@RequestMapping("/reusable")
@Transactional
public class ReusableService extends AbstractDaoService implements HasTags<Integer> {

    private final AuthorizationService authorizationService;
    private final ReusableRepository reusableRepository;
    private final EntityManager entityManager;
    private final ConversionService conversionService;
    private final VersionService<ReusableVersion> versionService;

    @Autowired
    public ReusableService(
            AuthorizationService authorizationService,
            ReusableRepository reusableRepository,
            EntityManager entityManager,
            @Qualifier("conversionService") ConversionService conversionService,
            VersionService<ReusableVersion> versionService) {
        this.authorizationService = authorizationService;
        this.reusableRepository = reusableRepository;
        this.entityManager = entityManager;
        this.conversionService = conversionService;
        this.versionService = versionService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ReusableDTO create(@RequestBody ReusableDTO dto) {
        return createInternal(dto);
    }

    private ReusableDTO createInternal(ReusableDTO dto) {
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
        return reusableRepository.findById(id).orElse(null);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReusableDTO getDTOById(@PathVariable("id") Integer id) {
        Reusable reusable = reusableRepository.findById(id).orElse(null);
        return conversionService.convert(reusable, ReusableDTO.class);
    }

    public List<Reusable> list() {
        return reusableRepository.findAll();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ReusableDTO> page(@Pagination Pageable pageable) {
        return reusableRepository.findAll(pageable)
                .map(reusable -> {
                    final ReusableDTO dto = conversionService.convert(reusable, ReusableDTO.class);
                    // permissionService.fillWriteAccess(reusable, dto);
                    return dto;
                });
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ReusableDTO update(@PathVariable("id") Integer id, @RequestBody ReusableDTO entity) {
        Date currentTime = Calendar.getInstance().getTime();

        saveVersion(id);

        Reusable existing = reusableRepository.findById(id).orElse(null);
        UserEntity modifier = userRepository.findById(authorizationService.getAuthenticatedPrincipal().getUserId()).orElseThrow();

        existing.setName(entity.getName())
                .setDescription(entity.getDescription())
                .setData(entity.getData());
        existing.setModifiedBy(modifier);
        existing.setModifiedDate(currentTime);

        Reusable saved = save(existing);
        return conversionService.convert(saved, ReusableDTO.class);
    }

    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ReusableDTO copy(@PathVariable("id") Integer id) {
        ReusableDTO def = getDTOById(id);
        def.setId(null);
        def.setTags(null);
        def.setName(NameUtils.getNameForCopy(def.getName(), this::getNamesLike, reusableRepository.findByName(def.getName())));

        return createInternal(def);
    }

    @PostMapping(value = "/{id}/tag", produces = MediaType.APPLICATION_JSON_VALUE)
    public void assignTag(@PathVariable("id") Integer id, @RequestBody int tagId) {
        Reusable entity = getById(id);
        assignTag(entity, tagId);
    }

    @DeleteMapping(value = "/{id}/tag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void unassignTag(@PathVariable("id") Integer id, @PathVariable("tagId") int tagId) {
        Reusable entity = getById(id);
        unassignTag(entity, tagId);
    }

    @PostMapping(value = "/{id}/protectedtag", produces = MediaType.APPLICATION_JSON_VALUE)
    public void assignPermissionProtectedTag(@PathVariable("id") int id, @RequestBody int tagId) {
        Reusable entity = getById(id);
        assignTag(entity, tagId);
    }

    @DeleteMapping(value = "/{id}/protectedtag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void unassignPermissionProtectedTag(@PathVariable("id") int id, @PathVariable("tagId") int tagId) {
        Reusable entity = getById(id);
        unassignTag(entity, tagId);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable("id") Integer id) {
        Reusable existing = reusableRepository.findById(id).orElse(null);
        reusableRepository.deleteById(id);
    }

    @GetMapping(value = "/{id}/version", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VersionDTO> getVersions(@PathVariable("id") long id) {
        List<VersionBase> versions = versionService.getVersions(VersionType.REUSABLE, id);
        return versions.stream()
                .map(v -> conversionService.convert(v, VersionDTO.class))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReusableVersionFullDTO getVersion(@PathVariable("id") int id, @PathVariable("version") int version) {
        checkVersion(id, version);
        ReusableVersion reusableVersion = versionService.getById(VersionType.REUSABLE, id, version);

        return conversionService.convert(reusableVersion, ReusableVersionFullDTO.class);
    }

    @PutMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public VersionDTO updateVersion(@PathVariable("id") int id, @PathVariable("version") int version, @RequestBody VersionUpdateDTO updateDTO) {
        checkVersion(id, version);
        updateDTO.setAssetId(id);
        updateDTO.setVersion(version);
        ReusableVersion updated = versionService.update(VersionType.REUSABLE, updateDTO);

        return conversionService.convert(updated, VersionDTO.class);
    }

    @DeleteMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteVersion(@PathVariable("id") int id, @PathVariable("version") int version) {
        checkVersion(id, version);
        versionService.delete(VersionType.REUSABLE, id, version);
    }

    @PutMapping(value = "/{id}/version/{version}/createAsset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReusableDTO copyAssetFromVersion(@PathVariable("id") int id, @PathVariable("version") int version) {
        checkVersion(id, version);
        ReusableVersion reusableVersion = versionService.getById(VersionType.REUSABLE, id, version);
        ReusableVersionFullDTO fullDTO = conversionService.convert(reusableVersion, ReusableVersionFullDTO.class);
        ReusableDTO dto = conversionService.convert(fullDTO.getEntityDTO(), ReusableDTO.class);
        dto.setId(null);
        dto.setTags(null);
        dto.setName(NameUtils.getNameForCopy(dto.getName(), this::getNamesLike,
                reusableRepository.findByName(dto.getName())));
        return createInternal(dto);
    }

    @PostMapping(value = "/byTags", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ReusableDTO> listByTags(@RequestBody TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = requestDTO.getNames().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<Reusable> entities = reusableRepository.findByTags(names);
        return listByTags(entities, names, ReusableDTO.class);
    }

    private void checkVersion(int id, int version) {
        Version reusableVersion = versionService.getById(VersionType.REUSABLE, id, version);
        ExceptionUtils.throwNotFoundExceptionIfNull(reusableVersion,
                String.format("There is no reusable version with id = %d.", version));
    }

    public ReusableVersion saveVersion(int id) {
        Reusable def = this.reusableRepository.findById(id).orElse(null);
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
        return reusableRepository.findById(reusable.getId()).orElse(null);
    }

    @GetMapping(value = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean exists(@PathVariable("id") int id, @RequestParam(value = "name", required = false) String name) {
        return reusableRepository.existsCount(id, name) > 0;
    }

    public List<String> getNamesLike(String copyName) {
        return reusableRepository.findAllByNameStartsWith(copyName).stream().map(Reusable::getName).collect(Collectors.toList());
    }
}
