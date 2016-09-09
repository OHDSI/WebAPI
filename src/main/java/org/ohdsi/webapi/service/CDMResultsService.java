package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.report.*;
import org.ohdsi.webapi.cohortresults.VisualizationData;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author fdefalco
 */
@Path("{sourceKey}/cdmresults/")
@Component
public class CDMResultsService extends AbstractDaoService {

    private ObjectMapper mapper = new ObjectMapper();
    private CDMResultsAnalysisRunner queryRunner = null;

    @Path("{conceptId}/monthlyConditionOccurrencePrevalence")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlyPrevalence getMonthlyConditionOccurrencePrevalence(@PathParam("sourceKey") String sourceKey, @PathParam("conceptId") String conceptId) {
        try {
            Source source = getSourceRepository().findBySourceKey(sourceKey);
            String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

            String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getMonthlyConditionOccurrencePrevalence.sql");
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"OHDSI_schema", "conceptId"}, new String[]{tableQualifier, conceptId});
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

            return getSourceJdbcTemplate(source).query(sql_statement, new ResultSetExtractor<MonthlyPrevalence>() {
                @Override
                public MonthlyPrevalence extractData(ResultSet rs) throws SQLException, DataAccessException {
                    MonthlyPrevalence result = new MonthlyPrevalence();
                    while (rs.next()) {
                        result.monthKey.add(rs.getString(1));
                        result.prevalence.add(rs.getFloat(2));
                    }
                    return result;
                }
            });
        } catch (Exception exception) {
            throw new RuntimeException("Error retrieving monthly condition occurrence prevalence statistics." + exception.getMessage());
        }
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

    @Path("conceptRecordCount")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<SimpleEntry<Long, Long[]>> getConceptRecordCount(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String resultTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        for (int i = 0; i < identifiers.length; i++) {
            identifiers[i] = "'" + identifiers[i] + "'";
        }

        String identifierList = StringUtils.join(identifiers, ",");
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getConceptRecordCount.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[]{"resultTableQualifier", "vocabularyTableQualifier", "conceptIdentifiers"}, new String[]{resultTableQualifier, vocabularyTableQualifier, identifierList});
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

        return getSourceJdbcTemplate(source).query(sql_statement, rowMapper);
    }

    /**
     * Queries for CDM dashboard for the given sourceKey
     *
     * @return CDMDashboard
     */
    @GET
    @Path("dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public CDMDashboard getDashboard(@PathParam("sourceKey") final String sourceKey, @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

        final String key = CDMResultsAnalysisRunner.DASHBOARD;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VisualizationData data = /*refresh ?*/ null /* : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key)*/;

        CDMDashboard dashboard = null;

        if (refresh /*|| data == null*/) {
            dashboard = queryRunner.getDashboard(getSourceJdbcTemplate(source), /*id,*/ source, /*demographicsOnly,*/ true);

        } else {
            try {
                dashboard = mapper.readValue(data.getData(), CDMDashboard.class);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return dashboard;

    }

    /**
     * Queries for person report results for the given sourceKey
     *
     * @return CDMPersonSummary
     */
    @GET
    @Path("person")
    @Produces(MediaType.APPLICATION_JSON)
    public CDMPersonSummary getPersonReport(@PathParam("sourceKey") final String sourceKey, @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
        CDMPersonSummary person = null;
        final String key = CDMResultsAnalysisRunner.PERSON;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VisualizationData data = /* refresh ?*/ null /*: this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(source.getSourceId(), key)*/;

        if (refresh || data == null) {
            person = this.queryRunner.getPersonResults(this.getSourceJdbcTemplate(source), source, true);
        } else {
            try {
                person = mapper.readValue(data.getData(), CDMPersonSummary.class);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return person;
    }

    /**
     * Queries for person report results for the given sourceKey
     *
     * @return CDMPersonSummary
     */
    @GET
    @Path("drug")
    @Produces(MediaType.APPLICATION_JSON)
    public CDMDrugSummary getDrugReport(@PathParam("sourceKey") final String sourceKey, @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
        CDMDrugSummary drugSummary = null;
        final String key = CDMResultsAnalysisRunner.PERSON;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VisualizationData data = /*refresh ?*/ null /*: this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(source.getSourceId(), key)*/;

        if (refresh || data == null) {
            drugSummary = this.queryRunner.getDrugResults(this.getSourceJdbcTemplate(source), source, true);
        } else {
            try {
                drugSummary = mapper.readValue(data.getData(), CDMDrugSummary.class);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return drugSummary;
    }

    /**
     * Queries for person report results for the given sourceKey
     *
     * @return CDMPersonSummary
     */
    @GET
    @Path("procedure")
    @Produces(MediaType.APPLICATION_JSON)
    public CDMProcedureSummary getProcedureReport(@PathParam("sourceKey") final String sourceKey, @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
        CDMProcedureSummary procedureSummary = null;
        final String key = CDMResultsAnalysisRunner.PERSON;
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VisualizationData data = /*refresh ?*/ null /*: this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(source.getSourceId(), key)*/;

        if (refresh || data == null) {
            procedureSummary = this.queryRunner.getProcedureResults(this.getSourceJdbcTemplate(source), source, true);
        } else {
            try {
                procedureSummary = mapper.readValue(data.getData(), CDMProcedureSummary.class);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return procedureSummary;
    }

    @Path("drugeratreemap")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<DrugPrevalence> getDrugEraTreemap(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        for (int i = 0; i < identifiers.length; i++) {
            identifiers[i] = "'" + identifiers[i] + "'";
        }

        String identifierList = StringUtils.join(identifiers, ",");
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/cdmresults/sql/getDrugEraTreemap.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[]{"ohdsi_database_schema", "vocabulary_database_schema", "conceptList"}, new String[]{tableQualifier, vocabularyTableQualifier, identifierList});
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());


        List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
        List<DrugPrevalence> listOfResults = new ArrayList<DrugPrevalence>();
        for (Map rs : rows) {
            DrugPrevalence d = new DrugPrevalence();
            d.conceptId = Long.valueOf(String.valueOf(rs.get("concept_id")));
            d.conceptPath = String.valueOf(rs.get("concept_path"));
            d.lengthOfEra = Float.valueOf(String.valueOf(rs.get("length_of_era")));
            d.numPersons = Long.valueOf(String.valueOf(rs.get("num_persons")));
            d.percentPersons = Float.valueOf(String.valueOf(rs.get("percent_persons")));
            listOfResults.add(d);
        }

        return listOfResults;
    }

    @Path("{conceptId}/drugeraprevalence")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DrugEraPrevalence> getDrugEraPrevalenceByGenderAgeYear(@PathParam("sourceKey") String
                                                                               sourceKey, @PathParam("conceptId") String conceptId) {
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

    @Path("conditionoccurrencetreemap")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ConditionOccurrenceTreemapNode> getConditionOccurrenceTreemap(@PathParam("sourceKey") String
                                                                                      sourceKey, String[] identifiers) {
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

    class CDMResultsAnalysisRunner {

        public static final String DASHBOARD = "dashboard";
        public static final String PERSON = "person";
        public static final String BASE_SQL_PATH = "/resources/cdmresults/sql";

        public CDMDashboard getDashboard(JdbcTemplate jdbcTemplate,
                                            /*int id,*/ Source source,
                                            /*boolean demographicsOnly,*/
                                         boolean save) {

            final String key = DASHBOARD;
            CDMDashboard dashboard = new CDMDashboard();
            boolean empty = true;

//        String ageAtFirstObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql", id,
//                minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//        if (ageAtFirstObsSql != null) {
//          dashboard.setAgeAtFirstObservation(jdbcTemplate.query(ageAtFirstObsSql, new ConceptDistributionMapper()));
//        }
//
//        String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam,
//                minIntervalPersonCountParam, source);
//        if (genderSql != null) {
//          dashboard.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
//        }
//
//        if (!demographicsOnly) {
//          String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
//                  minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//          if (cumulObsSql != null) {
//            dashboard.setCumulativeObservation(jdbcTemplate.query(cumulObsSql, new CumulativeObservationMapper()));
//          }
//
//          String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
//                  minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//          if (obsByMonthSql != null) {
//            dashboard.setObservedByMonth(jdbcTemplate.query(obsByMonthSql, new MonthObservationMapper()));
//          }
//        }
//
//        if (CollectionUtils.isNotEmpty(dashboard.getAgeAtFirstObservation())
//                || CollectionUtils.isNotEmpty(dashboard.getCumulativeObservation())
//                || CollectionUtils.isNotEmpty(dashboard.getGender())
//                || CollectionUtils.isNotEmpty(dashboard.getObservedByMonth())) {
//          empty = false;
//        }
//
//        if (!empty && save) {
//          this.saveEntity(id, source.getSourceId(), key, dashboard);
//        }
//
            return dashboard;

        }

        /**
         * Queries for CDM person results for the given source
         *
         * @param jdbcTemplate JDBCTemplate
         * @return CDMPersonSummary
         */
        public CDMPersonSummary getPersonResults(JdbcTemplate jdbcTemplate,
                                                 final Source source,
                                                 boolean save) {

            final String key = PERSON;
            CDMPersonSummary person = new CDMPersonSummary();
            boolean empty = true;

            String personSummaryData = this.renderTranslateCohortSql(BASE_SQL_PATH + "report/person/population.sql", null, source);
//            if (personSummaryData != null) {
//                person.setYearOfBirth(jdbcTemplate.query(yobSql, new ConceptDistributionMapper()));
//            }

//            String yobStatSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (yobStatSql != null) {
//                person.setYearOfBirthStats(jdbcTemplate.query(yobStatSql, new CohortStatsMapper()));
//            }
//
//            String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (genderSql != null) {
//                person.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
//            }
//
//            String raceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (raceSql != null) {
//                person.setRace(jdbcTemplate.query(raceSql, new ConceptCountMapper()));
//            }
//
//            String ethnicitySql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (ethnicitySql != null) {
//                person.setEthnicity(jdbcTemplate.query(ethnicitySql, new ConceptCountMapper()));
//            }
//
//            if (CollectionUtils.isNotEmpty(person.getEthnicity())
//                    || CollectionUtils.isNotEmpty(person.getGender())
//                    || CollectionUtils.isNotEmpty(person.getRace())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirth())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirthStats())) {
//                empty = false;
//            }
//
//            if (!empty && save) {
//                this.saveEntity(id, source.getSourceId(), key, person);
//            }

            return person;
        }

        /**
         * Passes in common params for cdm results, and performs SQL
         * translate/render
         */
        public String renderTranslateCohortSql(String sqlPath, Integer id, Integer conceptId,
                                               Source source) {
            String sql = null;

//            String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
//            String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
//
//            try {
//                String[] cols;
//                String[] colValues;
//                if (conceptId != null) {
//                    cols = DRILLDOWN_COLUMNS;
//                    colValues = new String[]{vocabularyTableQualifier,
//                            resultsTableQualifier, String.valueOf(id),
//                            minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
//                                    : minCovariatePersonCountParam,
//                            minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
//                                    : minIntervalPersonCountParam,
//                            String.valueOf(conceptId)};
//                } else {
//                    cols = STANDARD_COLUMNS;
//                    colValues = new String[]{vocabularyTableQualifier,
//                            resultsTableQualifier, String.valueOf(id),
//                            minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
//                                    : minCovariatePersonCountParam,
//                            minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
//                                    : minIntervalPersonCountParam};
//                }
//
//                sql = ResourceHelper.GetResourceAsString(sqlPath);
//                sql = SqlRender.renderSql(sql, cols, colValues);
//                sql = SqlTranslate.translateSql(sql, sourceDialect, source.getSourceDialect());
//            } catch (Exception e) {
//                log.error(String.format("Unable to translate sql for  %s", sql), e);
//            }

            return sql;
        }

        /**
         * Passes in common params for cdm results, and performs SQL
         * translate/render
         */
        public String renderTranslateCohortSql(String sqlPath, Integer id,
                                               Source source) {
            return renderTranslateCohortSql(sqlPath, id, null, source);
        }

        public CDMDrugSummary getDrugResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
            final String key = PERSON;
            CDMDrugSummary cdmDrugSummary = new CDMDrugSummary();
            boolean empty = true;

            String personSummaryData = this.renderTranslateCohortSql(BASE_SQL_PATH + "report/person/population.sql", null, source);
//            if (personSummaryData != null) {
//                person.setYearOfBirth(jdbcTemplate.query(yobSql, new ConceptDistributionMapper()));
//            }

//            String yobStatSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (yobStatSql != null) {
//                person.setYearOfBirthStats(jdbcTemplate.query(yobStatSql, new CohortStatsMapper()));
//            }
//
//            String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (genderSql != null) {
//                person.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
//            }
//
//            String raceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (raceSql != null) {
//                person.setRace(jdbcTemplate.query(raceSql, new ConceptCountMapper()));
//            }
//
//            String ethnicitySql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (ethnicitySql != null) {
//                person.setEthnicity(jdbcTemplate.query(ethnicitySql, new ConceptCountMapper()));
//            }
//
//            if (CollectionUtils.isNotEmpty(person.getEthnicity())
//                    || CollectionUtils.isNotEmpty(person.getGender())
//                    || CollectionUtils.isNotEmpty(person.getRace())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirth())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirthStats())) {
//                empty = false;
//            }
//
//            if (!empty && save) {
//                this.saveEntity(id, source.getSourceId(), key, person);
//            }

            return cdmDrugSummary;
        }

        public CDMProcedureSummary getProcedureResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
            final String key = PERSON;
            CDMProcedureSummary cdmProcedureSummary = new CDMProcedureSummary();
            boolean empty = true;

            String personSummaryData = this.renderTranslateCohortSql(BASE_SQL_PATH + "report/person/population.sql", null, source);
//            if (personSummaryData != null) {
//                person.setYearOfBirth(jdbcTemplate.query(yobSql, new ConceptDistributionMapper()));
//            }

//            String yobStatSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (yobStatSql != null) {
//                person.setYearOfBirthStats(jdbcTemplate.query(yobStatSql, new CohortStatsMapper()));
//            }
//
//            String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (genderSql != null) {
//                person.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
//            }
//
//            String raceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (raceSql != null) {
//                person.setRace(jdbcTemplate.query(raceSql, new ConceptCountMapper()));
//            }
//
//            String ethnicitySql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (ethnicitySql != null) {
//                person.setEthnicity(jdbcTemplate.query(ethnicitySql, new ConceptCountMapper()));
//            }
//
//            if (CollectionUtils.isNotEmpty(person.getEthnicity())
//                    || CollectionUtils.isNotEmpty(person.getGender())
//                    || CollectionUtils.isNotEmpty(person.getRace())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirth())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirthStats())) {
//                empty = false;
//            }
//
//            if (!empty && save) {
//                this.saveEntity(id, source.getSourceId(), key, person);
//            }

            return cdmProcedureSummary;
        }
    }

}

