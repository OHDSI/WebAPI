package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cache.ResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCacheTasklet;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.report.*;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.cache.annotation.CacheResult;

import static org.ohdsi.webapi.Constants.WARM_CACHE_BY_USER;

/**
 * @author fdefalco
 */
@Path("/cdmresults")
@Component
public class CDMResultsService extends AbstractDaoService {
    private final Logger logger = LoggerFactory.getLogger(CDMResultsService.class);

    private CDMResultsAnalysisRunner queryRunner = null;

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private JobService jobService;

    @Autowired
    private SourceService sourceService;
    @Value("${jasypt.encryptor.enabled}")
    private boolean encryptorEnabled;

    @Autowired
    private SourceAccessor sourceAccessor;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        queryRunner = new CDMResultsAnalysisRunner(this.getSourceDialect(), objectMapper);
        warmCaches();
    }

    public void warmCaches(){
			sourceService.getSources()
				.stream()
				.filter(s -> s.daimons.stream().anyMatch(sd -> Objects.equals(sd.getDaimonType(), SourceDaimon.DaimonType.Results)) && s.daimons.stream().anyMatch(sd -> sd.getPriority() > 0))
				.forEach(s -> warmCache(s.sourceKey));
    }

    @Path("{sourceKey}/conceptRecordCount")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<SimpleEntry<Integer, Long[]>> getConceptRecordCount(@PathParam("sourceKey") String sourceKey, ArrayList<Integer> identifiers) {
        ResultsCache resultsCache = new ResultsCache();
        CDMResultsCache sourceCache = resultsCache.getCache(sourceKey);

        List<Integer> notCachedRecordIds = new ArrayList<>();

        List<SimpleEntry<Integer, Long[]>> cachedRecordCounts = identifiers.stream()
            .map(id -> {
                Long[] counts = sourceCache.cache.get(id);
                if (counts != null) {
                    return new SimpleEntry<>(id, counts);
                } else {
                    notCachedRecordIds.add(id);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!sourceCache.warm && notCachedRecordIds.size() > 0) {
            Source source = getSourceRepository().findBySourceKey(sourceKey);
            List<SimpleEntry<Integer, Long[]>> queriedList = this.executeGetConceptRecordCount(notCachedRecordIds.toArray(new Integer[notCachedRecordIds.size()]), source, sourceCache);
            cachedRecordCounts.addAll(queriedList);
        }

        return cachedRecordCounts;
    }
    
    protected List<SimpleEntry<Integer, Long[]>> executeGetConceptRecordCount(Integer[] identifiers, Source source, CDMResultsCache sourceCache) {
        List<SimpleEntry<Integer, Long[]>> returnVal = new ArrayList<>();
        if (identifiers.length == 0) {
            return returnVal;
        } else {
            int parameterLimit = PreparedSqlRender.getParameterLimit(source);
            if (parameterLimit > 0 && identifiers.length > parameterLimit) {
                returnVal = executeGetConceptRecordCount(Arrays.copyOfRange(identifiers, parameterLimit, identifiers.length), source, sourceCache);
                logger.debug("executeGetConceptRecordCount: " + returnVal.size());
                identifiers = Arrays.copyOfRange(identifiers, 0, parameterLimit);
            }
            PreparedStatementRenderer psr = prepareGetConceptRecordCount(identifiers, source);
            returnVal.addAll(getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), CDMResultsCacheTasklet.getMapper(sourceCache.cache)));
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
        ResultsCache resultsCache = new ResultsCache();
        CDMResultsCache cache = resultsCache.getCache(sourceKey);
        if (!cache.warm && jobService.findJobByName(Constants.WARM_CACHE, getWarmCacheJobName(sourceKey)) == null) {
            Source source = getSourceRepository().findBySourceKey(sourceKey);
            return warmCache(source, Constants.WARM_CACHE);
        } else {
            return new JobExecutionResource();
        }
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

    private String getWarmCacheJobName(String sourceKey) {

        return "warming " + sourceKey + " cache";
    }

    private JobExecutionResource warmCache(final Source source, final String jobName) {
        CDMResultsCacheTasklet tasklet = new CDMResultsCacheTasklet(this.getSourceJdbcTemplate(source), source);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString(Constants.Params.JOB_NAME, getWarmCacheJobName(source.getSourceKey()));
        return this.jobTemplate.launchTasklet(jobName, "warmCacheStep", tasklet, builder.toJobParameters());
    }
}
