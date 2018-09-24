package org.ohdsi.webapi.pathway;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.pathway.converter.SerializedPathwayAnalysisToPathwayAnalysisConverter;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.domain.PathwayCohort;
import org.ohdsi.webapi.pathway.domain.PathwayEventCohort;
import org.ohdsi.webapi.pathway.domain.PathwayTargetCohort;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.pathway.dto.internal.PersonPathwayEvent;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisGenerationRepository;
import org.ohdsi.webapi.pathway.repository.PathwayEventCohortRepository;
import org.ohdsi.webapi.pathway.repository.PathwayTargetCohortRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.PathwayAnalysisGenerationId;
import org.ohdsi.webapi.shiro.annotations.SourceId;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.shiro.management.Security;
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
import java.util.Set;
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
public class PathwayServiceImpl extends AbstractDaoService implements PathwayService {

    private final PathwayAnalysisEntityRepository pathwayAnalysisRepository;
    private final PathwayAnalysisGenerationRepository pathwayAnalysisGenerationRepository;
    private final SourceService sourceService;
    private final JobTemplate jobTemplate;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilders;
    private final EntityManager entityManager;
    private final PermissionManager permissionManager;
    private final Security security;
    private final DesignImportService designImportService;
    private final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;
    private final UserRepository userRepository;
    private final PathwayEventCohortRepository eventCohortRepository;
    private final PathwayTargetCohortRepository targetCohortRepository;

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
            EntityManager entityManager,
            PermissionManager permissionManager,
            Security security,
            DesignImportService designImportService,
            AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository,
            UserRepository userRepository,
            PathwayEventCohortRepository eventCohortRepository,
            PathwayTargetCohortRepository targetCohortRepository
    ) {

        this.pathwayAnalysisRepository = pathwayAnalysisRepository;
        this.pathwayAnalysisGenerationRepository = pathwayAnalysisGenerationRepository;
        this.sourceService = sourceService;
        this.jobTemplate = jobTemplate;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilders = jobBuilders;
        this.entityManager = entityManager;
        this.permissionManager = permissionManager;
        this.security = security;
        this.designImportService = designImportService;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.userRepository = userRepository;
        this.targetCohortRepository = targetCohortRepository;
        this.eventCohortRepository = eventCohortRepository;

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
    @DataSourceAccess
    public String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, @SourceId Integer sourceId) {

        Map<Integer, Integer> eventCohortCodes = getEventCohortCodes(pathwayAnalysis);
        Source source = sourceService.findBySourceId(sourceId);

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
    @DataSourceAccess
    public void generatePathways(final Integer pathwayAnalysisId, final @SourceId Integer sourceId) {

        PathwayAnalysisEntity pathwayAnalysis = getById(pathwayAnalysisId);
        Source source = getSourceRepository().findBySourceId(sourceId);

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(JOB_NAME, String.format("Generating Pathway Analysis %d using %s (%s)", pathwayAnalysisId, source.getSourceName(), source.getSourceKey()));
        builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));
        builder.addString(PATHWAY_ANALYSIS_ID, pathwayAnalysis.getId().toString());
        builder.addString(JOB_AUTHOR, getCurrentUserLogin());

        final JobParameters jobParameters = builder.toJobParameters();

        GeneratePathwayAnalysisTasklet generateCcTasklet = new GeneratePathwayAnalysisTasklet(
                getSourceJdbcTemplate(source),
                getTransactionTemplate(),
                this,
                analysisGenerationInfoEntityRepository,
                userRepository
        );

        Step generatePathwayAnalysisStep = stepBuilderFactory.get("cohortCharacterizations.generate")
                .tasklet(generateCcTasklet)
                .build();

        SimpleJobBuilder generateJobBuilder = jobBuilders.get(GENERATE_PATHWAY_ANALYSIS)
                .start(generatePathwayAnalysisStep);

        Job generateCohortJob = generateJobBuilder.build();
        this.jobTemplate.launch(generateCohortJob, jobParameters);
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
