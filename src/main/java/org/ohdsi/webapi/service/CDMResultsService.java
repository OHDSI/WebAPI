package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.achilles.aspect.AchillesCache;
import org.ohdsi.webapi.achilles.service.AchillesCacheService;
import org.ohdsi.webapi.cdmresults.AchillesCacheTasklet;
import org.ohdsi.webapi.cdmresults.CDMResultsCacheTasklet;
import org.ohdsi.webapi.cdmresults.DescendantRecordAndPersonCount;
import org.ohdsi.webapi.cdmresults.DescendantRecordCount;
import org.ohdsi.webapi.cdmresults.domain.CDMCacheEntity;
import org.ohdsi.webapi.cdmresults.service.CDMCacheService;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.ohdsi.webapi.report.CDMDeath;
import org.ohdsi.webapi.report.CDMObservationPeriod;
import org.ohdsi.webapi.report.CDMPersonSummary;
import org.ohdsi.webapi.report.CDMResultsAnalysisRunner;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.Params.JOB_START_TIME;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.DASHBOARD;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.DATA_DENSITY;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.DEATH;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.DRILLDOWN;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.OBSERVATION_PERIOD;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.PERSON;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.TREEMAP;

/**
 * CDM Results Service - provides REST endpoints for CDM results and Achilles reports.
 *
 * @author fdefalco
 */
@RestController
@RequestMapping("/cdmresults")
public class CDMResultsService extends AbstractDaoService {
    private final Logger logger = LoggerFactory.getLogger(CDMResultsService.class);

    private static final String CONCEPT_COUNT_SQL = "/resources/cdmresults/sql/getConceptRecordCount.sql";

    private static final String CONCEPT_COUNT_PERSON_SQL = "/resources/cdmresults/sql/getConceptRecordPersonCount.sql";

    @Autowired
    private CDMResultsAnalysisRunner queryRunner;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SourceService sourceService;

    

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${cdm.result.cache.warming.enable}")
    private boolean cdmResultCacheWarmingEnable;

    @Value("${cdm.cache.cron.warming.enable}")
    private boolean cdmCacheCronWarmingEnable;
    
    @Value("${cdm.cache.achilles.warming.enable}")
    private boolean cdmAchillesCacheWarmingEnable;

    @Value("${cache.achilles.usePersonCount:false}")
    private boolean usePersonCount;

    @Value("${cache.jobs.count:3}")
    private int cacheJobsCount;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AchillesCacheService cacheService;

    @Autowired
    private CDMCacheService cdmCacheService;

    @Autowired
    private ConversionService conversionService;

    @PostConstruct
    public void initQueryRunner() {
        queryRunner.init(this.getSourceDialect(), objectMapper);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        warmCaches();
    }

    @Scheduled(cron = "${cdm.cache.cron.expression}")
    public void scheduledWarmCaches(){
        if (cdmCacheCronWarmingEnable) {
            warmCaches();
        }
    }

