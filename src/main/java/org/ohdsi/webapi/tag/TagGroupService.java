package org.ohdsi.webapi.tag;

import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.reusable.ReusableService;
import org.ohdsi.webapi.service.*;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagGroupSubscriptionDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.ForbiddenException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TagGroupService extends AbstractDaoService {
    private final TagRepository tagRepository;
    private final PathwayService pathwayService;
    private final CcService ccService;
    private final CohortDefinitionService cohortDefinitionService;
    private final ConceptSetService conceptSetService;
    private final IRAnalysisResource irAnalysisService;
    private final ReusableService reusableService;

    @Autowired
    public TagGroupService(
            TagRepository tagRepository,
            PathwayService pathwayService,
            CcService ccService,
            CohortDefinitionService cohortDefinitionService,
            ConceptSetService conceptSetService,
            IRAnalysisResource irAnalysisService,
            ReusableService reusableService) {
        this.tagRepository = tagRepository;
        this.pathwayService = pathwayService;
        this.ccService = ccService;
        this.cohortDefinitionService = cohortDefinitionService;
        this.conceptSetService = conceptSetService;
        this.irAnalysisService = irAnalysisService;
        this.reusableService = reusableService;
    }

    public void assignGroup(TagGroupSubscriptionDTO dto) {
        tagRepository.findByIdIn(new ArrayList<>(dto.getTags()))
                .forEach(tag -> {
                    assignGroup(ccService, dto.getAssets().getCharacterizations(), tag.getId());
                    assignGroup(pathwayService, dto.getAssets().getPathways(), tag.getId());
                    assignGroup(cohortDefinitionService, dto.getAssets().getCohorts(), tag.getId());
                    assignGroup(conceptSetService, dto.getAssets().getConceptSets(), tag.getId());
                    assignGroup(irAnalysisService, dto.getAssets().getIncidenceRates(), tag.getId());
                    assignGroup(reusableService, dto.getAssets().getReusables(), tag.getId());
                });
    }

    public void unassignGroup(TagGroupSubscriptionDTO dto) {
        tagRepository.findByIdIn(new ArrayList<>(dto.getTags()))
                .forEach(tag -> {
                    unassignGroup(ccService, dto.getAssets().getCharacterizations(), tag.getId());
                    unassignGroup(pathwayService, dto.getAssets().getPathways(), tag.getId());
                    unassignGroup(cohortDefinitionService, dto.getAssets().getCohorts(), tag.getId());
                    unassignGroup(conceptSetService, dto.getAssets().getConceptSets(), tag.getId());
                    unassignGroup(irAnalysisService, dto.getAssets().getIncidenceRates(), tag.getId());
                    unassignGroup(reusableService, dto.getAssets().getReusables(), tag.getId());
                });

    }

    private <T extends Number> void assignGroup(HasTags<T> service, List<T> assetIds, Integer tagId) {
        assetIds.forEach(id -> {
            try {
                service.assignTag(id, tagId);
            } catch (final ForbiddenException e) {
                log.warn("Tag {} cannot be assigned to entity {} in service {} - forbidden", tagId, id, service.getClass().getName());
            }
        });
    }

    private <T extends Number> void unassignGroup(HasTags<T> service, List<T> assetIds, Integer tagId) {
        assetIds.forEach(id -> {
            try {
                service.unassignTag(id, tagId);
            } catch(final ForbiddenException e) {
                log.warn("Tag {} cannot be unassigned from entity {} in service {} - forbidden", tagId, id, service.getClass().getName());
            }
        });
    }
}
