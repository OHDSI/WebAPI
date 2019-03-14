package org.ohdsi.webapi.pathway;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.google.common.base.MoreObjects;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.internal.CohortPathways;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.pathway.dto.internal.PathwayCode;
import org.ohdsi.webapi.pathway.dto.internal.PathwayGenerationStats;
import org.ohdsi.webapi.pathway.dto.internal.PersonPathwayEvent;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.PathwayAnalysisGenerationId;
import org.ohdsi.webapi.shiro.annotations.SourceId;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CopyUtils;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.summingInt;
import static org.ohdsi.webapi.Constants.GENERATE_PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.JOB_AUTHOR;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.PATHWAY_ANALYSIS_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;

@Service
@Transactional
public class PathwayServiceImpl extends AbstractDaoService implements PathwayService, GeneratesNotification {
    private final PathwayAnalysisEntityRepository pathwayAnalysisRepository;
    private final PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository;
    private final SourceService sourceService;
    private final JobTemplate jobTemplate;
    private final EntityManager entityManager;
    private final DesignImportService designImportService;
    private final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;
    private final UserRepository userRepository;
    private final GenerationUtils generationUtils;
    private final JobService jobService;

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
            EntityManager entityManager,
            Security security,
            DesignImportService designImportService,
            AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            UserRepository userRepository,
            GenerationUtils generationUtils,
            JobService jobService) {

        this.pathwayAnalysisRepository = pathwayAnalysisRepository;
        this.pathwayAnalysisGenerationRepository = pathwayAnalysisGenerationRepository;
        this.sourceService = sourceService;
        this.jobTemplate = jobTemplate;
        this.entityManager = entityManager;
        this.jobService = jobService;
        this.security = security;
        this.designImportService = designImportService;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.userRepository = userRepository;
        this.generationUtils = generationUtils;

        SerializedPathwayAnalysisToPathwayAnalysisConverter.setConversionService(conversionService);
    }

    @Override
    public PathwayAnalysisEntity create(PathwayAnalysisEntity toSave) {

        PathwayAnalysisEntity newAnalysis = new PathwayAnalysisEntity();

        copyProps(toSave, newAnalysis);

        toSave.getTargetCohorts().forEach(tc -> {
            tc.setId(null);
            tc.setPathwayAnalysis(newAnalysis);
            newAnalysis.getTargetCohorts().add(tc);
        });

        toSave.getEventCohorts().forEach(ec -> {
            ec.setId(null);
            ec.setPathwayAnalysis(newAnalysis);
            newAnalysis.getEventCohorts().add(ec);
        });

        newAnalysis.setCreatedBy(getCurrentUser());
        newAnalysis.setCreatedDate(new Date());
        return save(newAnalysis);
    }

    @Override
    public PathwayAnalysisEntity importAnalysis(PathwayAnalysisEntity toImport) {

        PathwayAnalysisEntity newAnalysis = new PathwayAnalysisEntity();

        copyProps(toImport, newAnalysis);

        Stream.concat(toImport.getTargetCohorts().stream(), toImport.getEventCohorts().stream()).forEach(pc -> {
            CohortDefinition cohortDefinition = designImportService.persistCohortOrGetExisting(pc.getCohortDefinition());
            pc.setId(null);
            pc.setCohortDefinition(cohortDefinition);
            pc.setPathwayAnalysis(newAnalysis);
            if (pc instanceof PathwayTargetCohort) {
                newAnalysis.getTargetCohorts().add((PathwayTargetCohort) pc);
            } else {
                newAnalysis.getEventCohorts().add((PathwayEventCohort) pc);
            }
        });

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
    public int countLikeName(String name) {

        return pathwayAnalysisRepository.countByNameStartsWith(name);
    }
    
    @Override
    public String getNameForCopy(String dtoName) {
        return CopyUtils.getNameForCopy(dtoName, this::countLikeName, pathwayAnalysisRepository.findByName(dtoName));
    }

    @Override
    public PathwayAnalysisEntity update(PathwayAnalysisEntity forUpdate) {

        PathwayAnalysisEntity existing = getById(forUpdate.getId());

        copyProps(forUpdate, existing);
        updateCohorts(existing, existing.getTargetCohorts(), forUpdate.getTargetCohorts());
        updateCohorts(existing, existing.getEventCohorts(), forUpdate.getEventCohorts());

        existing.setModifiedBy(getCurrentUser());
        existing.setModifiedDate(new Date());

        return save(existing);
    }

    private <T extends PathwayCohort> void updateCohorts(PathwayAnalysisEntity analysis, Set<T> existing, Set<T> forUpdate) {

        Set<PathwayCohort> removedCohorts = existing
                .stream()
                .filter(ec -> !forUpdate.contains(ec))
                .collect(Collectors.toSet());
        existing.removeAll(removedCohorts);
        forUpdate.forEach(updatedCohort -> existing.stream()
                .filter(ec -> ec.equals(updatedCohort))
                .findFirst()
                .map(ec -> {
                    ec.setName(updatedCohort.getName());
                    return ec;
                })
                .orElseGet(() -> {
                    updatedCohort.setId(null);
                    updatedCohort.setPathwayAnalysis(analysis);
                    existing.add(updatedCohort);
                    return updatedCohort;
                }));
    }

    @Override
    public void delete(Integer id) {

        pathwayAnalysisRepository.delete(id);
    }

    @Override
    public Map<Integer, Integer> getEventCohortCodes(PathwayAnalysisEntity pathwayAnalysis) {

        Integer index = 0;

        List<PathwayEventCohort> sortedEventCohortsCopy = pathwayAnalysis.getEventCohorts()
                .stream()
                .sorted(Comparator.comparing(PathwayEventCohort::getName))
                .collect(Collectors.toList());

        Map<Integer, Integer> cohortDefIdToIndexMap = new HashMap<>();

        for (PathwayEventCohort eventCohort : sortedEventCohortsCopy) {
            cohortDefIdToIndexMap.put(eventCohort.getCohortDefinition().getId(), index++);
        }

        return cohortDefIdToIndexMap;
    }

    @Override
    @DataSourceAccess
    public String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, @SourceId Integer sourceId, String cohortTable, String sessionId) {

        Map<Integer, Integer> eventCohortCodes = getEventCohortCodes(pathwayAnalysis);
        Source source = sourceService.findBySourceId(sourceId);
        final StringJoiner joiner = new StringJoiner("\n\n");

        String analysisSql = ResourceHelper.GetResourceAsString("/resources/pathway/runPathwayAnalysis.sql");
        String eventCohortInputSql = ResourceHelper.GetResourceAsString("/resources/pathway/eventCohortInput.sql");

        String tempTableQualifier = SourceUtils.getTempQualifier(source);
        String resultsTableQualifier = SourceUtils.getResultsQualifier(source);

        String eventCohortIdIndexSql = eventCohortCodes.entrySet()
                .stream()
                .map(ec -> {
                    String[] params = new String[]{"cohort_definition_id", "event_cohort_index"};
                    String[] values = new String[]{ec.getKey().toString(), ec.getValue().toString()};
                    return SqlRender.renderSql(eventCohortInputSql, params, values);
                })
                .collect(Collectors.joining(" UNION ALL "));

        pathwayAnalysis.getTargetCohorts().forEach(tc -> {

            String[] params = new String[]{
                    "generation_id",
                    "event_cohort_id_index_map",
                    "temp_database_schema",
                    "target_database_schema",
                    "target_cohort_table",
                    "pathway_target_cohort_id",
                    "max_depth",
                    "combo_window",
                    "allow_repeats"
            };
            String[] values = new String[]{
                    generationId.toString(),
                    eventCohortIdIndexSql,
                    tempTableQualifier,
                    resultsTableQualifier,
                    cohortTable,
                    tc.getCohortDefinition().getId().toString(),
                    pathwayAnalysis.getMaxDepth().toString(),
                    MoreObjects.firstNonNull(pathwayAnalysis.getCombinationWindow(), 1).toString(),
                    String.valueOf(pathwayAnalysis.isAllowRepeats())
            };

            String renderedSql = SqlRender.renderSql(analysisSql, params, values);
            String translatedSql = SqlTranslate.translateSql(renderedSql, source.getSourceDialect(), sessionId, SourceUtils.getTempQualifier(source));

            joiner.add(translatedSql);
        });

        return joiner.toString();
    }

    @Override
    public String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId) {

        return buildAnalysisSql(generationId, pathwayAnalysis, sourceId, "cohort", SessionUtils.sessionId());
    }


    @Override
    @DataSourceAccess
    public JobExecutionResource generatePathways(final Integer pathwayAnalysisId, final @SourceId Integer sourceId) {

        PathwayService pathwayService = this;

        PathwayAnalysisEntity pathwayAnalysis = getById(pathwayAnalysisId);
        Source source = getSourceRepository().findBySourceId(sourceId);

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(JOB_NAME, String.format("Generating Pathway Analysis %d using %s (%s)", pathwayAnalysisId, source.getSourceName(), source.getSourceKey()));
        builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
        builder.addString(PATHWAY_ANALYSIS_ID, pathwayAnalysis.getId().toString());
        builder.addString(JOB_AUTHOR, getCurrentUserLogin());

        JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

        SimpleJobBuilder generateAnalysisJob = generationUtils.buildJobForCohortBasedAnalysisTasklet(
                GENERATE_PATHWAY_ANALYSIS,
                source,
                builder,
                jdbcTemplate,
                chunkContext -> {
                    Integer analysisId = Integer.valueOf(chunkContext.getStepContext().getJobParameters().get(PATHWAY_ANALYSIS_ID).toString());
                    PathwayAnalysisEntity analysis = pathwayService.getById(analysisId);
                    return Stream.concat(analysis.getTargetCohorts().stream(), analysis.getEventCohorts().stream())
                            .map(PathwayCohort::getCohortDefinition)
                            .collect(Collectors.toList());
                },
                new GeneratePathwayAnalysisTasklet(
                        getSourceJdbcTemplate(source),
                        getTransactionTemplate(),
                        pathwayService,
                        analysisGenerationInfoEntityRepository,
                        userRepository,
                        sourceService
                )
        );

        final JobParameters jobParameters = builder.toJobParameters();

        return jobService.runJob(generateAnalysisJob.build(), jobParameters);
    }

    @Override
    @DataSourceAccess
    public void cancelGeneration(Integer pathwayAnalysisId, @SourceId Integer sourceId) {

        jobService.cancelJobExecution(GENERATE_PATHWAY_ANALYSIS, j -> {
            JobParameters jobParameters = j.getJobParameters();
            return Objects.equals(jobParameters.getString(PATHWAY_ANALYSIS_ID), Integer.toString(pathwayAnalysisId))
                    && Objects.equals(jobParameters.getString(SOURCE_ID), String.valueOf(sourceId));
        });
    }

    @Override
    public List<PathwayAnalysisGenerationEntity> getPathwayGenerations(final Integer pathwayAnalysisId) {

        return pathwayAnalysisGenerationRepository.findAllByPathwayAnalysisId(pathwayAnalysisId, EntityUtils.fromAttributePaths("source"));
    }

    @Override
    public PathwayAnalysisGenerationEntity getGeneration(Long generationId) {

        return pathwayAnalysisGenerationRepository.findOne(generationId, EntityUtils.fromAttributePaths("source"));
    }

    @Override
    @DataSourceAccess
    public PathwayAnalysisResult getResultingPathways(final @PathwayAnalysisGenerationId Long generationId) {

        PathwayAnalysisGenerationEntity generation = getGeneration(generationId);
        Source source = generation.getSource();
        Set<Integer> comboCodes = new HashSet<>();

        PathwayAnalysisResult result = new PathwayAnalysisResult();

        List<PathwayGenerationStats> targetCohortStatsList = queryGenerationStats(source, generationId);

        generation.getDesign().getTargetCohorts().forEach(tc -> {

            CohortPathways cohortPathways = new CohortPathways();
            cohortPathways.setCohortId(tc.getCohortDefinition().getId());

            targetCohortStatsList.stream()
                    .filter(s -> Objects.equals(s.getTargetCohortId(), cohortPathways.getCohortId()))
                    .findFirst()
                    .ifPresent(targetCohortStats -> {
                        cohortPathways.setTargetCohortCount(targetCohortStats.getTargetCohortCount());
                        cohortPathways.setTotalPathwaysCount(targetCohortStats.getPathwaysCount());
                    });

            List<PersonPathwayEvent> events = queryGenerationResults(source, generationId, tc.getCohortDefinition().getId());
            events.forEach(e -> comboCodes.add(e.getComboId()));

            Map<String, Integer> chains = events.stream()
                    .collect(Collectors.groupingBy(PersonPathwayEvent::getSubjectId))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        entry.getValue().sort(Comparator.comparing(PersonPathwayEvent::getStartDate));
                        return entry.getValue().stream().map(e -> e.getComboId().toString()).collect(Collectors.joining("-"));
                    })
                    .collect(Collectors.groupingBy(Function.identity(), summingInt(x -> 1)))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() > generation.getDesign().getMinCellCount())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            cohortPathways.setPathwaysCounts(chains);

            result.getCohortPathwaysList().add(cohortPathways);
        });

        comboCodes.forEach(code -> {
            List<PathwayEventCohort> eventCohorts = getEventCohortsByComboCode(generation.getDesign(), code);
            String names = eventCohorts.stream()
                    .map(PathwayEventCohort::getName)
                    .collect(Collectors.joining(","));
            result.getCodes().add(new PathwayCode(code, names, eventCohorts.size() > 1));
        });

        return result;
    }

    private List<PersonPathwayEvent> queryGenerationResults(Source source, Long generationId, Integer targetCohortId) {

        PreparedStatementRenderer pathwayEventsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/getPersonLevelPathwayResults.sql",
                new String[]{"target_database_schema"},
                new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                new String[] { "pathway_analysis_generation_id", "target_cohort_id" },
                new Object[] { generationId, targetCohortId }
        );

        return getSourceJdbcTemplate(source).query(pathwayEventsPsr.getSql(), pathwayEventsPsr.getSetter(), (rs, rowNum) -> {
            PersonPathwayEvent event = new PersonPathwayEvent();
            event.setComboId(rs.getInt("combo_id"));
            event.setSubjectId(rs.getInt("subject_id"));
            event.setStartDate(rs.getDate("cohort_start_date"));
            event.setEndDate(rs.getDate("cohort_end_date"));
            return event;
        });
    }

    private List<PathwayGenerationStats> queryGenerationStats(Source source, Long generationId) {

        PreparedStatementRenderer pathwayStatsPsr = new PreparedStatementRenderer(
                source,
                "/resources/pathway/getStats.sql",
                new String[]{"target_database_schema"},
                new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results)},
                new String[] { "generation_id" },
                new Object[] { generationId }
        );

        return getSourceJdbcTemplate(source).query(pathwayStatsPsr.getSql(), pathwayStatsPsr.getSetter(), (rs, rowNum) -> {
            PathwayGenerationStats stats = new PathwayGenerationStats();
            stats.setTargetCohortId(rs.getInt("target_cohort_id"));
            stats.setTargetCohortCount(rs.getInt("target_cohort_count"));
            stats.setPathwaysCount(rs.getInt("pathways_count"));
            return stats;
        });
    }

    private void copyProps(PathwayAnalysisEntity from, PathwayAnalysisEntity to) {
        
        to.setName(from.getName());
        to.setMaxDepth(from.getMaxDepth());
        to.setMinCellCount(from.getMinCellCount());
        to.setCombinationWindow(from.getCombinationWindow());
    }

    private List<PathwayEventCohort> getEventCohortsByComboCode(PathwayAnalysisEntity pathwayAnalysis, Integer comboCode) {

        Map<Integer, Integer> eventCodes = getEventCohortCodes(pathwayAnalysis);
        return pathwayAnalysis.getEventCohorts()
                .stream()
                .filter(ec -> ((int) Math.pow(2, Double.valueOf(eventCodes.get(ec.getCohortDefinition().getId()))) & comboCode) > 0)
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

    @Override
    public String getJobName() {
        return GENERATE_PATHWAY_ANALYSIS;
    }

    @Override
    public String getExecutionFoldingKey() {
        return PATHWAY_ANALYSIS_ID;
    }
}
