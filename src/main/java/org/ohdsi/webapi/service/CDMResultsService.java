package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cache.ResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCache;
import org.ohdsi.webapi.cdmresults.CDMResultsCacheTasklet;
import org.ohdsi.webapi.report.*;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author fdefalco
 */
@Path("/cdmresults")
@Component
public class CDMResultsService extends AbstractDaoService {

    private CDMResultsAnalysisRunner queryRunner = null;

    @Autowired
    private JobTemplate jobTemplate;

    @PostConstruct
    public void init() {
        queryRunner = new CDMResultsAnalysisRunner(this.getSourceDialect());
    }

    private final RowMapper<SimpleEntry<Long, Long[]>> rowMapper = new RowMapper<SimpleEntry<Long, Long[]>>() {
        @Override
        public SimpleEntry<Long, Long[]> mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            long id = resultSet.getLong("concept_id");
            long record_count = resultSet.getLong("record_count");
            long descendant_record_count = resultSet.getLong("descendant_record_count");

            SimpleEntry<Long, Long[]> entry = new SimpleEntry<Long, Long[]>(id, new Long[]{record_count, descendant_record_count});
            return entry;
        }
    };

    @Path("{sourceKey}/conceptRecordCount")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<SimpleEntry<Long, Long[]>> getConceptRecordCount(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
        ResultsCache resultsCache = new ResultsCache();
        CDMResultsCache sourceCache = resultsCache.getCache(sourceKey);
        if (sourceCache != null && sourceCache.warm) {
            ArrayList<SimpleEntry<Long, Long[]>> listFromCache = new ArrayList<>();
            for (String identifier : identifiers) {
                Long id = Long.parseLong(identifier);
                Long[] counts = sourceCache.cache.get(id);
                SimpleEntry<Long, Long[]> se = new SimpleEntry<>(id, counts);
                listFromCache.add(se);
            }
            return listFromCache;
        }

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        for (int i = 0;
                i < identifiers.length;
                i++) {
            identifiers[i] = "'" + identifiers[i] + "'";
        }

        String identifierList = StringUtils.join(identifiers, ",");
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getConceptRecordCount.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[]{"resultTableQualifier", "vocabularyTableQualifier", "conceptIdentifiers"}, new String[]{resultTableQualifier, vocabularyTableQualifier, identifierList});
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

        return getSourceJdbcTemplate(source)
                .query(sql_statement, rowMapper);
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
        if (cache != null) {
            return new JobExecutionResource();
        }

        Source source = getSourceRepository().findBySourceKey(sourceKey);
        CDMResultsCacheTasklet tasklet = new CDMResultsCacheTasklet(this.getSourceJdbcTemplate(source), source);
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("jobName", "warming " + sourceKey + " cache ");
        return this.jobTemplate.launchTasklet("warmCache", "warmCacheStep", tasklet, builder.toJobParameters());
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
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getDrugEraPrevalenceByGenderAgeYear.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[]{"ohdsi_database_schema", "vocabulary_database_schema", "conceptId"}, new String[]{tableQualifier, vocabularyTableQualifier, conceptId});
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

        List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        List<DrugEraPrevalence> listOfResults = new ArrayList<DrugEraPrevalence>();
        for (Map rs : rows) {
            DrugEraPrevalence d = new DrugEraPrevalence();
            d.conceptId = Long.valueOf(String.valueOf(rs.get("concept_id")));
            d.trellisName = String.valueOf(rs.get("trellis_name"));
            d.seriesName = String.valueOf(rs.get("series_name"));
            d.xCalendarYear = Long.valueOf(String.valueOf(rs.get("x_calendar_year")));
            d.yPrevalence1000Pp = Float.valueOf(String.valueOf(rs.get("y_prevalence_1000pp")));
            listOfResults.add(d);
        }

        return listOfResults;
    }

    @Path("{sourceKey}/conditionoccurrencetreemap")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ConditionOccurrenceTreemapNode> getConditionOccurrenceTreemap(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

        for (int i = 0; i < identifiers.length; i++) {
            identifiers[i] = "'" + identifiers[i] + "'";
        }

        String identifierList = StringUtils.join(identifiers, ",");
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getConditionOccurrenceTreemap.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[]{"ohdsi_database_schema", "cdm_database_schema", "conceptIdList"}, new String[]{tableQualifier, cdmTableQualifier, identifierList});
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

        List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        List<ConditionOccurrenceTreemapNode> listOfResults = new ArrayList<ConditionOccurrenceTreemapNode>();
        for (Map rs : rows) {
            ConditionOccurrenceTreemapNode c = new ConditionOccurrenceTreemapNode();
            c.conceptId = Long.valueOf(String.valueOf(rs.get("concept_id")));
            c.conceptPath = String.valueOf(rs.get("concept_path"));
            c.numPersons = Long.valueOf(String.valueOf(rs.get("num_persons")));
            c.percentPersons = Float.valueOf(String.valueOf(rs.get("percent_persons")));
            c.recordsPerPerson = Float.valueOf(String.valueOf(rs.get("records_per_person")));
            listOfResults.add(c);
        }

        return listOfResults;
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

}
