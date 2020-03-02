package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cache.ResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCacheTasklet;
import org.ohdsi.webapi.cdmresults.DescendantRecordCount;
import org.ohdsi.webapi.cdmresults.cache.CDMResultsCache;
import org.ohdsi.webapi.cdmresults.mapper.DescendantRecordCountMapper;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.report.CDMAchillesHeel;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.ohdsi.webapi.report.CDMDeath;
import org.ohdsi.webapi.report.CDMPersonSummary;
import org.ohdsi.webapi.report.CDMResultsAnalysisRunner;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.cache.annotation.CacheResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.WARM_CACHE_BY_USER;

/**
 * @author fdefalco
 */
@Path("/cdmresults")
@Component
@DependsOn("jobInvalidator")
public class CDMResultsService extends AbstractDaoService {
    private final Logger logger = LoggerFactory.getLogger(CDMResultsService.class);

    private CDMResultsAnalysisRunner queryRunner = null;

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private JobService jobService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceAccessor sourceAccessor;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jasypt.encryptor.enabled}")
    private boolean encryptorEnabled;

    @Value("${cdm.result.cache.warming.enable}")
    private boolean cdmResultCacheWarmingEnable;

    private DescendantRecordCountMapper descendantRecordCountMapper = new DescendantRecordCountMapper();

    @PostConstruct
    public void init() {
        queryRunner = new CDMResultsAnalysisRunner(this.getSourceDialect(), objectMapper);
        warmCaches();
    }



    @Path("{sourceKey}/conceptRecordCount")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<SimpleEntry<Integer, List<Long>>> getConceptRecordCount(@PathParam("sourceKey") String sourceKey, List<Integer> identifiers) {

        Collection<DescendantRecordCount> recordCounts = ResultsCache.get(sourceKey)
                .findAndCache(identifiers, idsForRequest -> {
                    Source source = getSourceRepository().findBySourceKey(sourceKey);
                    return this.executeGetConceptRecordCount(idsForRequest, source);
                });

        return convertToResponse(recordCounts);
    }

    private List<SimpleEntry<Integer, List<Long>>> convertToResponse(Collection<DescendantRecordCount> conceptRecordCounts) {
        return conceptRecordCounts.stream()
                .map(descendantRecordCount -> new SimpleEntry<>(
                        descendantRecordCount.getId(),
                        Arrays.asList(descendantRecordCount.getRecordCount(), descendantRecordCount.getDescendantRecordCount())
                ))
                .collect(Collectors.toList());
    }
    
    protected List<DescendantRecordCount> executeGetConceptRecordCount(List<Integer> identifiers, Source source) {
        List<DescendantRecordCount> returnVal = new ArrayList<>();
        if (identifiers.size() == 0) {
            return returnVal;
        } else {
            // Take into account the fact that the identifiers are used in 2
            // places in the target query so the parameter limit will need to be divided
            int parameterLimit = Math.floorDiv(PreparedSqlRender.getParameterLimit(source), 2);
            if (parameterLimit > 0 && identifiers.size() > parameterLimit) {
                returnVal = executeGetConceptRecordCount(identifiers.subList(parameterLimit, identifiers.size()), source);
                logger.debug("executeGetConceptRecordCount: " + returnVal.size());


                identifiers = identifiers.subList(0, parameterLimit );
            }
            PreparedStatementRenderer psr = prepareGetConceptRecordCount(identifiers.toArray(new Integer[0]), source);
            List<DescendantRecordCount> descendantRecordCounts = getSourceJdbcTemplate(source)
                    .query(psr.getSql(), psr.getSetter(),
                            (resultSet, rowNum) -> descendantRecordCountMapper.mapRow(resultSet));

            returnVal.addAll(descendantRecordCounts);
        }
        return returnVal;
    }

    protected PreparedStatementRenderer prepareGetConceptRecordCount(Integer[] identifiers, Source source) {

        String sqlPath = "/resources/cdmresults/sql/getConceptRecordCount.sql";

        String resultTableQualifierName = "resultTableQualifier";
        String vocabularyTableQualifierName = "vocabularyTableQualifier";
        String resultTableQualifierValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifierValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String[] tableQualifierNames = {resultTableQualifierName, vocabularyTableQualifierName};
        String[] tableQualifierValues = {resultTableQualifierValue, vocabularyTableQualifierValue};
        return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, "conceptIdentifiers", identifiers);
    }

    /**
     * Queries for dashboard report for the sourceKey
     *
     * @return CDMDashboard
     */
    @GET
    @Path("{sourceKey}/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
		@CacheResult(cacheName="datasources.dashboard")
    public CDMDashboard getDashboard(@PathParam("sourceKey")
            final String sourceKey) {

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        CDMDashboard dashboard = queryRunner.getDashboard(getSourceJdbcTemplate(source), source);
        return dashboard;
    }

    /**
     * Queries for person report for the sourceKey
     *
     * @return CDMPersonSummary
     */
    @GET
    @Path("{sourceKey}/person")
    @Produces(MediaType.APPLICATION_JSON)
		@CacheResult(cacheName="datasources.person")
    public CDMPersonSummary getPerson(@PathParam("sourceKey")
            final String sourceKey, @DefaultValue("false")
            @QueryParam("refresh") boolean refresh) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        CDMPersonSummary person = this.queryRunner.getPersonResults(this.getSourceJdbcTemplate(source), source);
        return person;
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
                JobExecutionResource jobExecutionResource = jobService.findJobByName(Constants.WARM_CACHE, getWarmCacheJobName(sourceKey));
                if (jobExecutionResource == null) {
                    if (source.getDaimons().stream().anyMatch(sd -> Objects.equals(sd.getDaimonType(), SourceDaimon.DaimonType.Results))) {
                        return warmCache(source, WARM_CACHE_BY_USER);
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
    public CDMAchillesHeel getAchillesHeelReport(@PathParam("sourceKey")
            final String sourceKey, @DefaultValue("false")
            @QueryParam("refresh") boolean refresh) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        CDMAchillesHeel cdmAchillesHeel = this.queryRunner.getHeelResults(this.getSourceJdbcTemplate(source), source);
        return cdmAchillesHeel;
    }

    /**
     * Queries for data density report for the given sourceKey
     *
     * @return CDMDataDensity
     */
    @GET
    @Path("{sourceKey}/datadensity")
    @Produces(MediaType.APPLICATION_JSON)
        @CacheResult(cacheName="datasources.dataDensity")
    public CDMDataDensity getDataDensity(@PathParam("sourceKey")
            final String sourceKey, @DefaultValue("false")
            @QueryParam("refresh") boolean refresh) {
        CDMDataDensity cdmDataDensity;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        cdmDataDensity = this.queryRunner.getDataDensityResults(this.getSourceJdbcTemplate(source), source);
        return cdmDataDensity;
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
    public CDMDeath getDeath(@PathParam("sourceKey")
            final String sourceKey, @DefaultValue("false")
            @QueryParam("refresh") boolean refresh) {
        CDMDeath cdmDeath;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        cdmDeath = this.queryRunner.getDeathResults(this.getSourceJdbcTemplate(source), source);
        return cdmDeath;
    }

    /**
     * Queries for domain treemap results
     *
     * @return List<ArrayNode>
     */
    @GET
    @Path("{sourceKey}/{domain}/")
    @Produces(MediaType.APPLICATION_JSON)
		@CacheResult(cacheName="datasources.domain")				
    public ArrayNode getTreemap(
            @PathParam("domain")
            final String domain,
            @PathParam("sourceKey")
            final String sourceKey) {
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
		@CacheResult(cacheName="datasources.drilldown")		
    public JsonNode getDrilldown(@PathParam("domain")
            final String domain,
            @PathParam("conceptId")
            final int conceptId,
            @PathParam("sourceKey")
            final String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        JdbcTemplate jdbcTemplate = this.getSourceJdbcTemplate(source);
        return queryRunner.getDrilldown(jdbcTemplate, domain, conceptId, source);
    }

    private void warmCaches(){
        sourceService.getSources()
                .stream()
                .filter(s ->
                        s.getDaimons().stream().anyMatch(sd -> Objects.equals(sd.getDaimonType(), SourceDaimon.DaimonType.Results)) &&
                                s.getDaimons().stream().anyMatch(sd -> sd.getPriority() > 0))
                .forEach(s -> warmCacheByKey(s.getSourceKey()));
    }

    private JobExecutionResource warmCacheByKey(String sourceKey) {
        CDMResultsCache cache = ResultsCache.get(sourceKey);
        if (cache.notWarm() && jobService.findJobByName(Constants.WARM_CACHE, getWarmCacheJobName(sourceKey)) == null) {
            Source source = getSourceRepository().findBySourceKey(sourceKey);
            return warmCache(source, Constants.WARM_CACHE);
        } else {
            return new JobExecutionResource();
        }
    }

    private JobExecutionResource warmCache(final Source source, final String jobName) {

        if (!cdmResultCacheWarmingEnable) {
            logger.info("Cache warming is disabled for CDM results");
            return new JobExecutionResource();
        }

        CDMResultsCacheTasklet tasklet = new CDMResultsCacheTasklet(this.getSourceJdbcTemplate(source), source);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(Constants.Params.JOB_NAME, getWarmCacheJobName(source.getSourceKey()));
        return this.jobTemplate.launchTasklet(jobName, "warmCacheStep", tasklet, builder.toJobParameters());
    }

    private String getWarmCacheJobName(String sourceKey) {
        return "warming " + sourceKey + " cache";
    }

}
