package org.ohdsi.webapi.pathway;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.pathway.dto.internal.PersonPathwayEvent;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.pathway.repository.PathwayEventCohortRepository;
import org.ohdsi.webapi.pathway.repository.PathwayTargetCohortRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summingInt;

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
    public String buildAnalysisSql(Integer generationId, Integer pathwayAnalysisId, String sourceKey) {

        PathwayAnalysisEntity pathwayAnalysis = getById(pathwayAnalysisId);
        Map<Integer, Integer> eventCohortCodes = getEventCohortCodes(pathwayAnalysisId);
        Source source = sourceService.findBySourceKey(sourceKey);

        String analysisSql = ResourceHelper.GetResourceAsString("/resources/pathway/runPathwayAnalysis.sql");
        String eventCohortInputSql = ResourceHelper.GetResourceAsString("/resources/pathway/eventCohortInput.sql");

        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String eventCohortIdIndexSql = pathwayAnalysis.getEventCohorts()
                .stream()
                .map(ec -> {
                    String[] params = new String[]{"cohort_definition_id", "event_cohort_index"};
                    String[] values = new String[]{ec.getCohortDefinition().getId().toString(), eventCohortCodes.get(ec.getId()).toString()};
                    return SqlRender.renderSql(eventCohortInputSql, params, values);
                })
                .collect(Collectors.joining(" UNION ALL "));

        String[] params = new String[]{
                "generation_id",
                "event_cohort_id_index_map",
                "target_database_schema",
                "target_cohort_table",
                "pathway_target_cohort_id_list"
        };
        String[] values = new String[]{
               generationId.toString() ,
                eventCohortIdIndexSql,
                resultsTableQualifier,
                "cohort",
                pathwayAnalysis.getTargetCohorts().stream().map(tc -> tc.getCohortDefinition().getId().toString()).collect(Collectors.joining(", "))
        };

        String renderedSql = SqlRender.renderSql(analysisSql, params, values);
        String translatedSql = SqlTranslate.translateSql(renderedSql, source.getSourceDialect());

        return translatedSql;
    }

    @Override
    public PathwayAnalysisResult getResultingPathways(final Integer id) {

        PathwayAnalysisResult result = new PathwayAnalysisResult();
        result.setCodes(new HashSet<>());

        // TODO:
        String sourceKey = "BCBS_Synpuf";
        Source source = sourceService.findBySourceKey(sourceKey);

        PreparedStatementRenderer pathwayEventsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/getPersonLevelPathwayResults.sql",
                new String[] {"target_database_schema"},
                new String[] {source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                "pathway_analysis_generation_id",
                id
        );

        List<PersonPathwayEvent> events = getSourceJdbcTemplate(source).query(pathwayEventsPsr.getSql(), pathwayEventsPsr.getSetter(), (rs, rowNum) -> {
            PersonPathwayEvent event = new PersonPathwayEvent();
            event.setComboId(rs.getInt("combo_id"));
            event.setSubjectId(rs.getInt("subject_id"));
            event.setStartDate(rs.getDate("cohort_start_date"));
            event.setEndDate(rs.getDate("cohort_end_date"));

            result.getCodes().add(event.getComboId());

            return event;
        });

        Map<String, Integer> chains = events.stream()
                .collect(Collectors.groupingBy(PersonPathwayEvent::getSubjectId))
                .entrySet()
                .stream()
                .map(entry -> {
                    entry.getValue().sort(Comparator.comparing(PersonPathwayEvent::getStartDate));
                    return entry.getValue().stream().map(e -> e.getComboId().toString()).collect(Collectors.joining("-"));
                })
                .collect(Collectors.groupingBy(Function.identity(), summingInt(x -> 1)));

        result.setPathwaysCounts(chains);

        return result;
    }

    @Override
    public List<PathwayEventCohort> getEventCohortsByComboCode(PathwayAnalysisEntity pathwayAnalysis, Map<Integer, Integer> eventCodes, Integer comboCode) {

        return pathwayAnalysis.getEventCohorts()
                .stream()
                .filter(ec -> ((int) Math.pow(2, Double.valueOf(eventCodes.get(ec.getId()))) & comboCode) > 0)
                .collect(Collectors.toList());
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
