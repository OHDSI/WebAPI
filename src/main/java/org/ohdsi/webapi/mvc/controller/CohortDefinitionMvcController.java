package org.ohdsi.webapi.mvc.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.hibernate.Hibernate;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.cohortdefinition.printfriendly.MarkdownRender;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.cohort.CohortChecker;
import org.ohdsi.webapi.check.warning.Warning;
import org.ohdsi.webapi.check.warning.WarningUtils;
import org.ohdsi.webapi.cohortdefinition.*;
import org.ohdsi.webapi.cohortdefinition.dto.*;
import org.ohdsi.webapi.cohortdefinition.event.CohortDefinitionChangedEvent;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.common.generation.GenerateSqlResult;
import org.ohdsi.webapi.common.sensitiveinfo.CohortGenerationSensitiveInfoService;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.*;
import org.ohdsi.webapi.service.dto.CheckResultDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.datasource.SourceIdAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.*;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

/**
 * Spring MVC version of CohortDefinitionService
 *
 * Migration Status: Replaces /service/CohortDefinitionService.java (Jersey)
 * Endpoints: 25+ endpoints for cohort definition management
 * Complexity: High - comprehensive CRUD, versioning, generation, tags, validation
 */
@RestController
@RequestMapping("/cohortdefinition")
public class CohortDefinitionMvcController extends AbstractMvcController {

    private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Autowired
    private TransactionTemplate transactionTemplateRequiresNew;

    @Autowired
    private TransactionTemplate transactionTemplateNoTransaction;

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private CohortGenerationService cohortGenerationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private CohortGenerationSensitiveInfoService sensitiveInfoService;

    @Autowired
    private SourceIdAccessor sourceIdAccessor;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private CohortChecker cohortChecker;

    @Autowired
    private VersionService<CohortVersion> versionService;

    @Value("${security.defaultGlobalReadPermissions}")
    private boolean defaultGlobalReadPermissions;

    private final MarkdownRender markdownPF = new MarkdownRender();
    private final List<Extension> extensions = Arrays.asList(TablesExtension.create());

