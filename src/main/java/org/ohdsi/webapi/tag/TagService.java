package org.ohdsi.webapi.tag;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagAssetType;
import org.ohdsi.webapi.tag.domain.TagInfo;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.dto.TagInfoDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService extends AbstractDaoService {
    private final TagRepository tagRepository;
    private final EntityManager entityManager;
    private final ConversionService conversionService;

    @Autowired
    public TagService(
            TagRepository tagRepository,
            EntityManager entityManager,
            ConversionService conversionService) {
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
        this.conversionService = conversionService;
    }

    public TagDTO createFromDTO(TagDTO dto) {
        Tag tag = conversionService.convert(dto, Tag.class);
        Tag saved = create(tag);
        return conversionService.convert(saved, TagDTO.class);
    }

    public Tag create(Tag tag) {
        tag.setCreatedBy(getCurrentUser());
        tag.setCreatedDate(new Date());

        return save(tag);
    }

    public TagDTO getDTOById(Integer id) {
        Tag tag = tagRepository.findOne(id);
        return conversionService.convert(tag, TagDTO.class);
    }

    public Tag getById(Integer id) {
        return tagRepository.findOne(id);
    }

    public List<TagInfoDTO> listInfoDTO(TagAssetType assetType, String namePart) {
        return listInfo(assetType, namePart).stream()
                .map(tag -> conversionService.convert(tag, TagInfoDTO.class))
                .collect(Collectors.toList());
    }

    public List<TagInfo> listInfo(TagAssetType assetType, String namePart) {
        List<TagInfo> tagInfos = new ArrayList<>();
        switch (assetType) {
            case CONCEPT_SET:
                break;
            case COHORT: {
                tagInfos = tagRepository.findAllCohortTagsByNameInterface(namePart);
                break;
            }
            case COHORT_CHARACTERIZATION:
                break;
            case INCIDENT_RATE:
                break;
            case PATHWAY:
                break;
            default: {
                throw new IllegalArgumentException("unknown asset type");
            }
        }
        tagInfos.forEach(t -> System.out.println(t.getTag().getName() + "--" + t.getTagCount()));
        return tagInfos;
    }

//	@Override
//	public PathwayAnalysisEntity update(PathwayAnalysisEntity forUpdate) {
//
//		PathwayAnalysisEntity existing = getById(forUpdate.getId());
//
//		copyProps(forUpdate, existing);
//		updateCohorts(existing, existing.getTargetCohorts(), forUpdate.getTargetCohorts());
//		updateCohorts(existing, existing.getEventCohorts(), forUpdate.getEventCohorts());
//
//		existing.setModifiedBy(getCurrentUser());
//		existing.setModifiedDate(new Date());
//
//		return save(existing);
//	}

    public void delete(Integer id) {
        tagRepository.delete(id);
    }

    private Tag save(Tag tag) {
        tag = tagRepository.saveAndFlush(tag);
        entityManager.refresh(tag);
        return tagRepository.findOne(tag.getId());
    }
}
