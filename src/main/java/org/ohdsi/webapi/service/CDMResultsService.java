package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import org.ohdsi.webapi.report.CDMAchillesHeel;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.ohdsi.webapi.report.CDMDeath;
import org.ohdsi.webapi.report.CDMObservationPeriod;
import org.ohdsi.webapi.report.CDMPersonSummary;
import org.ohdsi.webapi.report.CDMResultsAnalysisRunner;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.SourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.HEEL;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.OBSERVATION_PERIOD;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.PERSON;
import static org.ohdsi.webapi.cdmresults.AchillesCacheTasklet.TREEMAP;

/**
 * @author fdefalco
 */
@Path("/cdmresults")
@Component
@DependsOn({"jobInvalidator", "flyway"})
public class CDMResultsService extends AbstractDaoService implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(CDMResultsService.class);

    private static final String CONCEPT_COUNT_SQL = "/resources/cdmresults/sql/getConceptRecordCount.sql";

    private static final String CONCEPT_COUNT_PERSON_SQL = "/resources/cdmresults/sql/getConceptRecordPersonCount.sql";

    @Autowired
    private CDMResultsAnalysisRunner queryRunner;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceAccessor sourceAccessor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    @Value("${cdm.result.cache.warming.enable}")
    private boolean cdmResultCacheWarmingEnable;

    @Value("${cdm.cache.cron.warming.enable}")
    private boolean cdmCacheCronWarmingEnable;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        queryRunner.init(this.getSourceDialect(), objectMapper);
        warmCaches();
    }

    @Scheduled(cron = "${cdm.cache.cron.expression}")
    public void scheduledWarmCaches(){
        if (cdmCacheCronWarmingEnable) {
            warmCaches();
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

    @Path("{sourceKey}/conceptRecordCount")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<SimpleEntry<Integer, List<Long>>> getConceptRecordCount(@PathParam("sourceKey") String sourceKey, List<Integer> identifiers) {
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
     * @return CDMDashboard
     */
    @GET
    @Path("{sourceKey}/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(DASHBOARD)
    public CDMDashboard getDashboard(@PathParam("sourceKey")
            final String sourceKey) {
        return getRawDashboard(sourceKey);
    }

    public CDMDashboard getRawDashboard(final String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return queryRunner.getDashboard(getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for person report for the sourceKey
     *
     * @return CDMPersonSummary
     */
    @GET
    @Path("{sourceKey}/person")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(PERSON)
    public CDMPersonSummary getPerson(@PathParam("sourceKey") final String sourceKey) {
        return getRawPerson(sourceKey);
    }

    public CDMPersonSummary getRawPerson(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getPersonResults(this.getSourceJdbcTemplate(source), source);
    }

    @GET
    @Path("{sourceKey}/warmCache")
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource warmCache(@PathParam("sourceKey") final String sourceKey) {
        return this.warmCacheByKey(sourceKey);
    }

    @GET
    @Path("{sourceKey}/refreshCache")
    @Produces(MediaType.APPLICATION_JSON)
    public JobExecutionResource refreshCache(@PathParam("sourceKey") final String sourceKey) {
        if(isSecured() && isAdmin()) {
            Source source = getSourceRepository().findBySourceKey(sourceKey);
            if (sourceAccessor.hasAccess(source)) {
                JobExecutionResource jobExecutionResource = jobService.findJobByName(Constants.WARM_CACHE, getWarmCacheJobName(String.valueOf(source.getSourceId()),sourceKey));
                if (jobExecutionResource == null) {
                    if (source.getDaimons().stream().anyMatch(sd -> Objects.equals(sd.getDaimonType(), SourceDaimon.DaimonType.Results))) {
                        return warmCacheByKey(source.getSourceKey());
                    }
                } else {
                    return jobExecutionResource;
                }
            }
        }
        return new JobExecutionResource();
    }

    /**
     * Queries for achilles heel report for the given sourceKey
     *
     * @return CDMAchillesHeel
     */
    @GET
    @Path("{sourceKey}/achillesheel")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(HEEL)
    public CDMAchillesHeel getAchillesHeelReport(@PathParam("sourceKey")
            final String sourceKey) {
        return getRawAchillesHeelReport(sourceKey);
    }

    public CDMAchillesHeel getRawAchillesHeelReport(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getHeelResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for data density report for the given sourceKey
     *
     * @return CDMDataDensity
     */
    @GET
    @Path("{sourceKey}/datadensity")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(DATA_DENSITY)
    public CDMDataDensity getDataDensity(@PathParam("sourceKey") final String sourceKey) {
        return getRawDataDesity(sourceKey);
    }

    public CDMDataDensity getRawDataDesity(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getDataDensityResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for death report for the given sourceKey Queries for treemap
     * results
     *
     * @return CDMDataDensity
     */
    @GET
    @Path("{sourceKey}/death")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(DEATH)
    public CDMDeath getDeath(@PathParam("sourceKey") final String sourceKey) {
        return getRawDeath(sourceKey);
    }

    public CDMDeath getRawDeath(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getDeathResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for observation period report for the given sourceKey
     *
     * @return CDMDataDensity
     */
    @GET
    @Path("{sourceKey}/observationPeriod")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(OBSERVATION_PERIOD)
    public CDMObservationPeriod getObservationPeriod(@PathParam("sourceKey") final String sourceKey) {
        return getRawObservationPeriod(sourceKey);
    }

    public CDMObservationPeriod getRawObservationPeriod(String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return this.queryRunner.getObservationPeriodResults(this.getSourceJdbcTemplate(source), source);
    }

    /**
     * Queries for domain treemap results
     *
     * @return List<ArrayNode>
     */
    @GET
    @Path("{sourceKey}/{domain}/")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(TREEMAP)
    public ArrayNode getTreemap(
            @PathParam("domain")
            final String domain,
            @PathParam("sourceKey")
            final String sourceKey) {

        return getRawTreeMap(domain, sourceKey);
    }

    public ArrayNode getRawTreeMap(String domain, String sourceKey) {

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        return queryRunner.getTreemap(this.getSourceJdbcTemplate(source), domain, source);
    }

    /**
     * Queries for drilldown results
     *
     * @return List<ArrayNode>
     */
    @GET
    @Path("{sourceKey}/{domain}/{conceptId}")
    @Produces(MediaType.APPLICATION_JSON)
    @AchillesCache(DRILLDOWN)
    public JsonNode getDrilldown(@PathParam("domain")
            final String domain,
            @PathParam("conceptId")
            final int conceptId,
            @PathParam("sourceKey")
            final String sourceKey) {

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

    private JobExecutionResource warmCaches(Source source) {

        if (!cdmResultCacheWarmingEnable) {
            logger.info("Cache warming is disabled for CDM results");
            return new JobExecutionResource();
        }
        if (!SourceUtils.hasSourceDaimon(source, SourceDaimon.DaimonType.Vocabulary) || !SourceUtils.hasSourceDaimon(source, SourceDaimon.DaimonType.Results)) {
            logger.info("Cache wouldn't be applied to sources without Vocabulary and Result schemas, source [{}] was omitted", source.getSourceName());
            return new JobExecutionResource();
        }
        
        String jobName = getWarmCacheJobName(String.valueOf(source.getSourceId()), source.getSourceKey());
        Step resultsCacheStep = getCountStep(source, jobName);
        Step achillesCacheStep = getAchillesStep(source, jobName);
        SimpleJobBuilder builder = jobBuilders.get(jobName)
                .start(achillesCacheStep);
        
        /* 
        * Only run the results cache step if the results source has a 
        * priority >= 1 
        */
        if (getResultsDaimonPriority(source) > 0) {
            builder = builder.next(resultsCacheStep);
        }

        return createJob(source.getSourceKey(), source.getSourceId(), jobName, builder);
    }

    private void warmCaches(Collection<Source> sources) {

        if (!cdmResultCacheWarmingEnable) {
            logger.info("Cache warming is disabled for CDM results");
            return;
        }
        List<Source> vocabularySources = sources.stream()
                .filter(s -> SourceUtils.hasSourceDaimon(s, SourceDaimon.DaimonType.Vocabulary) 
                        && SourceUtils.hasSourceDaimon(s, SourceDaimon.DaimonType.Results))
                .collect(Collectors.toList());

        long[] bucketSizes = getBucketSizes(vocabularySources);
        int bucketIndex = 0, counter = 0;
        List<Integer> sourceIds = new ArrayList<>();
        List<String> sourceKeys = new ArrayList<>();
        List<Step> jobSteps = new ArrayList<>();
        for (Source source : vocabularySources) {
            sourceIds.add(source.getSourceId());
            sourceKeys.add(source.getSourceKey());
            String jobStepName = getWarmCacheJobName(String.valueOf(source.getSourceId()), source.getSourceKey());
            // Check whether cache job for current source already exists
            if (jobService.findJobByName(jobStepName, jobStepName) == null) {
                // Create the job step
                Step jobStep = getJobStep(source, jobStepName);

                // get priority of the results daimon
                int priority = getResultsDaimonPriority(source);
                // if source has results daimon with high priority - put it at the beginning of the queue 
                if (priority > 0) {
                    jobSteps.add(0, jobStep);
                } else {
                    jobSteps.add(jobStep);
                }
            }

            if (counter++ >= bucketSizes[bucketIndex] - 1) {
                createJob(sourceIds.stream().map(String::valueOf).collect(Collectors.joining(",")),
                        String.join(", ", sourceKeys),
                        jobSteps);
                
                bucketIndex++;
                counter = 0;
                sourceIds.clear();
                sourceKeys.clear();
                jobSteps.clear();
            }
        }
    }

    private Step getJobStep(Source source, String jobStepName) {
        int resultDaimonPriority = getResultsDaimonPriority(source);
        SimpleJob job = new SimpleJob(jobStepName);
        job.setJobRepository(jobRepository);

        job.addStep(getAchillesStep(source, jobStepName));
        if (resultDaimonPriority > 0) {
            job.addStep(getCountStep(source, jobStepName));
        }

        return stepBuilderFactory.get(jobStepName)
                .job(job)
                .parametersExtractor((job1, stepExecution) -> new JobParametersBuilder()
                        .addString(Constants.Params.JOB_NAME, jobStepName)
                        .addString(Constants.Params.SOURCE_KEY, source.getSourceKey())
                        .addString(Constants.Params.SOURCE_ID, String.valueOf(source.getSourceId()))
                        .addString(Constants.Params.JOB_AUTHOR, security.getSubject())
                        .addLong(JOB_START_TIME, System.currentTimeMillis())
                        .toJobParameters())
                .build();
    }

    private void createJob(String sourceIds, String sourceKeys, List<Step> steps) {
        String jobName = getWarmCacheJobName(sourceIds, sourceKeys);
        if (jobService.findJobByName(jobName, jobName) == null && steps.size() > 0) {
            JobBuilder jobBuilder = jobBuilders.get(jobName);

            final SimpleJobBuilder[] stepBuilder = {null};
            steps.forEach(step -> {
                if (stepBuilder[0] != null) {
                    stepBuilder[0].next(step);
                } else {
                    stepBuilder[0] = jobBuilder.start(step);
                }
            });

            if (stepBuilder[0] != null) {
                createJob(sourceKeys, -1, jobName, stepBuilder[0]);
            }
        }
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

    private JobExecutionResource createJob(String sourceKey, int sourceId, String jobName, SimpleJobBuilder stepBuilder) {
        return jobService.runJob(stepBuilder.build(), new JobParametersBuilder()
                .addString(Constants.Params.JOB_NAME, jobName)
                .addString(Constants.Params.SOURCE_KEY, sourceKey)
                .addString(Constants.Params.SOURCE_ID, String.valueOf(sourceId))
                .toJobParameters());
    }

    private Step getAchillesStep(Source source, String jobStepName) {
        CDMResultsService instance = applicationContext.getBean(CDMResultsService.class);
        AchillesCacheTasklet achillesTasklet = new AchillesCacheTasklet(source, instance, cacheService,
                queryRunner, objectMapper);
        return stepBuilderFactory.get(jobStepName + " achilles")
                .tasklet(achillesTasklet)
                .build();
    }

    private Step getCountStep(Source source, String jobStepName) {
        CDMResultsCacheTasklet countTasklet = new CDMResultsCacheTasklet(source, cdmCacheService);
        return stepBuilderFactory.get(jobStepName + " counts")
                .tasklet(countTasklet)
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
        // for multiple sources: try to compose a job name from source keys, and if it is too long - use source ids
        String jobName = String.format("warming cache: %s", sourceKeys);

        if (jobName.length() >= 100) { // job name in batch_job_instance is varchar(100)
            jobName = String.format("warming cache: %s", sourceIds);

            if (jobName.length() >= 100) { // if we still have more than 100 symbols
                jobName = jobName.substring(0, 88);
                jobName = jobName.substring(0, jobName.lastIndexOf(','))
                        .concat(" and more..."); // todo: this is quick fix. need better solution
            }
        }
        return jobName;
    }
}