    /**
     * Get the record count and descendant record count for one or more concepts in a single CDM database
     *
     * <p>
     *     This POST request accepts a json array containing one or more concept IDs. (e.g. [201826, 437827])
     * </p>
     *
     * @param sourceKey The unique identifier for a CDM source (e.g. SYNPUF5PCT)
     * @param identifiers List of concept IDs
     *
     * @return A javascript object with one element per concept. Each element is an array of lenth two containing the
     * record count and descendent record count for the concept.
     *
     * <p>
     *     [
     *     {
     *         "201826": [
     *             612861,
     *             653173
     *         ]
     *     },
     *     {
     *         "437827": [
     *             224421,
     *             224421
     *         ]
     *     }
     * ]
     * </p>
     * For concept id "201826" in the SYNPUF5PCT data source the record count is 612861 and the descendant record count is 653173.
     */
    @PostMapping(value = "/{sourceKey}/conceptRecordCount",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleEntry<Integer, List<Long>>> getConceptRecordCount(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody List<Integer> identifiers) {
        Source source = sourceService.findBySourceKey(sourceKey);
        if (source != null) {
            List<CDMCacheEntity> entities = cdmCacheService.findAndCache(source, identifiers);
            List<DescendantRecordCount> recordCounts = entities.stream()
                    .map(entity -> {
                        if (usePersonCount) {
                            return conversionService.convert(entity, DescendantRecordAndPersonCount.class);
                        } else {
                            return conversionService.convert(entity, DescendantRecordCount.class);
                        }
                    })
                    .collect(Collectors.toList());
            return convertToResponse(recordCounts);
        }
        return Collections.emptyList();
    }

    private List<SimpleEntry<Integer, List<Long>>> convertToResponse(Collection<DescendantRecordCount> conceptRecordCounts) {
        return conceptRecordCounts.stream()
                .map(c -> new SimpleEntry<>(c.getId(), c.getValues()))
                .collect(Collectors.toList());
    }

    /**
     * Queries for dashboard report for the sourceKey
     *
     * @param sourceKey The source key
     * @return CDMDashboard
     */
    @GetMapping(value = "/{sourceKey}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(DASHBOARD)
    public CDMDashboard getDashboard(@PathVariable("sourceKey") final String sourceKey) {
        return getRawDashboard(sourceKey);
    }

    public CDMDashboard getRawDashboard(final String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return queryRunner.getDashboard(getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for person report for the sourceKey
     *
     * @param sourceKey The source key
     * @return CDMPersonSummary
     */
    @GetMapping(value = "/{sourceKey}/person", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(PERSON)
    public CDMPersonSummary getPerson(@PathVariable("sourceKey") final String sourceKey) {
        return getRawPerson(sourceKey);
    }

    public CDMPersonSummary getRawPerson(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getPersonResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Warm the results cache for a selected source
     *
     * @summary Warm cache for source key
     * @param sourceKey The source key
     * @return The job execution information
     */
    @GetMapping(value = "/{sourceKey}/warmCache", produces = MediaType.APPLICATION_JSON_VALUE)
    public JobExecutionResource warmCache(@PathVariable("sourceKey") final String sourceKey) {
        return this.warmCacheByKey(sourceKey);
    }

    /**
     * Refresh the results cache for a selected source
     *
     * @summary Refresh results cache
     * @param sourceKey The source key
     * @return The job execution resource
     */
    @GetMapping(value = "/{sourceKey}/refreshCache", produces = MediaType.APPLICATION_JSON_VALUE)
    public JobExecutionResource refreshCache(@PathVariable("sourceKey") final String sourceKey) {
        // TODO: Add Source Permission Check
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        JobExecutionResource jobExecutionResource = jobService.findJobByName(Constants.WARM_CACHE, getWarmCacheJobName(String.valueOf(source.getSourceId()),sourceKey));
        if (jobExecutionResource == null) {
            if (source.getDaimons().stream().anyMatch(sd -> Objects.equals(sd.getDaimonType(), SourceDaimon.DaimonType.Results))) {
                return warmCacheByKey(source.getSourceKey());
            }
        } else {
            return jobExecutionResource;
        }
        return new JobExecutionResource();
    }

    /**
     * Clear the cdm_cache and achilles_cache for a specific source
     *
     * @summary Clear the cdm_cache and achilles_cache for a source
     * @param sourceKey The source key
     * @throws ResponseStatusException if the user is not an admin
     */
    @PostMapping(value = "/{sourceKey}/clearCache")
    @Transactional()
    public void clearCacheForSource(@PathVariable("sourceKey") final String sourceKey) {
      // TODO: Add admin:source permission check
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      cacheService.clearCache(source);
      cdmCacheService.clearCache(source);
    }
    
    /**
     * Clear the cdm_cache and achilles_cache for all sources
     *
     * @summary Clear the cdm_cache and achilles_cache for all sources
     * @throws ResponseStatusException if the user is not an admin
     */
    @PostMapping(value = "/clearCache")
    @Transactional()
    public void clearCache() {
        //TODO: Add admin permission check
        cacheService.clearCache();
        cdmCacheService.clearCache();
    }

    /**
     * Queries for data density report for the given sourceKey
     *
     * @param sourceKey The source key
     * @return CDMDataDensity
     */
    @GetMapping(value = "/{sourceKey}/datadensity", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(DATA_DENSITY)
    public CDMDataDensity getDataDensity(@PathVariable("sourceKey") final String sourceKey) {
        return getRawDataDesity(sourceKey);
    }

    public CDMDataDensity getRawDataDesity(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getDataDensityResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for death report for the given sourceKey
     *
     * @param sourceKey The source key
     * @return CDMDeath
     */
    @GetMapping(value = "/{sourceKey}/death", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(DEATH)
    public CDMDeath getDeath(@PathVariable("sourceKey") final String sourceKey) {
        return getRawDeath(sourceKey);
    }

    public CDMDeath getRawDeath(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getDeathResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for observation period report for the given sourceKey
     *
     * @param sourceKey The source key
     * @return CDMObservationPeriod
     */
    @GetMapping(value = "/{sourceKey}/observationPeriod", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(OBSERVATION_PERIOD)
    public CDMObservationPeriod getObservationPeriod(@PathVariable("sourceKey") final String sourceKey) {
        return getRawObservationPeriod(sourceKey);
    }

    public CDMObservationPeriod getRawObservationPeriod(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getObservationPeriodResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for domain treemap results
     *
     * @param sourceKey The source key
     * @param domain The domain
     * @return ArrayNode
     */
    @GetMapping(value = "/{sourceKey}/{domain}/", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(TREEMAP)
    public ArrayNode getTreemap(
            @PathVariable("sourceKey") final String sourceKey,
            @PathVariable("domain") final String domain) {

        return getRawTreeMap(domain, sourceKey);
    }

    public ArrayNode getRawTreeMap(String domain, String sourceKey) {

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return queryRunner.getTreemap(this.getSourceJdbcTemplate(source), domain, source);
    }

    /**
     * Queries for drilldown results
     *
     * @param sourceKey The source key
     * @param domain The domain for the drilldown
     * @param conceptId The concept ID
     * @return The JSON results
     */
    @GetMapping(value = "/{sourceKey}/{domain}/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @AchillesCache(DRILLDOWN)
    public JsonNode getDrilldown(
            @PathVariable("sourceKey") final String sourceKey,
            @PathVariable("domain") final String domain,
            @PathVariable("conceptId") final int conceptId) {

        return getRawDrilldown(domain, conceptId, sourceKey);
    }

    public JsonNode getRawDrilldown(String domain, int conceptId, String sourceKey) {

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        JdbcTemplate jdbcTemplate = this.getSourceJdbcTemplate(source);
        return queryRunner.getDrilldown(jdbcTemplate, domain, conceptId, source);
    }

    private JobExecutionResource warmCacheByKey(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (jobService.findJobByName(getWarmCacheJobName(String.valueOf(source.getSourceId()), sourceKey), getWarmCacheJobName(String.valueOf(source.getSourceId()), sourceKey)) == null) {
            return warmCaches(source);
        } else {
            return new JobExecutionResource();
        }
    }

    private void warmCaches(){
            Collection<Source> sources =  sourceService.getSources();
            warmCaches(sources);

            if (logger.isInfoEnabled()) {
                List<String> sourceNames = sources
                        .stream()
                        .filter(s -> !SourceUtils.hasSourceDaimon(s, SourceDaimon.DaimonType.Vocabulary)
                                || !SourceUtils.hasSourceDaimon(s, SourceDaimon.DaimonType.Results))
                        .map(Source::getSourceName)
                        .collect(Collectors.toList());
                if (!sourceNames.isEmpty()) {
                    logger.info("Following sources do not have Vocabulary or Result schema and will not be cached: {}",
                            sourceNames.stream().collect(Collectors.joining(", ")));
                }
            }
    }
    
    /*
     * Warm cache for a single source
    */
    private JobExecutionResource warmCaches(Source source) {

        if (!cdmResultCacheWarmingEnable) {
            logger.info("Cache warming is disabled for CDM results");
            return new JobExecutionResource();
        }
        if (!SourceUtils.hasSourceDaimon(source, SourceDaimon.DaimonType.Vocabulary) || !SourceUtils.hasSourceDaimon(source, SourceDaimon.DaimonType.Results)) {
            logger.info("Cache wouldn't be applied to sources without Vocabulary and Result schemas, source [{}] was omitted", source.getSourceName());
            return new JobExecutionResource();
        }
        
        int resultDaimonPriority = getResultsDaimonPriority(source);
        if (!cdmAchillesCacheWarmingEnable && resultDaimonPriority <= 0) {
            logger.info("Cache wouldn't be applied to sources with result daimon priority <= 0 AND when the Achilles cache is disabled, source [{}] was omitted", source.getSourceName());
            return new JobExecutionResource();
        }
        
        String jobName = getWarmCacheJobName(String.valueOf(source.getSourceId()), source.getSourceKey());
        List<Step> jobSteps = createCacheWarmingJobSteps(source, jobName);
        SimpleJobBuilder builder = createJob(jobName, jobSteps);
        return runJob(source.getSourceKey(), source.getSourceId(), jobName, builder);
    }

    /*
     * Warm cache for a collection of sources
    */
    private void warmCaches(Collection<Source> sources) {

        if (!cdmResultCacheWarmingEnable) {
            logger.info("Cache warming is disabled for CDM results");
            return;
        }
        List<Source> vocabularySources = sources.stream()
                .filter(s -> SourceUtils.hasSourceDaimon(s, SourceDaimon.DaimonType.Vocabulary) 
                        && SourceUtils.hasSourceDaimon(s, SourceDaimon.DaimonType.Results)
                        && s.isIsCacheEnabled())
                .collect(Collectors.toList());

        long[] bucketSizes = getBucketSizes(vocabularySources);
        int bucketIndex = 0, counter = 0;
        List<Integer> sourceIds = new ArrayList<>();
        List<String> sourceKeys = new ArrayList<>();
        List<Step> allJobSteps = new ArrayList<>();
        for (Source source : vocabularySources) {
            sourceIds.add(source.getSourceId());
            sourceKeys.add(source.getSourceKey());
            String jobName = getWarmCacheJobName(String.valueOf(source.getSourceId()), source.getSourceKey());
            // Check whether cache job for current source already exists
            if (jobService.findJobByName(jobName, jobName) == null) {
                // Create the job step
                List<Step> jobSteps = createCacheWarmingJobSteps(source, jobName);

                // get priority of the results daimon
                int priority = getResultsDaimonPriority(source);
                // if source has results daimon with high priority - put it at the beginning of the queue 
                if (priority > 0) {
                    allJobSteps.addAll(0, jobSteps);
                } else {
                    allJobSteps.addAll(jobSteps);
                }
            }

            if (counter++ >= bucketSizes[bucketIndex] - 1) {
                if (!allJobSteps.isEmpty()) {
                    String compositeJobName = getWarmCacheJobName(sourceIds.stream().map(String::valueOf)
                            .collect(Collectors.joining(",")), String.join(",", sourceKeys));
                    SimpleJobBuilder builder = createJob(compositeJobName, allJobSteps);
                    runJob(source.getSourceKey(), source.getSourceId(), jobName, builder);
                }
                
                bucketIndex++;
                counter = 0;
                sourceIds.clear();
                sourceKeys.clear();
                allJobSteps.clear();
            }
        }
    }

    private SimpleJobBuilder createJob(String jobName, List<Step> steps) {
        final SimpleJobBuilder[] stepBuilder = {null};
        if (jobService.findJobByName(jobName, jobName) == null && !steps.isEmpty()) {
            JobBuilder jobBuilder = new JobBuilder(jobName, jobRepository);

            
            steps.forEach(step -> {
                if (stepBuilder[0] != null) {
                    stepBuilder[0].next(step);
                } else {
                    stepBuilder[0] = jobBuilder.start(step);
                }
            });
        }
        return stepBuilder[0];
    }

    /*
     * Runs the job and returns the JobExecutionResource
    */
    private JobExecutionResource runJob(String sourceKey, int sourceId, String jobName, SimpleJobBuilder stepBuilder) {
        return jobService.runJob(stepBuilder.build(), new JobParametersBuilder()
                .addString(Constants.Params.JOB_NAME, jobName)

                // batch_job_execution_params.string_val is varchar(250). too many source keys can exceed 250 symbols
                .addString(Constants.Params.SOURCE_KEY, StringUtils.substring(sourceKey, 0, 250))

                .addString(Constants.Params.SOURCE_ID, String.valueOf(sourceId))
                .toJobParameters());
    }
    
    private List<Step> createCacheWarmingJobSteps(Source source, String jobName) {
        int resultDaimonPriority = getResultsDaimonPriority(source);
        SimpleJob job = new SimpleJob(jobName);
        job.setJobRepository(jobRepository);
        List<Step> steps = new ArrayList<>();
        
        if (cdmAchillesCacheWarmingEnable) {
            steps.add(getAchillesStep(source, jobName));
        }
        if (resultDaimonPriority > 0) {
            steps.add(getCountStep(source, jobName));
        }
        return steps;
    }

    private Step getAchillesStep(Source source, String jobStepName) {
        CDMResultsService instance = applicationContext.getBean(CDMResultsService.class);
        AchillesCacheTasklet achillesTasklet = new AchillesCacheTasklet(source, instance, cacheService,
                queryRunner, objectMapper);
        return new StepBuilder(jobStepName + " achilles", jobRepository)
                .tasklet(achillesTasklet, transactionManager)
                .build();
    }

    private Step getCountStep(Source source, String jobStepName) {
        CDMResultsCacheTasklet countTasklet = new CDMResultsCacheTasklet(source, cdmCacheService);
        return new StepBuilder(jobStepName + " counts", jobRepository)
                .tasklet(countTasklet, transactionManager)
                .build();
    }

    private int getResultsDaimonPriority(Source source) {
        Optional<Integer> resultsPriority = source.getDaimons().stream()
                .filter(d -> d.getDaimonType().equals(SourceDaimon.DaimonType.Results))
                .map(SourceDaimon::getPriority)
                .filter(p -> p > 0)
                .findAny();
        return resultsPriority.orElse(0);
    }

    private String getWarmCacheJobName(String sourceIds, String sourceKeys) {
        return getJobName("warming cache", sourceIds, sourceKeys);
    }

    private String getJobName(String jobType, String sourceIds, String sourceKeys) {
        // for multiple sources: try to compose a job name from source keys, and if it is too long - use source ids
        String jobName = String.format("%s: %s", jobType, sourceKeys);

        if (jobName.length() >= 100) { // job name in batch_job_instance is varchar(100)
            jobName = String.format("%s: %s", jobType, sourceIds);

            if (jobName.length() >= 100) { // if we still have more than 100 symbols
                jobName = jobName.substring(0, 88);
                jobName = jobName.substring(0, jobName.lastIndexOf(','))
                        .concat(" and more..."); // todo: this is quick fix. need better solution
            }
        }
        return jobName;
    }
    
    private long[] getBucketSizes(List<Source> vocabularySources) {
        int jobCount = cacheJobsCount;
        long bucketSize, size = vocabularySources.size();
        long[] bucketSizes = new long[cacheJobsCount];
        // Get sizes of all buckets so that their values are approximately equal
        while (jobCount > 0) {
            if (jobCount > 1) {
                bucketSize = Math.round(Math.floor(size * 1.0 / jobCount));
            } else {
                bucketSize = size;
            }
            bucketSizes[cacheJobsCount - jobCount] = bucketSize;
            jobCount--;
            size -= bucketSize;
        }
        return bucketSizes;
    }
}
