package org.ohdsi.webapi.tag;

import org.apache.shiro.SecurityUtils;
import org.glassfish.jersey.internal.util.Producer;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagInfo;
import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.dto.AssignmentPermissionsDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService extends AbstractDaoService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);
    private final TagRepository tagRepository;
    private final EntityManager entityManager;
    private final ConversionService conversionService;

    private final ArrayList<Producer<List<TagInfo>>> infoProducers;

    @Autowired
    public TagService(
            TagRepository tagRepository,
            EntityManager entityManager,
            ConversionService conversionService) {
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
        this.conversionService = conversionService;

        this.infoProducers = new ArrayList<>();
        this.infoProducers.add(tagRepository::findCohortTagInfo);
        this.infoProducers.add(tagRepository::findCcTagInfo);
        this.infoProducers.add(tagRepository::findConceptSetTagInfo);
        this.infoProducers.add(tagRepository::findIrTagInfo);
        this.infoProducers.add(tagRepository::findPathwayTagInfo);
        this.infoProducers.add(tagRepository::findReusableTagInfo);
    }

    public TagDTO create(TagDTO dto) {
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

        if (this.getPermissionService().isSecurityEnabled() && !SecurityUtils.getSubject().isPermitted("tag:management") && !allowCustom) {
            throw new IllegalArgumentException("Tag can be added only to groups that allows to do it");
        }

        tag.setGroups(new HashSet<>(groups));
        tag.setCreatedBy(getCurrentUser());
        tag.setCreatedDate(new Date());

        return save(tag);
    }

    public Tag getById(Integer id) {
        return tagRepository.findOne(id);
    }

    public TagDTO getDTOById(Integer id) {
        Tag tag = tagRepository.findOne(id);
        return conversionService.convert(tag, TagDTO.class);
    }

    public List<TagDTO> listInfoDTO(String namePart) {
        return listInfo(namePart).stream()
                .map(tag -> conversionService.convert(tag, TagDTO.class))
                .collect(Collectors.toList());
    }

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

    public TagDTO update(Integer id, TagDTO entity) {
        Tag existing = tagRepository.findOne(id);

        checkOwnerOrAdmin(existing.getCreatedBy());

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

    public void delete(Integer id) {
        Tag existing = tagRepository.findOne(id);

        checkOwnerOrAdmin(existing.getCreatedBy());

        tagRepository.delete(id);
    }

    private Tag save(Tag tag) {
        tag = tagRepository.saveAndFlush(tag);
        entityManager.refresh(tag);
        return tagRepository.findOne(tag.getId());
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
            tagRepository.save(tags);
        } catch (Exception e) {
            logger.error("Cannot refresh tags statistics");
        }
        logger.info("Finishing tags statistics refreshing");
    }

    private void processTagInfo(Producer<List<TagInfo>> infoProducer,
                                Map<Integer, TagDTO> infoMap) {
        List<TagInfo> tagInfos = infoProducer.call();
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

    public AssignmentPermissionsDTO getAssignmentPermissions() {
        final AssignmentPermissionsDTO tagPermission = new AssignmentPermissionsDTO();
        tagPermission.setAnyAssetMultiAssignPermitted(isAdmin());
        tagPermission.setCanAssignProtectedTags(!isSecured() || TagSecurityUtils.canAssingProtectedTags());
        tagPermission.setCanUnassignProtectedTags(!isSecured() || TagSecurityUtils.canUnassingProtectedTags());
        return tagPermission;
    }
}
