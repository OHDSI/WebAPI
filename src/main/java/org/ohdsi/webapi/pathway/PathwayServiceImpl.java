package org.ohdsi.webapi.pathway;

import com.odysseusinc.arachne.commons.types.DBMSType;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.pathway.repository.PathwayEventCohortRepository;
import org.ohdsi.webapi.pathway.repository.PathwayTargetCohortRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PathwayServiceImpl extends AbstractDaoService implements PathwayService {

    private final PathwayAnalysisEntityRepository pathwayAnalysisRepository;
    private final PathwayTargetCohortRepository pathwayTargetCohortRepository;
    private final PathwayEventCohortRepository pathwayEventCohortRepository;
    private final EntityManager entityManager;
    private final SourceService sourceService;

    @Autowired
    public PathwayServiceImpl(
            PathwayAnalysisEntityRepository pathwayAnalysisRepository,
            PathwayTargetCohortRepository pathwayTargetCohortRepository,
            PathwayEventCohortRepository pathwayEventCohortRepository,
            EntityManager entityManager,
            SourceService sourceService
    ) {

        this.pathwayAnalysisRepository = pathwayAnalysisRepository;
        this.pathwayTargetCohortRepository = pathwayTargetCohortRepository;
        this.pathwayEventCohortRepository = pathwayEventCohortRepository;
        this.entityManager = entityManager;
        this.sourceService = sourceService;
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
    public PathwayAnalysisEntity getById(Integer id) {
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
    public void delete(Integer id) {

        pathwayAnalysisRepository.delete(id);
    }

    @Override
    public Map<Integer, Integer> getEventCohortCodes(Integer pathwayAnalysisId) {
        Map<Integer, Integer> map = new HashMap<>();
        Query q = entityManager.createNativeQuery(
                "SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS event_index " +
                "FROM pathway_event_cohorts " +
                "WHERE pathway_analysis_id = :pathway_analysis_id"
        );
        q.setParameter("pathway_analysis_id", pathwayAnalysisId);

        List<Object[]> list = q.getResultList();
        for (Object[] result : list) {
            map.put(Integer.parseInt(result[0].toString()), Integer.parseInt(result[1].toString()));
        }
        return map;
    }

    @Override
    public String buildAnalysisSql(Integer pathwayAnalysisId, String sourceKey) {

        PathwayAnalysisEntity pathwayAnalysis = getById(pathwayAnalysisId);
        Map<Integer, Integer> eventCohortCodes = getEventCohortCodes(pathwayAnalysisId);
        Source source = sourceService.findBySourceKey(sourceKey);

        String analysisSql = ResourceHelper.GetResourceAsString("/resources/pathway/runPathwayAnalysis.sql");
        String eventCohortInputSql = ResourceHelper.GetResourceAsString("/resources/pathway/eventCohortInput.sql");

        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String eventCohortIdIndexSql = pathwayAnalysis.getEventCohorts()
                .stream()
                .map(ec -> {
                    String[] params = new String[]{"event_cohort_id", "event_cohort_index"};
                    String[] values = new String[]{ec.getCohortDefinition().getId().toString(), eventCohortCodes.get(ec.getId()).toString()};
                    return SqlRender.renderSql(eventCohortInputSql, params, values);
                })
                .collect(Collectors.joining(" UNION ALL "));

        String[] params = new String[]{
                "event_cohort_id_index_map",
                "target_database_schema",
                "target_cohort_table",
                "pathway_target_cohort_id_list"
        };
        String[] values = new String[]{
                eventCohortIdIndexSql,
                resultsTableQualifier,
                "cohort",
                pathwayAnalysis.getTargetCohorts().stream().map(tc -> tc.getCohortDefinition().getId().toString()).collect(Collectors.joining(", "))
        };

        String renderedSql = SqlRender.renderSql(analysisSql, params, values);
        String translatedSql = SqlTranslate.translateSql(renderedSql, DBMSType.POSTGRESQL.getOhdsiDB());

        return translatedSql;
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
