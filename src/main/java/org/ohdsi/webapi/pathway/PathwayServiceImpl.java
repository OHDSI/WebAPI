package org.ohdsi.webapi.pathway;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGeneration;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.pathway.dto.internal.PersonPathwayEvent;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summingInt;
import static org.ohdsi.webapi.Constants.GENERATE_PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.DESIGN;
import static org.ohdsi.webapi.Constants.Params.HASH_CODE;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.PATHWAY_ANALYSIS_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.common.GenerationUtils.checkSourceAccess;

@Service
@Transactional
public class PathwayServiceImpl extends AbstractDaoService implements PathwayService {

    private final PathwayAnalysisEntityRepository pathwayAnalysisRepository;
    private final PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository;
    private final SourceService sourceService;
    private final JobTemplate jobTemplate;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilders;
    private final EntityManager entityManager;

    private final EntityGraph defaultEntityGraph = EntityUtils.fromAttributePaths(
            "targetCohorts.cohortDefinition",
            "eventCohorts.cohortDefinition",
            "createdBy",
            "modifiedBy"
    );

    @Autowired
    public PathwayServiceImpl(
            PathwayAnalysisEntityRepository pathwayAnalysisRepository,
            PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository,
            SourceService sourceService,
            ConversionService conversionService,
            JobTemplate jobTemplate,
            StepBuilderFactory stepBuilderFactory,
            JobBuilderFactory jobBuilders,
            EntityManager entityManager
    ) {

        this.pathwayAnalysisRepository = pathwayAnalysisRepository;
        this.pathwayAnalysisGenerationRepository = pathwayAnalysisGenerationRepository;
        this.sourceService = sourceService;
        this.jobTemplate = jobTemplate;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilders = jobBuilders;
        this.entityManager = entityManager;
        SerializedPathwayAnalysisToPathwayAnalysisConverter.setConversionService(conversionService);
    }

    @Override
    public PathwayAnalysisEntity create(PathwayAnalysisEntity toSave) {

        PathwayAnalysisEntity newAnalysis = new PathwayAnalysisEntity();

        newAnalysis.setName(toSave.getName());

        toSave.getTargetCohorts().forEach(tc -> {
            tc.setPathwayAnalysis(newAnalysis);
            newAnalysis.getTargetCohorts().add(tc);
        });

        toSave.getEventCohorts().forEach(ec -> {
            ec.setPathwayAnalysis(newAnalysis);
            newAnalysis.getEventCohorts().add(ec);
        });

        newAnalysis.setMaxDepth(toSave.getMaxDepth());
        newAnalysis.setMinCellCount(toSave.getMinCellCount());
        newAnalysis.setCombinationWindow(toSave.getCombinationWindow());

        newAnalysis.setCreatedBy(getCurrentUser());
        newAnalysis.setCreatedDate(new Date());

        return save(newAnalysis);
    }

    @Override
    public Page<PathwayAnalysisEntity> getPage(final Pageable pageable) {

        return pathwayAnalysisRepository.findAll(pageable, defaultEntityGraph);
    }

    @Override
    public PathwayAnalysisEntity getById(Integer id) {

        return pathwayAnalysisRepository.findOne(id, defaultEntityGraph);
    }

    @Override
    public PathwayAnalysisEntity update(PathwayAnalysisEntity forUpdate) {

        PathwayAnalysisEntity existing = getById(forUpdate.getId());

        existing.setModifiedBy(getCurrentUser());
        existing.setModifiedDate(new Date());

        existing.setName(forUpdate.getName());
        existing.setMaxDepth(forUpdate.getMaxDepth());
        existing.setMinCellCount(forUpdate.getMinCellCount());
        existing.setCombinationWindow(forUpdate.getCombinationWindow());

        existing.getTargetCohorts().clear();
        forUpdate.getTargetCohorts().forEach(tc -> {
            tc.setPathwayAnalysis(existing);
            existing.getTargetCohorts().add(tc);
        });

        existing.getEventCohorts().clear();
        forUpdate.getEventCohorts().forEach(ec -> {
            ec.setPathwayAnalysis(existing);
            existing.getEventCohorts().add(ec);
        });

        return save(existing);
    }

    @Override
    public void delete(Integer id) {

        // TODO:
        // remove permissions associated with the entity

        pathwayAnalysisRepository.delete(id);
    }

    @Override
    public Map<Integer, Integer> getEventCohortCodes(PathwayAnalysisEntity pathwayAnalysis) {

        Integer index = 1;

        List<PathwayEventCohort> sortedEventCohortsCopy = pathwayAnalysis.getEventCohorts()
                .stream()
                .sorted(Comparator.comparing(PathwayEventCohort::getName))
                .collect(Collectors.toList());

        Map<Integer, Integer> idToIndexMap = new HashMap<>();

        for (PathwayEventCohort eventCohort : sortedEventCohortsCopy) {
            idToIndexMap.put(eventCohort.getId(), index++);
        }

        return idToIndexMap;
    }