    private final RowMapper<InclusionRuleReport.Summary> summaryMapper = (rs, rowNum) -> {
        InclusionRuleReport.Summary summary = new InclusionRuleReport.Summary();
        summary.baseCount = rs.getLong("base_count");
        summary.finalCount = rs.getLong("final_count");
        summary.lostCount = rs.getLong("lost_count");

        double matchRatio = (summary.baseCount > 0) ? ((double) summary.finalCount / (double) summary.baseCount) : 0.0;
        summary.percentMatched = new BigDecimal(matchRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        return summary;
    };

    private final RowMapper<InclusionRuleReport.InclusionRuleStatistic> inclusionRuleStatisticMapper = (rs, rowNum) -> {
        InclusionRuleReport.InclusionRuleStatistic statistic = new InclusionRuleReport.InclusionRuleStatistic();
        statistic.id = rs.getInt("rule_sequence");
        statistic.name = rs.getString("name");
        statistic.countSatisfying = rs.getLong("person_count");
        long personTotal = rs.getLong("person_total");

        long gainCount = rs.getLong("gain_count");
        double excludeRatio = personTotal > 0 ? (double) gainCount / (double) personTotal : 0.0;
        String percentExcluded = new BigDecimal(excludeRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
        statistic.percentExcluded = percentExcluded + "%";

        long satisfyCount = rs.getLong("person_count");
        double satisfyRatio = personTotal > 0 ? (double) satisfyCount / (double) personTotal : 0.0;
        String percentSatisfying = new BigDecimal(satisfyRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
        statistic.percentSatisfying = percentSatisfying + "%";
        return statistic;
    };

    private final RowMapper<Long[]> inclusionRuleResultItemMapper = (rs, rowNum) -> {
        Long[] resultItem = new Long[2];
        resultItem[0] = rs.getLong("inclusion_rule_mask");
        resultItem[1] = rs.getLong("person_count");
        return resultItem;
    };

    public static class GenerateSqlRequest {
        @JsonProperty("expression")
        public CohortExpression expression;

        @JsonProperty("options")
        public CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;
    }

    /**
     * Returns OHDSI template SQL for a given cohort definition
     *
     * Jersey: POST /WebAPI/cohortdefinition/sql
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/sql
     *
     * @summary Generate Sql
     */
    @PostMapping(value = "/sql", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GenerateSqlResult> generateSql(@RequestBody GenerateSqlRequest request) {
        CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = request.options;
        GenerateSqlResult result = new GenerateSqlResult();
        if (options == null) {
            options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
        }
        String expressionSql = queryBuilder.buildExpressionQuery(request.expression, options);
        result.templateSql = SqlRender.renderSql(expressionSql, null, null);

        return ok(result);
    }

    /**
     * Returns metadata about all cohort definitions in the WebAPI database
     *
     * Jersey: GET /WebAPI/cohortdefinition/
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/
     *
     * @summary List Cohort Definitions
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @Cacheable(cacheNames = "cohortDefinitionList", key = "@permissionService.getSubjectCacheKey()")
    public ResponseEntity<List<CohortMetadataDTO>> getCohortDefinitionList() {
        List<CohortDefinition> definitions = cohortDefinitionRepository.list();
        List<CohortMetadataDTO> result = definitions.stream()
                .filter(!defaultGlobalReadPermissions ? entity -> permissionService.hasReadAccess(entity) : entity -> true)
                .map(def -> {
                    CohortMetadataDTO dto = conversionService.convert(def, CohortMetadataImplDTO.class);
                    permissionService.fillWriteAccess(def, dto);
                    permissionService.fillReadAccess(def, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ok(result);
    }

    /**
     * Creates a cohort definition in the WebAPI database
     *
     * Jersey: POST /WebAPI/cohortdefinition/
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/
     *
     * @summary Create Cohort Definition
     */
    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    public ResponseEntity<CohortDTO> createCohortDefinition(@RequestBody CohortDTO dto) {
        Date currentTime = Calendar.getInstance().getTime();

        UserEntity user = userRepository.findByLogin(security.getSubject());
        CohortDefinition newDef = new CohortDefinition();
        newDef.setName(StringUtils.trim(dto.getName()))
                .setDescription(dto.getDescription())
                .setExpressionType(dto.getExpressionType());
        newDef.setCreatedBy(user);
        newDef.setCreatedDate(currentTime);

        newDef = this.cohortDefinitionRepository.save(newDef);

        CohortDefinitionDetails details = new CohortDefinitionDetails();
        details.setCohortDefinition(newDef)
                .setExpression(Utils.serialize(dto.getExpression()));

        newDef.setDetails(details);

        CohortDefinition createdDefinition = this.cohortDefinitionRepository.save(newDef);
        return ok(conversionService.convert(createdDefinition, CohortDTO.class));
    }

    /**
     * Returns the 'raw' cohort definition for the given id
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}
     *
     * @summary Get Raw Cohort Definition
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortRawDTO> getCohortDefinitionRaw(@PathVariable("id") final int id) {
        CohortRawDTO result = transactionTemplate.execute(transactionStatus -> {
            CohortDefinition d = this.cohortDefinitionRepository.findOneWithDetail(id);
            ExceptionUtils.throwNotFoundExceptionIfNull(d, String.format("There is no cohort definition with id = %d.", id));
            return conversionService.convert(d, CohortRawDTO.class);
        });
        return ok(result);
    }

    /**
     * Check that a cohort exists
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/exists
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/exists
     *
     * @summary Check Cohort Definition Name
     */
    @GetMapping(value = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getCountCDefWithSameName(
            @PathVariable("id") final int id,
            @RequestParam(value = "name", required = false) String name) {
        return ok(cohortDefinitionRepository.getCountCDefWithSameName(id, name));
    }

    /**
     * Saves the cohort definition for the given id
     *
     * Jersey: PUT /WebAPI/cohortdefinition/{id}
     * Spring MVC: PUT /WebAPI/v2/cohortdefinition/{id}
     *
     * @summary Save Cohort Definition
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    public ResponseEntity<CohortDTO> saveCohortDefinition(@PathVariable("id") final int id, @RequestBody CohortDTO def) {
        Date currentTime = Calendar.getInstance().getTime();

        saveVersion(id);

        CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);
        UserEntity modifier = userRepository.findByLogin(security.getSubject());

        currentDefinition.setName(def.getName())
                .setDescription(def.getDescription())
                .setExpressionType(def.getExpressionType())
                .getDetails().setExpression(Utils.serialize(def.getExpression()));
        currentDefinition.setModifiedBy(modifier);
        currentDefinition.setModifiedDate(currentTime);

        currentDefinition = this.cohortDefinitionRepository.save(currentDefinition);
        eventPublisher.publishEvent(new CohortDefinitionChangedEvent(currentDefinition));

        CohortDTO result = getCohortDefinition(id);
        return ok(result);
    }

    /**
     * Queues up a generate cohort task for the specified cohort definition id
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/generate/{sourceKey}
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/generate/{sourceKey}
     *
     * @summary Generate Cohort
     */
    @GetMapping(value = "/{id}/generate/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobExecutionResource> generateCohort(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") final String sourceKey,
            @RequestParam(value = "demographic", defaultValue = "false") boolean demographicStat) {

        Source source = transactionTemplate.execute(status -> {
            Source s = sourceRepository.findBySourceKey(sourceKey);
            if (s != null) {
                Hibernate.initialize(s);
            }
            return s;
        });

        CohortDefinition currentDefinition = transactionTemplate.execute(status -> {
            CohortDefinition cd = this.cohortDefinitionRepository.findOneWithDetail(id);
            if (cd != null) {
                if (cd.getDetails() != null) {
                    cd.getDetails().getExpression();
                }
                Hibernate.initialize(cd.getGenerationInfoList());
            }
            return cd;
        });

        UserEntity user = transactionTemplate.execute(status -> {
            UserEntity u = userRepository.findByLogin(security.getSubject());
            if (u != null) {
                Hibernate.initialize(u);
            }
            return u;
        });

        JobExecutionResource result = cohortGenerationService.generateCohortViaJob(user, currentDefinition, source, demographicStat);
        return ok(result);
    }

    /**
     * Cancel a cohort generation task
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/cancel/{sourceKey}
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/cancel/{sourceKey}
     *
     * @summary Cancel Cohort Generation
     */
    @GetMapping(value = "/{id}/cancel/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> cancelGenerateCohort(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") final String sourceKey) {

        final Source source = Optional.ofNullable(sourceRepository.findBySourceKey(sourceKey))
                .orElseThrow(() -> new RuntimeException("Source not found"));

        transactionTemplateRequiresNew.execute(status -> {
            CohortDefinition currentDefinition = cohortDefinitionRepository.findById(id).orElse(null);
            if (Objects.nonNull(currentDefinition)) {
                CohortGenerationInfo info = findBySourceId(currentDefinition.getGenerationInfoList(), source.getSourceId());
                if (Objects.nonNull(info)) {
                    invalidateExecution(info);
                    cohortDefinitionRepository.save(currentDefinition);
                }
            }
            return null;
        });

        jobService.cancelJobExecution(e -> {
            JobParameters parameters = e.getJobParameters();
            String jobName = e.getJobInstance().getJobName();
            return Objects.equals(parameters.getString(COHORT_DEFINITION_ID), Integer.toString(id))
                    && Objects.equals(parameters.getString(SOURCE_ID), Integer.toString(source.getSourceId()))
                    && Objects.equals(Constants.GENERATE_COHORT, jobName);
        });

        return ok();
    }

    /**
     * Returns a list of cohort generation info objects
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/info
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/info
     *
     * @summary Get cohort generation info
     */
    @GetMapping(value = "/{id}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<List<CohortGenerationInfoDTO>> getInfo(@PathVariable("id") final int id) {
        CohortDefinition def = this.cohortDefinitionRepository.findById(id).orElse(null);
        ExceptionUtils.throwNotFoundExceptionIfNull(def, String.format("There is no cohort definition with id = %d.", id));

        Set<CohortGenerationInfo> infoList = def.getGenerationInfoList();
        List<CohortGenerationInfo> result = infoList.stream()
                .filter(genInfo -> sourceIdAccessor.hasAccess(genInfo.getId().getSourceId()))
                .collect(Collectors.toList());

        Map<Integer, Source> sourceMap = sourceService.getSourcesMap(SourceMapKey.BY_SOURCE_ID);
        List<CohortGenerationInfo> filteredResult = sensitiveInfoService.filterSensitiveInfo(result,
                gi -> Collections.singletonMap(Constants.Variables.SOURCE, sourceMap.get(gi.getId().getSourceId())));

        List<CohortGenerationInfoDTO> dtos = filteredResult.stream()
                .map(t -> conversionService.convert(t, CohortGenerationInfoDTO.class))
                .collect(Collectors.toList());

        return ok(dtos);
    }

    /**
     * Copies the specified cohort definition
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/copy
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/copy
     *
     * @summary Copy Cohort Definition
     */
    @GetMapping(value = "/{id}/copy", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    public ResponseEntity<CohortDTO> copy(@PathVariable("id") final int id) {
        CohortDTO sourceDef = getCohortDefinition(id);
        sourceDef.setId(null);
        sourceDef.setTags(null);
        sourceDef.setName(NameUtils.getNameForCopy(sourceDef.getName(), this::getNamesLike,
                cohortDefinitionRepository.findByName(sourceDef.getName())));

        CohortDTO copyDef = createCohortDefinition(sourceDef).getBody();
        return ok(copyDef);
    }

    /**
     * Deletes the specified cohort definition
     *
     * Jersey: DELETE /WebAPI/cohortdefinition/{id}
     * Spring MVC: DELETE /WebAPI/v2/cohortdefinition/{id}
     *
     * @summary Delete Cohort Definition
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable("id") final int id) {
        transactionTemplateRequiresNew.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                CohortDefinition def = cohortDefinitionRepository.findById(id).orElse(null);
                if (!Objects.isNull(def)) {
                    def.getGenerationInfoList().forEach(cohortGenerationInfo -> {
                        Integer sourceId = cohortGenerationInfo.getId().getSourceId();

                        jobService.cancelJobExecution(e -> {
                            JobParameters parameters = e.getJobParameters();
                            String jobName = e.getJobInstance().getJobName();
                            return Objects.equals(parameters.getString(COHORT_DEFINITION_ID), Integer.toString(id))
                                    && Objects.equals(parameters.getString(SOURCE_ID), Integer.toString(sourceId))
                                    && Objects.equals(Constants.GENERATE_COHORT, jobName);
                        });
                    });
                    cohortDefinitionRepository.delete(def);
                }
            }
        });

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(JOB_NAME, String.format("Cleanup cohort %d.", id));
        builder.addString(COHORT_DEFINITION_ID, ("" + id));

        final JobParameters jobParameters = builder.toJobParameters();

        CleanupCohortTasklet cleanupTasklet = new CleanupCohortTasklet(transactionTemplateNoTransaction, sourceRepository);

        Step cleanupStep = new StepBuilder("cohortDefinition.cleanupCohort", jobRepository)
                .tasklet(cleanupTasklet, transactionManager)
                .build();

        SimpleJobBuilder cleanupJobBuilder = new JobBuilder("cleanupCohort", jobRepository)
                .start(cleanupStep);

        Job cleanupCohortJob = cleanupJobBuilder.build();

        jobTemplate.launch(cleanupCohortJob, jobParameters);

        return ok();
    }

    /**
     * Return concept sets used in a cohort definition as a zip file
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/export/conceptset
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/export/conceptset
     *
     * @summary Export Concept Sets as ZIP
     */
    @GetMapping(value = "/{id}/export/conceptset", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> exportConceptSets(@PathVariable("id") final int id) {
        Source source = sourceService.getPriorityVocabularySource();
        if (Objects.isNull(source)) {
            return forbidden();
        }

        CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(id);
        if (Objects.isNull(def)) {
            return notFound();
        }

        List<ConceptSetExport> exports = getConceptSetExports(def, new SourceInfo(source));
        ByteArrayOutputStream exportStream = ExportUtil.writeConceptSetExportToCSVAndZip(exports);

        ByteArrayResource resource = new ByteArrayResource(exportStream.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"cohortdefinition_" + def.getId() + "_export.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Get the Inclusion Rule report for the specified source and mode
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/report/{sourceKey}
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/report/{sourceKey}
     *
     * @summary Get Inclusion Rule Report
     */
    @GetMapping(value = "/{id}/report/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<InclusionRuleReport> getInclusionRuleReport(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") final String sourceKey,
            @RequestParam(value = "mode", defaultValue = "0") int modeId,
            @RequestParam(value = "ccGenerateId", required = false) String ccGenerateId) {

        Source source = this.sourceRepository.findBySourceKey(sourceKey);

        InclusionRuleReport.Summary summary = getInclusionRuleReportSummary(whitelist(id), source, modeId);
        List<InclusionRuleReport.InclusionRuleStatistic> inclusionRuleStats = getInclusionRuleStatistics(whitelist(id), source, modeId);
        String treemapData = getInclusionRuleTreemapData(whitelist(id), inclusionRuleStats.size(), source, modeId);

        InclusionRuleReport report = new InclusionRuleReport();
        report.summary = summary;
        report.inclusionRuleStats = inclusionRuleStats;
        report.treemapData = treemapData;

        return ok(report);
    }

    /**
     * Checks the cohort definition for logic issues
     *
     * Jersey: POST /WebAPI/cohortdefinition/check
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/check
     *
     * @summary Check Cohort Definition
     */
    @PostMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<CheckResultDTO> runDiagnostics(@RequestBody CohortExpression expression) {
        Checker checker = new Checker();
        return ok(new CheckResultDTO(checker.check(expression)));
    }

    /**
     * Checks the cohort definition for logic issues (V2 with tags)
     *
     * Jersey: POST /WebAPI/cohortdefinition/checkV2
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/checkV2
     *
     * @summary Check Cohort Definition V2
     */
    @PostMapping(value = "/checkV2", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<CheckResult> runDiagnosticsWithTags(@RequestBody CohortDTO cohortDTO) {
        Checker checker = new Checker();
        CheckResultDTO checkResultDTO = new CheckResultDTO(checker.check(cohortDTO.getExpression()));
        List<Warning> circeWarnings = checkResultDTO.getWarnings().stream()
                .map(WarningUtils::convertCirceWarning)
                .collect(Collectors.toList());
        CheckResult checkResult = new CheckResult(cohortChecker.check(cohortDTO));
        checkResult.getWarnings().addAll(circeWarnings);
        return ok(checkResult);
    }

    /**
     * Render a cohort expression in html or markdown form
     *
     * Jersey: POST /WebAPI/cohortdefinition/printfriendly/cohort
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/printfriendly/cohort
     *
     * @summary Cohort Print Friendly
     */
    @PostMapping(value = "/printfriendly/cohort", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cohortPrintFriendly(
            @RequestBody CohortExpression expression,
            @RequestParam(value = "format", defaultValue = "html") String format) {
        String markdown = convertCohortExpressionToMarkdown(expression);
        return printFriendly(markdown, format);
    }

    /**
     * Render a list of concept sets in html or markdown form
     *
     * Jersey: POST /WebAPI/cohortdefinition/printfriendly/conceptsets
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/printfriendly/conceptsets
     *
     * @summary Concept Set Print Friendly
     */
    @PostMapping(value = "/printfriendly/conceptsets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> conceptSetListPrintFriendly(
            @RequestBody List<ConceptSet> conceptSetList,
            @RequestParam(value = "format", defaultValue = "html") String format) {
        String markdown = markdownPF.renderConceptSetList(conceptSetList.toArray(new ConceptSet[0]));
        return printFriendly(markdown, format);
    }

    /**
     * Assign tag to Cohort Definition
     *
     * Jersey: POST /WebAPI/cohortdefinition/{id}/tag/
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/{id}/tag/
     *
     * @summary Assign Tag
     */
    @PostMapping(value = "/{id}/tag", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    @Transactional
    public ResponseEntity<Void> assignTag(@PathVariable("id") final Integer id, @RequestBody int tagId) {
        CohortDefinition entity = cohortDefinitionRepository.findById(id).orElse(null);
        assignTag(entity, tagId);
        return ok();
    }

    /**
     * Unassign tag from Cohort Definition
     *
     * Jersey: DELETE /WebAPI/cohortdefinition/{id}/tag/{tagId}
     * Spring MVC: DELETE /WebAPI/v2/cohortdefinition/{id}/tag/{tagId}
     *
     * @summary Unassign Tag
     */
    @DeleteMapping(value = "/{id}/tag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    @Transactional
    public ResponseEntity<Void> unassignTag(@PathVariable("id") final Integer id, @PathVariable("tagId") final int tagId) {
        CohortDefinition entity = cohortDefinitionRepository.findById(id).orElse(null);
        unassignTag(entity, tagId);
        return ok();
    }

    /**
     * Assign protected tag to Cohort Definition
     *
     * Jersey: POST /WebAPI/cohortdefinition/{id}/protectedtag/
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/{id}/protectedtag/
     *
     * @summary Assign Protected Tag
     */
    @PostMapping(value = "/{id}/protectedtag", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> assignPermissionProtectedTag(@PathVariable("id") final int id, @RequestBody int tagId) {
        return assignTag(id, tagId);
    }

    /**
     * Unassign protected tag from Cohort Definition
     *
     * Jersey: DELETE /WebAPI/cohortdefinition/{id}/protectedtag/{tagId}
     * Spring MVC: DELETE /WebAPI/v2/cohortdefinition/{id}/protectedtag/{tagId}
     *
     * @summary Unassign Protected Tag
     */
    @DeleteMapping(value = "/{id}/protectedtag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> unassignPermissionProtectedTag(@PathVariable("id") final int id, @PathVariable("tagId") final int tagId) {
        return unassignTag(id, tagId);
    }

    /**
     * Get list of versions of Cohort Definition
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/version/
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/version/
     *
     * @summary Get Cohort Definition Versions
     */
    @GetMapping(value = "/{id}/version", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<List<VersionDTO>> getVersions(@PathVariable("id") final long id) {
        List<VersionBase> versions = versionService.getVersions(VersionType.COHORT, id);
        List<VersionDTO> dtos = versions.stream()
                .map(v -> conversionService.convert(v, VersionDTO.class))
                .collect(Collectors.toList());
        return ok(dtos);
    }

    /**
     * Get version of Cohort Definition
     *
     * Jersey: GET /WebAPI/cohortdefinition/{id}/version/{version}
     * Spring MVC: GET /WebAPI/v2/cohortdefinition/{id}/version/{version}
     *
     * @summary Get Cohort Definition Version
     */
    @GetMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<CohortVersionFullDTO> getVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version) {
        checkVersion(id, version, false);
        CohortVersion cohortVersion = versionService.getById(VersionType.COHORT, id, version);
        return ok(conversionService.convert(cohortVersion, CohortVersionFullDTO.class));
    }

    /**
     * Updates version of Cohort Definition
     *
     * Jersey: PUT /WebAPI/cohortdefinition/{id}/version/{version}
     * Spring MVC: PUT /WebAPI/v2/cohortdefinition/{id}/version/{version}
     *
     * @summary Update Version
     */
    @PutMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<VersionDTO> updateVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version,
            @RequestBody VersionUpdateDTO updateDTO) {
        checkVersion(id, version);
        updateDTO.setAssetId(id);
        updateDTO.setVersion(version);
        CohortVersion updated = versionService.update(VersionType.COHORT, updateDTO);
        return ok(conversionService.convert(updated, VersionDTO.class));
    }

    /**
     * Delete version of Cohort Definition
     *
     * Jersey: DELETE /WebAPI/cohortdefinition/{id}/version/{version}
     * Spring MVC: DELETE /WebAPI/v2/cohortdefinition/{id}/version/{version}
     *
     * @summary Delete Cohort Definition Version
     */
    @DeleteMapping(value = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Void> deleteVersion(@PathVariable("id") final int id, @PathVariable("version") final int version) {
        checkVersion(id, version);
        versionService.delete(VersionType.COHORT, id, version);
        return ok();
    }

    /**
     * Create a new asset from version of Cohort Definition
     *
     * Jersey: PUT /WebAPI/cohortdefinition/{id}/version/{version}/createAsset
     * Spring MVC: PUT /WebAPI/v2/cohortdefinition/{id}/version/{version}/createAsset
     *
     * @summary Create Cohort from Version
     */
    @PutMapping(value = "/{id}/version/{version}/createAsset", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @CacheEvict(cacheNames = "cohortDefinitionList", allEntries = true)
    public ResponseEntity<CohortDTO> copyAssetFromVersion(
            @PathVariable("id") final int id,
            @PathVariable("version") final int version) {
        checkVersion(id, version, false);
        CohortVersion cohortVersion = versionService.getById(VersionType.COHORT, id, version);
        CohortVersionFullDTO fullDTO = conversionService.convert(cohortVersion, CohortVersionFullDTO.class);
        CohortDTO dto = conversionService.convert(fullDTO.getEntityDTO(), CohortDTO.class);
        dto.setId(null);
        dto.setTags(null);
        dto.setName(NameUtils.getNameForCopy(dto.getName(), this::getNamesLike,
                cohortDefinitionRepository.findByName(dto.getName())));
        CohortDTO created = createCohortDefinition(dto).getBody();
        return ok(created);
    }

    /**
     * Get list of cohort definitions with assigned tags
     *
     * Jersey: POST /WebAPI/cohortdefinition/byTags
     * Spring MVC: POST /WebAPI/v2/cohortdefinition/byTags
     *
     * @summary List Cohorts By Tag
     */
    @PostMapping(value = "/byTags", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<List<CohortDTO>> listByTags(@RequestBody TagNameListRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
            return ok(Collections.emptyList());
        }
        List<String> names = requestDTO.getNames().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<CohortDefinition> entities = cohortDefinitionRepository.findByTags(names);
        List<CohortDTO> result = listByTags(entities, names, CohortDTO.class);
        return ok(result);
    }

    // Helper methods

    private CohortGenerationInfo findBySourceId(Set<CohortGenerationInfo> infoList, Integer sourceId) {
        for (CohortGenerationInfo info : infoList) {
            if (info.getId().getSourceId().equals(sourceId)) {
                return info;
            }
        }
        return null;
    }

    private InclusionRuleReport.Summary getInclusionRuleReportSummary(int id, Source source, int modeId) {
        String sql = "select cs.base_count, cs.final_count, cc.lost_count from @tableQualifier.cohort_summary_stats cs left join @tableQualifier.cohort_censor_stats cc "
                + "on cc.cohort_definition_id = cs.cohort_definition_id where cs.cohort_definition_id = @id and cs.mode_id = @modeId";
        String tqName = "tableQualifier";
        String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String[] varNames = {"id", "modeId"};
        Object[] varValues = {whitelist(id), whitelist(modeId)};
        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, varNames, varValues, SessionUtils.sessionId());
        List<InclusionRuleReport.Summary> result = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), summaryMapper);
        return result.isEmpty() ? new InclusionRuleReport.Summary() : result.get(0);
    }

    private List<InclusionRuleReport.InclusionRuleStatistic> getInclusionRuleStatistics(int id, Source source, int modeId) {
        String sql = "select i.rule_sequence, i.name, s.person_count, s.gain_count, s.person_total"
                + " from @tableQualifier.cohort_inclusion i join @tableQualifier.cohort_inclusion_stats s on i.cohort_definition_id = s.cohort_definition_id"
                + " and i.rule_sequence = s.rule_sequence"
                + " where i.cohort_definition_id = @id and mode_id = @modeId ORDER BY i.rule_sequence";
        String tqName = "tableQualifier";
        String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String[] varNames = {"id", "modeId"};
        Object[] varValues = {whitelist(id), whitelist(modeId)};
        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, varNames, varValues, SessionUtils.sessionId());
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), inclusionRuleStatisticMapper);
    }

    private int countSetBits(long n) {
        int count = 0;
        while (n > 0) {
            n &= (n - 1);
            count++;
        }
        return count;
    }

    private String formatBitMask(Long n, int size) {
        return StringUtils.reverse(StringUtils.leftPad(Long.toBinaryString(n), size, "0"));
    }

    private String getInclusionRuleTreemapData(int id, int inclusionRuleCount, Source source, int modeId) {
        String sql = "select inclusion_rule_mask, person_count from @tableQualifier.cohort_inclusion_result where cohort_definition_id = @id and mode_id = @modeId";
        String tqName = "tableQualifier";
        String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String[] varNames = {"id", "modeId"};
        Object[] varValues = {whitelist(id), whitelist(modeId)};
        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, varNames, varValues, SessionUtils.sessionId());

        List<Long[]> items = this.getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), inclusionRuleResultItemMapper);
        Map<Integer, List<Long[]>> groups = new HashMap<>();
        for (Long[] item : items) {
            int bitsSet = countSetBits(item[0]);
            if (!groups.containsKey(bitsSet)) {
                groups.put(bitsSet, new ArrayList<Long[]>());
            }
            groups.get(bitsSet).add(item);
        }

        StringBuilder treemapData = new StringBuilder("{\"name\" : \"Everyone\", \"children\" : [");

        List<Integer> groupKeys = new ArrayList<>(groups.keySet());
        Collections.sort(groupKeys);
        Collections.reverse(groupKeys);

        int groupCount = 0;
        for (Integer groupKey : groupKeys) {
            if (groupCount > 0) {
                treemapData.append(",");
            }

            treemapData.append(String.format("{\"name\" : \"Group %d\", \"children\" : [", groupKey));

            int groupItemCount = 0;
            for (Long[] groupItem : groups.get(groupKey)) {
                if (groupItemCount > 0) {
                    treemapData.append(",");
                }

                treemapData.append(String.format("{\"name\": \"%s\", \"size\": %d}", formatBitMask(groupItem[0], inclusionRuleCount), groupItem[1]));
                groupItemCount++;
            }
            groupCount++;
        }

        treemapData.append(StringUtils.repeat("]}", groupCount + 1));

        return treemapData.toString();
    }

    private List<ConceptSetExport> getConceptSetExports(CohortDefinition def, SourceInfo vocabSource) throws RuntimeException {
        CohortExpression expression;
        try {
            expression = objectMapper.readValue(def.getDetails().getExpression(), CohortExpression.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Arrays.stream(expression.conceptSets)
                .map(cs -> vocabularyService.exportConceptSet(cs, vocabSource))
                .collect(Collectors.toList());
    }

    public String convertCohortExpressionToMarkdown(CohortExpression expression) {
        return markdownPF.renderCohort(expression);
    }

    public String convertMarkdownToHTML(String markdown) {
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        return renderer.render(document);
    }

    private ResponseEntity<String> printFriendly(String markdown, String format) {
        if ("html".equalsIgnoreCase(format)) {
            String html = convertMarkdownToHTML(markdown);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } else if ("markdown".equals(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(markdown);
        } else {
            return ResponseEntity.status(415).build(); // Unsupported Media Type
        }
    }

    private void checkVersion(int id, int version) {
        checkVersion(id, version, true);
    }

    private void checkVersion(int id, int version, boolean checkOwnerShip) {
        Version cohortVersion = versionService.getById(VersionType.COHORT, id, version);
        ExceptionUtils.throwNotFoundExceptionIfNull(cohortVersion,
                String.format("There is no cohort version with id = %d.", version));

        CohortDefinition entity = cohortDefinitionRepository.findById(id).orElse(null);
        if (checkOwnerShip) {
            checkOwnerOrAdminOrGranted(entity);
        }
    }

    private CohortVersion saveVersion(int id) {
        CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(id);
        CohortVersion version = conversionService.convert(def, CohortVersion.class);

        UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
        Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
        version.setCreatedBy(user);
        version.setCreatedDate(versionDate);
        return versionService.create(VersionType.COHORT, version);
    }

    public CohortDTO getCohortDefinition(final int id) {
        return transactionTemplate.execute(transactionStatus -> {
            CohortDefinition d = this.cohortDefinitionRepository.findOneWithDetail(id);
            ExceptionUtils.throwNotFoundExceptionIfNull(d, String.format("There is no cohort definition with id = %d.", id));
            return conversionService.convert(d, CohortDTO.class);
        });
    }

    public List<String> getNamesLike(String copyName) {
        return cohortDefinitionRepository.findAllByNameStartsWith(copyName).stream()
                .map(CohortDefinition::getName)
                .collect(Collectors.toList());
    }

    // Helper service reference for DAO operations
    @Autowired
    private CohortDefinitionService cohortDefinitionService;

    @Autowired
    private TagService tagService;

    @Value("${jdbc.suppressInvalidApiException}")
    protected boolean suppressApiException;

    // Delegate methods to existing service for complex DAO operations
    private CancelableJdbcTemplate getSourceJdbcTemplate(Source source) {
        // Delegate to the existing service implementation
        return cohortDefinitionService.getSourceJdbcTemplate(source);
    }

    private void checkOwnerOrAdminOrGranted(CohortDefinition entity) {
        if (!isSecured()) {
            return;
        }

        UserEntity user = userRepository.findByLogin(security.getSubject());
        Long ownerId = Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy().getId() : null;

        if (!(user.getId().equals(ownerId) || isAdmin() || permissionService.hasWriteAccess(entity))) {
            throw new RuntimeException("Forbidden");
        }
    }

    private CohortGenerationInfo invalidateExecution(CohortGenerationInfo info) {
        info.setIsValid(false);
        info.setStatus(org.ohdsi.webapi.GenerationStatus.COMPLETE);
        info.setMessage("Invalidated by system");
        return info;
    }

    private <T> List<T> listByTags(List<CohortDefinition> entities, List<String> names, Class<T> clazz) {
        return entities.stream()
                .filter(e -> e.getTags().stream()
                        .map(tag -> tag.getName().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toList())
                        .containsAll(names))
                .map(entity -> {
                    T dto = conversionService.convert(entity, clazz);
                    if (dto instanceof org.ohdsi.webapi.service.dto.CommonEntityDTO) {
                        permissionService.fillWriteAccess(entity, (org.ohdsi.webapi.service.dto.CommonEntityDTO) dto);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void assignTag(CohortDefinition entity, int tagId) {
        checkOwnerOrAdminOrGranted(entity);
        if (Objects.nonNull(entity)) {
            org.ohdsi.webapi.tag.domain.Tag tag = tagService.getById(tagId);
            if (Objects.nonNull(tag)) {
                entity.getTags().add(tag);
            }
        }
    }

    private void unassignTag(CohortDefinition entity, int tagId) {
        checkOwnerOrAdminOrGranted(entity);
        if (Objects.nonNull(entity)) {
            org.ohdsi.webapi.tag.domain.Tag tag = tagService.getById(tagId);
            if (Objects.nonNull(tag)) {
                Set<org.ohdsi.webapi.tag.domain.Tag> tags = entity.getTags().stream()
                        .filter(t -> t.getId() != tagId)
                        .collect(Collectors.toSet());
                entity.setTags(tags);
            }
        }
    }
}
