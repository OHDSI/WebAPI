package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.domain.PathwayCohortType;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.pathway.repository.PathwayEventCohortRepository;
import org.ohdsi.webapi.pathway.repository.PathwayTargetCohortRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PathwayServiceImpl extends AbstractDaoService implements PathwayService {

    private PathwayAnalysisEntityRepository pathwayAnalysisRepository;
    private PathwayTargetCohortRepository pathwayTargetCohortRepository;
    private PathwayEventCohortRepository pathwayEventCohortRepository;

    @Autowired
    public PathwayServiceImpl(
            PathwayAnalysisEntityRepository pathwayAnalysisRepository,
            PathwayTargetCohortRepository pathwayTargetCohortRepository,
            PathwayEventCohortRepository pathwayEventCohortRepository
    ) {

        this.pathwayAnalysisRepository = pathwayAnalysisRepository;
        this.pathwayTargetCohortRepository = pathwayTargetCohortRepository;
        this.pathwayEventCohortRepository = pathwayEventCohortRepository;
    }

    @Override
    public PathwayAnalysisEntity create(PathwayAnalysisEntity analysis) {

        analysis.setId(null);

        List<PathwayTargetCohort> targetCohortList = analysis.getTargetCohorts();
        analysis.setTargetCohorts(null);

        List<PathwayEventCohort> eventCohortList = analysis.getEventCohorts();
        analysis.setEventCohorts(null);

        analysis.setCreatedBy(getCurrentUser());
        analysis.setCreatedAt(new Date());

        PathwayAnalysisEntity savedAnalysis = pathwayAnalysisRepository.saveAndFlush(analysis);

        List<PathwayTargetCohort> savedTargetCohortList = targetCohortList
                .stream()
                .map(tc -> {
                    tc.setPathwayAnalysis(savedAnalysis);
                    return pathwayTargetCohortRepository.saveAndFlush(tc);
                })
                .collect(Collectors.toList());
        savedAnalysis.setTargetCohorts(savedTargetCohortList);

        List<PathwayEventCohort> savedEventCohortList = eventCohortList
                .stream()
                .map(ec -> {
                    ec.setPathwayAnalysis(savedAnalysis);
                    return pathwayEventCohortRepository.saveAndFlush(ec);
                })
                .collect(Collectors.toList());
        analysis.setEventCohorts(savedEventCohortList);

        return analysis;
    }

    @Override
    public Page<PathwayAnalysisEntity> getPage(final Pageable pageable) {
        return pathwayAnalysisRepository.findAll(pageable).map(this::gatherLinkedEntities);
    }

    @Override
    public PathwayAnalysisEntity getById(Long id) {
        return gatherLinkedEntities(pathwayAnalysisRepository.findOne(id));
    }

    @Override
    public PathwayAnalysisEntity update(PathwayAnalysisEntity pathwayAnalysis) {

        PathwayAnalysisEntity existing = pathwayAnalysisRepository.findOne(pathwayAnalysis.getId());

        pathwayAnalysis.setCreatedBy(existing.getCreatedBy());
        pathwayAnalysis.setCreatedAt(existing.getCreatedAt());

        pathwayAnalysis.setUpdatedBy(getCurrentUser());
        pathwayAnalysis.setUpdatedAt(new Date());

        pathwayAnalysis.getTargetCohorts().forEach(tc -> tc.setPathwayAnalysis(pathwayAnalysis));
        pathwayAnalysis.getEventCohorts().forEach(ec -> ec.setPathwayAnalysis(pathwayAnalysis));

        return gatherLinkedEntities(pathwayAnalysisRepository.saveAndFlush(pathwayAnalysis));
    }

    @Override
    public void delete(Long id) {

        pathwayAnalysisRepository.delete(id);
    }

    private PathwayAnalysisEntity gatherLinkedEntities(PathwayAnalysisEntity pathwayAnalysis) {

        pathwayAnalysis.setTargetCohorts(
                pathwayTargetCohortRepository.findAllByPathwayAnalysisId(
                        pathwayAnalysis.getId(),
                        EntityUtils.fromAttributePaths("cohortDefinition")
                )
        );

        pathwayAnalysis.setEventCohorts(
                pathwayEventCohortRepository.findAllByPathwayAnalysisId(
                    pathwayAnalysis.getId(),
                    EntityUtils.fromAttributePaths("cohortDefinition")
            )
        );

        return pathwayAnalysis;
    }
}
