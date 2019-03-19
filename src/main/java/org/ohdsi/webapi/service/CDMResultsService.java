package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cache.ResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCacheTasklet;
import org.ohdsi.webapi.report.*;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

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

    @PostConstruct
    public void init() {
        queryRunner = new CDMResultsAnalysisRunner(this.getSourceDialect());
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
            CDMResultsCacheTasklet tasklet = new CDMResultsCacheTasklet(this.getSourceJdbcTemplate(source), source);
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addString(Constants.Params.JOB_NAME, getWarmCacheJobName(sourceKey));
            return this.jobTemplate.launchTasklet(Constants.WARM_CACHE, "warmCacheStep", tasklet, builder.toJobParameters());
        } else {
            return new JobExecutionResource();
        }
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

    @Path("{sourceKey}/{conceptId}/drugeraprevalence")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DrugEraPrevalence> getDrugEraPrevalenceByGenderAgeYear(@PathParam("sourceKey") String sourceKey, @PathParam("conceptId") String conceptId) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = prepareGetDrugEraPrevalenceByGenderAgeYear(conceptId, source);

        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

            DrugEraPrevalence d = new DrugEraPrevalence();
            d.conceptId = rs.getLong("concept_id");
            d.trellisName = rs.getString("trellis_name");
            d.seriesName = rs.getString("series_name");
            d.xCalendarYear = rs.getLong("x_calendar_year");
            d.yPrevalence1000Pp =rs.getFloat("y_prevalence_1000pp");
            return d;
        });
    }

    protected PreparedStatementRenderer prepareGetDrugEraPrevalenceByGenderAgeYear(String conceptId, Source source) {

        String path = "/resources/cdmresults/sql/getDrugEraPrevalenceByGenderAgeYear.sql";
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
        String[] search = new String[]{"ohdsi_database_schema", "vocabulary_database_schema"};
        String[] replace = new String[]{tableQualifier, vocabularyTableQualifier};
        return new PreparedStatementRenderer(source, path, search, replace, "conceptId", Integer.parseInt(conceptId));
    }

    @Path("{sourceKey}/conditionoccurrencetreemap")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ConditionOccurrenceTreemapNode> getConditionOccurrenceTreemap(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        PreparedStatementRenderer psr = prepareGetConditionOccurrenceTreemap(identifiers, source);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(),new RowMapper<ConditionOccurrenceTreemapNode>() {
            @Override
            public ConditionOccurrenceTreemapNode mapRow(ResultSet rs, int rowNum) throws SQLException {
                ConditionOccurrenceTreemapNode c = new ConditionOccurrenceTreemapNode();
                c.conceptId = rs.getLong("concept_id");
                c.conceptPath = rs.getString("concept_path");
                c.numPersons = rs.getLong("num_persons");
                c.percentPersons = rs.getFloat("percent_persons");
                c.recordsPerPerson = rs.getFloat("records_per_person");
                return c;
            }
        });
    }

    protected PreparedStatementRenderer prepareGetConditionOccurrenceTreemap(String[] identifiers, Source source) {

        String sqlPath = "/resources/cdmresults/sql/getConditionOccurrenceTreemap.sql";
        String resultsName = "ohdsi_database_schema";
        String resultsValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String cdmName = "cdm_database_schema";
        String cdmValue = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
        String[] search = new String[]{resultsName, cdmName};
        String[] replace = new String[]{resultsValue, cdmValue};
        String[] names = new String[]{"conceptIdList"};
        Object[] results = new Object[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            results[i] = Integer.parseInt(identifiers[i]);
        }
        return new PreparedStatementRenderer(source, sqlPath, search, replace, names, new Object[]{results});
    }

    /**
     * Queries for measurement treemap results
     *
     * @return List<ArrayNode>
     */
    @GET
    @Path("{sourceKey}/{domain}/")
    @Produces(MediaType.APPLICATION_JSON)
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

}
