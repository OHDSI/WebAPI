package org.ohdsi.webapi.tag;

import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.service.*;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagGroupSubscriptionDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    public TagGroupService(
            TagRepository tagRepository,
            PathwayService pathwayService,
            CcService ccService,
            CohortDefinitionService cohortDefinitionService,
            ConceptSetService conceptSetService,
            IRAnalysisResource irAnalysisService) {
        this.tagRepository = tagRepository;
        this.pathwayService = pathwayService;
        this.ccService = ccService;
        this.cohortDefinitionService = cohortDefinitionService;
        this.conceptSetService = conceptSetService;
        this.irAnalysisService = irAnalysisService;
    }

    public void assignGroup(TagGroupSubscriptionDTO dto) {
        if (isAdmin()) {
            tagRepository.findByIdIn(new ArrayList<>(dto.getTags()))
                    .forEach(tag -> {
                        assignGroup(ccService, dto.getAssets().getCharacterizations(), tag.getId());
                        assignGroup(pathwayService, dto.getAssets().getPathways(), tag.getId());
                        assignGroup(cohortDefinitionService, dto.getAssets().getCohorts(), tag.getId());
                        assignGroup(conceptSetService, dto.getAssets().getConceptSets(), tag.getId());
                        assignGroup(irAnalysisService, dto.getAssets().getIncidenceRates(), tag.getId());
                    });
        }
    }

    public void unassignGroup(TagGroupSubscriptionDTO dto) {
        if (isAdmin()) {
            tagRepository.findByIdIn(new ArrayList<>(dto.getTags()))
                    .forEach(tag -> {
                        unassignGroup(ccService, dto.getAssets().getCharacterizations(), tag.getId());
                        unassignGroup(pathwayService, dto.getAssets().getPathways(), tag.getId());
                        unassignGroup(cohortDefinitionService, dto.getAssets().getCohorts(), tag.getId());
                        unassignGroup(conceptSetService, dto.getAssets().getConceptSets(), tag.getId());
                        unassignGroup(irAnalysisService, dto.getAssets().getIncidenceRates(), tag.getId());
                    });
        }
    }

    private <T extends Number> void assignGroup(HasTags<T> service, List<T> assetIds, Integer tagId) {
        assetIds.forEach(id -> {
            service.assignTag(id, tagId, true);
        });
    }

    private <T extends Number> void unassignGroup(HasTags<T> service, List<T> assetIds, Integer tagId) {
        assetIds.forEach(id -> {
            service.unassignTag(id, tagId, true);
        });
    }
}
