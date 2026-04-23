package org.ohdsi.webapi.tag;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagInfo;
import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.dto.AssignmentPermissionsDTO;
import org.ohdsi.webapi.tag.dto.TagGroupSubscriptionDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tag")
@Transactional
public class TagService extends AbstractDaoService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);
    private final TagRepository tagRepository;
    private final EntityManager entityManager;
    private final ConversionService conversionService;
    private final TagGroupService tagGroupService;

    private final ArrayList<Supplier<List<TagInfo>>> infoProducers;

    @Autowired
    public TagService(
            TagRepository tagRepository,
            EntityManager entityManager,
            @Qualifier("conversionService") ConversionService conversionService,
            @Lazy TagGroupService tagGroupService) {
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
        this.conversionService = conversionService;
        this.tagGroupService = tagGroupService;

        this.infoProducers = new ArrayList<>();
        this.infoProducers.add(tagRepository::findCohortTagInfo);
        this.infoProducers.add(tagRepository::findConceptSetTagInfo);
        this.infoProducers.add(tagRepository::findReusableTagInfo);
    }

    /**
     * Creates a tag.
     *
     * @param dto
     * @return
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public TagDTO create(@RequestBody TagDTO dto) {
        Tag tag = conversionService.convert(dto, Tag.class);
        Tag saved = create(tag);
        return conversionService.convert(saved, TagDTO.class);
    }

    public Tag create(Tag tag) {
        tag.setType(TagType.CUSTOM);
        List<Integer> groupIds = tag.getGroups().stream()
                .map(Tag::getId)
                .collect(Collectors.toList());
        List<Tag> groups = findByIdIn(groupIds);
        boolean allowCustom = groups.stream()
                .filter(Tag::isAllowCustom)
                .count() == groups.size();

        if (allowCustom) {
            throw new IllegalArgumentException("Tag can be added only to groups that allows to do it");
        }

        tag.setGroups(new HashSet<>(groups));
        tag.setCreatedBy(getCurrentUser());
        tag.setCreatedDate(new Date());

        return save(tag);
    }

    public Tag getById(Integer id) {
        return tagRepository.findById(id).orElse(null);
    }

    /**
     * Return tag by ID.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TagDTO getDTOById(@PathVariable("id") Integer id) {
        Tag tag = tagRepository.findById(id).orElse(null);
        return conversionService.convert(tag, TagDTO.class);
    }

    /**
     * Returns list of tags, which names contain a provided substring.
     *
     * @summary Search tags by name part
     * @param namePart
     * @return
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TagDTO> listInfoDTO(@RequestParam("namePart") String namePart) {
        if (StringUtils.isBlank(namePart)) {
            return Collections.emptyList();
        }
        return listInfo(namePart).stream()
                .map(tag -> conversionService.convert(tag, TagDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of all tags.
     *
     * @return
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TagDTO> listInfoDTO() {
        return listInfo().stream()
                .map(tag -> conversionService.convert(tag, TagDTO.class))
                .collect(Collectors.toList());
    }

    public List<Tag> listInfo(String namePart) {
        return tagRepository.findAllTags(namePart);
    }

    public List<Tag> listInfo() {
        return tagRepository.findAll();
    }

    public List<Tag> findByIdIn(List<Integer> ids) {
        return tagRepository.findByIdIn(ids);
    }

    /**
     * Updates tag with ID={id}.
     *
     * @param id
     * @param entity
     * @return
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public TagDTO update(@PathVariable("id") Integer id, @RequestBody TagDTO entity) {
        Tag existing = tagRepository.findById(id).orElse(null);

        Tag toUpdate = this.conversionService.convert(entity, Tag.class);

        List<Integer> groupIds = toUpdate.getGroups().stream()
                .map(Tag::getId)
                .collect(Collectors.toList());
        List<Tag> groups = findByIdIn(groupIds);
        toUpdate.setGroups(new HashSet<>(groups));

        toUpdate.setCreatedBy(existing.getCreatedBy());
        toUpdate.setCreatedDate(existing.getCreatedDate());
        toUpdate.setModifiedBy(getCurrentUser());
        toUpdate.setModifiedDate(new Date());

        Tag saved = save(toUpdate);
        return conversionService.convert(saved, TagDTO.class);
    }

    /**
     * Deletes tag with ID={id}.
     *
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable("id") Integer id) {
        Tag existing = tagRepository.findById(id).orElseThrow();
        tagRepository.deleteById(existing.getId());
    }

    private Tag save(Tag tag) {
        tag = tagRepository.saveAndFlush(tag);
        entityManager.refresh(tag);
        return tagRepository.findById(tag.getId()).orElse(null);
    }

    @Transactional
    @Scheduled(fixedDelayString = "${tag.refreshStat.period}")
    public void refreshTagStatistics() {
        logger.info("Starting tags statistics refreshing");
        try {
            // Getting tag statistics in one query with multiple join clauses
            // will take significant amount of time
            // So we'll get this information for each asset in series
            Map<Integer, TagDTO> infoMap = new HashMap<>();
            this.infoProducers.forEach(producer -> processTagInfo(producer, infoMap));

            List<Tag> tags = tagRepository.findAll();
            tags = tags.stream()
                    .peek(tag -> {
                        TagDTO info = infoMap.get(tag.getId());
                        if (Objects.nonNull(info)) {
                            tag.setCount(info.getCount());
                        }
                    })
                    .collect(Collectors.toList());
            tagRepository.saveAll(tags);
        } catch (Exception e) {
            logger.error("Cannot refresh tags statistics");
        }
        logger.info("Finishing tags statistics refreshing");
    }

    private void processTagInfo(Supplier<List<TagInfo>> infoProducer,
                                Map<Integer, TagDTO> infoMap) {
        List<TagInfo> tagInfos = infoProducer.get();
        tagInfos.forEach(info -> {
            int id = info.getId();
            TagDTO dto = infoMap.get(id);
            if (Objects.isNull(dto)) {
                infoMap.put(id, new TagDTO());
                dto = infoMap.get(id);
            }
            int count = dto.getCount() + info.getCount();
            dto.setCount(count);
        });
    }

    public List<Tag> findMandatoryTags() {
        return tagRepository.findMandatoryTags();
    }

    public Set<Integer> getAllGroupsForTag(Integer id) {
        Tag tag = getById(id);
        Set<Integer> groupIds = new HashSet<>();
        if (Objects.nonNull(tag)) {
            groupIds.add(tag.getId());
            findParentGroup(tag.getGroups(), groupIds);
        }
        return groupIds;
    }

    private void findParentGroup(Set<Tag> groups, Set<Integer> groupIds) {
        groups.forEach(g -> {
            groupIds.add(g.getId());
            findParentGroup(g.getGroups(), groupIds);
        });
    }

    /**
     * Tags assignment permissions for current user
     *
     * @return
     */
    @GetMapping(value = "/assignmentPermissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public AssignmentPermissionsDTO getAssignmentPermissions() {
        AuthorizationService authSvc = this.getAuthorizationService();
        final AssignmentPermissionsDTO tagPermission = new AssignmentPermissionsDTO();
        tagPermission.setAnyAssetMultiAssignPermitted(authSvc.isPermitted("admin:tags"));
        tagPermission.setCanAssignProtectedTags(authSvc.isPermitted("admin:tags"));
        tagPermission.setCanUnassignProtectedTags(authSvc.isPermitted("admin:tags"));
        return tagPermission;
    }

    /**
     * Assigns group of tags to groups of assets.
     *
     * @param dto
     */
    @PostMapping(value = "/multiAssign", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void assignGroup(@RequestBody TagGroupSubscriptionDTO dto) {
        tagGroupService.assignGroup(dto);
    }

    /**
     * Unassigns group of tags from groups of assets.
     *
     * @param dto
     */
    @PostMapping(value = "/multiUnassign", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void unassignGroup(@RequestBody TagGroupSubscriptionDTO dto) {
        tagGroupService.unassignGroup(dto);
    }
}