    @Override
    public String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId) {

        Map<Integer, Integer> eventCohortCodes = getEventCohortCodes(pathwayAnalysis);
        Source source = sourceService.findBySourceId(sourceId);

        checkSourceAccess(source);

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
                generationId.toString(),
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
    public void generatePathways(final Integer pathwayAnalysisId, final Integer sourceId) {

        SerializedPathwayAnalysisToPathwayAnalysisConverter designConverter = new SerializedPathwayAnalysisToPathwayAnalysisConverter();

        PathwayAnalysisEntity pathwayAnalysis = getById(pathwayAnalysisId);
        Source source = getSourceRepository().findBySourceId(sourceId);

        checkSourceAccess(source);

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(JOB_NAME, String.format("Generating Pathway Analysis %d using %s (%s)", pathwayAnalysisId, source.getSourceName(), source.getSourceKey()));
        builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
        builder.addString(PATHWAY_ANALYSIS_ID, pathwayAnalysis.getId().toString());
        builder.addString(DESIGN, designConverter.convertToDatabaseColumn(pathwayAnalysis));
        builder.addString(HASH_CODE, String.valueOf(getAnalysisHashCode(pathwayAnalysis)));

        final JobParameters jobParameters = builder.toJobParameters();

        GeneratePathwayAnalysisTasklet generateCcTasklet =
                new GeneratePathwayAnalysisTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), this);

        Step generatePathwayAnalysisStep = stepBuilderFactory.get("cohortCharacterizations.generate")
                .tasklet(generateCcTasklet)
                .build();

        SimpleJobBuilder generateJobBuilder = jobBuilders.get(GENERATE_PATHWAY_ANALYSIS)
                .start(generatePathwayAnalysisStep);

        Job generateCohortJob = generateJobBuilder.build();
        this.jobTemplate.launch(generateCohortJob, jobParameters);
    }

    public PathwayAnalysisGeneration getGeneration(Long generationId) {

        return pathwayAnalysisGenerationRepository.findOne(generationId, EntityUtils.fromAttributePaths("source"));
    }

    @Override
    public PathwayAnalysisResult getResultingPathways(final Long generationId) {

        PathwayAnalysisGeneration generation = getGeneration(generationId);
        Source source = generation.getSource();

        checkSourceAccess(source);

        PathwayAnalysisResult result = new PathwayAnalysisResult();

        PreparedStatementRenderer pathwayEventsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/getPersonLevelPathwayResults.sql",
                new String[]{"target_database_schema"},
                new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                "pathway_analysis_generation_id",
                generationId
        );

        List<PersonPathwayEvent> events = getSourceJdbcTemplate(source).query(pathwayEventsPsr.getSql(), pathwayEventsPsr.getSetter(), (rs, rowNum) -> {
            PersonPathwayEvent event = new PersonPathwayEvent();
            event.setComboId(rs.getInt("combo_id"));
            event.setSubjectId(rs.getInt("subject_id"));
            event.setStartDate(rs.getDate("cohort_start_date"));
            event.setEndDate(rs.getDate("cohort_end_date"));
            return event;
        });

        events.stream().map(PersonPathwayEvent::getComboId).distinct().forEach(code -> {
            List<PathwayEventCohort> eventCohorts = getEventCohortsByComboCode(generation.getDesign(), code);
            String names = eventCohorts.stream()
                    .map(PathwayEventCohort::getName)
                    .collect(Collectors.joining(","));
            result.getCodes().put(code, names);
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

    private List<PathwayEventCohort> getEventCohortsByComboCode(PathwayAnalysisEntity pathwayAnalysis, Integer comboCode) {

        Map<Integer, Integer> eventCodes = getEventCohortCodes(pathwayAnalysis);
        return pathwayAnalysis.getEventCohorts()
                .stream()
                .filter(ec -> ((int) Math.pow(2, Double.valueOf(eventCodes.get(ec.getId()))) & comboCode) > 0)
                .collect(Collectors.toList());
    }

    private int getAnalysisHashCode(PathwayAnalysisEntity pathwayAnalysis) {

        SerializedPathwayAnalysisToPathwayAnalysisConverter designConverter = new SerializedPathwayAnalysisToPathwayAnalysisConverter();
        return designConverter.convertToDatabaseColumn(pathwayAnalysis).hashCode();
    }

    private PathwayAnalysisEntity save(PathwayAnalysisEntity pathwayAnalysis) {

        pathwayAnalysis = pathwayAnalysisRepository.saveAndFlush(pathwayAnalysis);
        entityManager.refresh(pathwayAnalysis);
        pathwayAnalysis = getById(pathwayAnalysis.getId());
        pathwayAnalysis.setHashCode(getAnalysisHashCode(pathwayAnalysis));
        return pathwayAnalysis;
    }

}
