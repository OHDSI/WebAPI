package org.ohdsi.webapi.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.*;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptMapper;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.report.mapper.*;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class CDMResultsAnalysisRunner {

    public static final String DASHBOARD = "dashboard";
    public static final String PERSON = "person";
    public static final String BASE_SQL_PATH = "/resources/cdmresults/sql";
    public static final String DRUG = "drug";
    public static final String CONDITION = "condition";
    public static final String CONDITIONERA = "conditionera";
    public static final String OBSERVATIONPERIOD = "observationperiod";
    public static final String HEEL = "heel";
    public static final String PROCEDURE = "procedure";
    public static final String DATADENSITY = "datadensity";
    public static final String OBSERVATION = "observation";
    public static final String VISIT = "visit";
    public static final String VISIT_DRILLDOWN = "visit_drilldown";

    private static final Log log = LogFactory.getLog(CDMResultsAnalysisRunner.class);

    public static final String[] STANDARD_COLUMNS = new String[]{"results_database_schema", "vocab_database_schema"};

    public static final String[] DRILLDOWN_COLUMNS = new String[]{"results_database_schema", "vocab_database_schema", "conceptId"};

    private ObjectMapper mapper;
    private String sourceDialect;

//        private AchillesVisualizationDataRepository visualizationDataRepository;

    public CDMResultsAnalysisRunner(String sourceDialect) {

        this.sourceDialect = sourceDialect;
//            this.visualizationDataRepository = visualizationDataRepository;
        mapper = new ObjectMapper();
    }

    public CDMDashboard getDashboard(JdbcTemplate jdbcTemplate,
                                        /*int id,*/ Source source,
                                        /*boolean demographicsOnly,*/
                                     boolean save) {

        final String key = DASHBOARD;
        CDMDashboard dashboard = new CDMDashboard();
        boolean empty = true;

        String ageAtFirstObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql", null, source);
        if (ageAtFirstObsSql != null) {
            dashboard.setAgeAtFirstObservation(jdbcTemplate.query(ageAtFirstObsSql, new ConceptDistributionMapper()));
        }

        String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", null, source);
        if (genderSql != null) {
            dashboard.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
        }

//            if (!demographicsOnly) {
        String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", null, source);
        if (cumulObsSql != null) {
            dashboard.setCumulativeObservation(jdbcTemplate.query(cumulObsSql, new CumulativeObservationMapper()));
        }

        String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", null, source);
        if (obsByMonthSql != null) {
            dashboard.setObservedByMonth(jdbcTemplate.query(obsByMonthSql, new MonthObservationMapper()));
        }
//            }

//            if (CollectionUtils.isNotEmpty(dashboard.getAgeAtFirstObservation())
//                    || CollectionUtils.isNotEmpty(dashboard.getCumulativeObservation())
//                    || CollectionUtils.isNotEmpty(dashboard.getGender())
//                    || CollectionUtils.isNotEmpty(dashboard.getObservedByMonth())) {
//                empty = false;
//            }
//
//            if (!empty && save) {
//                this.saveEntity(id, source.getSourceId(), key, dashboard);
//            }

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
        Integer id = null;

        String personSummaryData = this.renderTranslateCohortSql(BASE_SQL_PATH + "/report/person/population.sql", null, source);
        if (personSummaryData != null) {
            person.setYearOfBirth(jdbcTemplate.query(personSummaryData, new ConceptDistributionMapper()));
        }

//            String yobStatSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
//            if (yobStatSql != null) {
//                person.setYearOfBirthStats(jdbcTemplate.query(yobStatSql, new CohortStatsMapper()));
//            }

        String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, /*minCovariatePersonCountParam, minIntervalPersonCountParam,*/ source);
        if (genderSql != null) {
            person.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
        }

        String raceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, /*minCovariatePersonCountParam, minIntervalPersonCountParam,*/ source);
        if (raceSql != null) {
            person.setRace(jdbcTemplate.query(raceSql, new ConceptCountMapper()));
        }

        String ethnicitySql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, /*minCovariatePersonCountParam, minIntervalPersonCountParam,*/ source);
        if (ethnicitySql != null) {
            person.setEthnicity(jdbcTemplate.query(ethnicitySql, new ConceptCountMapper()));
        }

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
    public String renderTranslateCohortSql(String sqlPath, Integer conceptId, Source source) {
        String sql = null;

        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        try {
            String[] cols;
            String[] colValues;
            if (conceptId != null) {
                cols = DRILLDOWN_COLUMNS;
                colValues = new String[]{vocabularyTableQualifier, resultsTableQualifier, String.valueOf(conceptId)};
            } else {
                cols = STANDARD_COLUMNS;
                colValues = new String[]{vocabularyTableQualifier, resultsTableQualifier};
            }
            sql = ResourceHelper.GetResourceAsString(sqlPath);
            sql = SqlRender.renderSql(sql, cols, colValues);
            sql = SqlTranslate.translateSql(sql, sourceDialect, source.getSourceDialect());
        } catch (Exception e) {
            log.error(String.format("Unable to translate sql for  %s", sql), e);
        }

        return sql;
    }

    /**
     * Passes in common params for cdm results, and performs SQL
     * translate/render
     */
    public String renderTranslateCohortSql(String sqlPath, Source source) {
        return renderTranslateCohortSql(sqlPath, null, source);
    }

    public CDMDrugSummary getDrugResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        final String key = PERSON;
        CDMDrugSummary cdmDrugSummary = new CDMDrugSummary();
        boolean empty = true;

        String personSummaryData = this.renderTranslateCohortSql(BASE_SQL_PATH + "report/person/population.sql", null, source);


//            if (CollectionUtils.isNotEmpty(person.getEthnicity())
//                    || CollectionUtils.isNotEmpty(person.getGender())
//                    || CollectionUtils.isNotEmpty(person.getRace())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirth())
//                    || CollectionUtils.isNotEmpty(person.getYearOfBirthStats())) {
//                empty = false;
//            }

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

    public CDMAchillesHeel getHeelResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        return new CDMAchillesHeel();
    }

    public CDMObservationPeriod getObservationPeriodResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        return new CDMObservationPeriod();
    }

    public CDMDataDensity getDataDensityResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        return new CDMDataDensity();
    }

    public List<CDMCondition> getCondition(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        return new ArrayList<CDMCondition>();
    }

    public List<CDMConditionEra> getConditionEras(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        return new ArrayList<CDMConditionEra>();
    }

    public List<CDMObservation> getObservationResults(JdbcTemplate sourceJdbcTemplate, Source source, boolean b) {
        return new ArrayList<CDMObservation>();
    }

    public String renderTranslateSql(String sqlPath, Integer conceptId, Source source) {
        String sql = null;

        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

        try {
            String[] cols;
            String[] colValues;
            if (conceptId != null) {
                cols = DRILLDOWN_COLUMNS;
                colValues = new String[]{resultsTableQualifier, vocabularyTableQualifier, String.valueOf(conceptId)};
            } else {
                cols = STANDARD_COLUMNS;
                colValues = new String[]{resultsTableQualifier, vocabularyTableQualifier};
            }

            sql = ResourceHelper.GetResourceAsString(sqlPath);
            sql = SqlRender.renderSql(sql, cols, colValues);
            sql = SqlTranslate.translateSql(sql, sourceDialect, source.getSourceDialect());
        } catch (Exception e) {
            log.error(String.format("Unable to translate sql for  %s", sql), e);
        }

        return sql;
    }

    public String renderTranslateSql(String sqlPath, Source source) {
        return renderTranslateSql(sqlPath, null, source);
    }

    public String renderDrillDownSql(String analysisName, String analysisType, int conceptId, Source source) {
        return renderTranslateSql(BASE_SQL_PATH + "/report/" + analysisType + "/" + analysisName + ".sql", conceptId, source);
    }

    public ArrayNode getTreemap(JdbcTemplate jdbcTemplate,
                                String domain,
                                Source source) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        String sqlPath = BASE_SQL_PATH + "/report/" + domain.toLowerCase() + "/treemap.sql";
        String sql = this.renderTranslateSql(sqlPath, source);
        if (sql != null) {
            List<JsonNode> list = jdbcTemplate.query(sql, new GenericRowMapper(mapper));
            arrayNode.addAll(list);
        }
        return arrayNode;
    }

    public org.ohdsi.webapi.cohortresults.CohortVisitsDrilldown getVisitsDrilldown(JdbcTemplate jdbcTemplate,
                                                                                         final int conceptId,
                                                                                         Source source) {

        org.ohdsi.webapi.cohortresults.CohortVisitsDrilldown drilldown = new org.ohdsi.webapi.cohortresults.CohortVisitsDrilldown();

        List<org.ohdsi.webapi.cohortresults.ConceptQuartileRecord> ageAtFirst = null;
        String ageAtFirstSql = this.renderDrillDownSql("sqlAgeAtFirstOccurrence", VISIT, conceptId, source);
        if (ageAtFirstSql != null) {
            ageAtFirst = jdbcTemplate.query(ageAtFirstSql, new org.ohdsi.webapi.cohortresults.mapper.ConceptQuartileMapper());
        }
        drilldown.setAgeAtFirstOccurrence(ageAtFirst);

        List<org.ohdsi.webapi.cohortresults.ConceptQuartileRecord> byType = null;
        String byTypeSql = this.renderDrillDownSql("sqlVisitDurationByType", VISIT, conceptId, source);
        if (byTypeSql != null) {
            byType = jdbcTemplate.query(byTypeSql, new org.ohdsi.webapi.cohortresults.mapper.ConceptQuartileMapper());
        }
        drilldown.setVisitDurationByType(byType);

        List<org.ohdsi.webapi.cohortresults.ConceptDecileRecord> prevalenceByGenderAgeYear = null;
        String prevalenceGenderAgeSql = this.renderDrillDownSql("sqlPrevalenceByGenderAgeYear", VISIT, conceptId, source);
        if (prevalenceGenderAgeSql != null) {
            prevalenceByGenderAgeYear = jdbcTemplate.query(prevalenceGenderAgeSql, new org.ohdsi.webapi.cohortresults.mapper.ConceptDecileMapper());
        }
        drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

        List<org.ohdsi.webapi.cohortresults.PrevalenceRecord> prevalenceByMonth = null;
        String prevalanceMonthSql = this.renderDrillDownSql("sqlPrevalenceByMonth", VISIT, conceptId, source);
        if (prevalanceMonthSql != null) {
            prevalenceByMonth = jdbcTemplate.query(prevalanceMonthSql, new org.ohdsi.webapi.cohortresults.mapper.PrevalanceConceptMapper());
        }
        drilldown.setPrevalenceByMonth(prevalenceByMonth);

        return drilldown;
    }

    public JsonNode getDrilldown(JdbcTemplate jdbcTemplate,
                                 final String domain,
                                 final int conceptId,
                                 Source source) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();

        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        String pattern = BASE_SQL_PATH + "/report/" + domain.toLowerCase() + "/*.sql";
        try {
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                String fullSqlPath = resource.getURL().getPath();
                if (!fullSqlPath.toLowerCase().contains("tree")) {
                    int startIndex = fullSqlPath.indexOf(BASE_SQL_PATH);
                    String sqlPath = fullSqlPath.substring(startIndex);
                    String sql = this.renderTranslateSql(sqlPath, conceptId, source);
                    if (sql != null) {
                        List<JsonNode> l = jdbcTemplate.query(sql, new GenericRowMapper(mapper));
                        String analysisName = resource.getFilename().substring(3).replace(".sql", "");
                        String fieldName = analysisName.substring(0,1).toLowerCase() + analysisName.substring(1);
                        objectNode.putArray(fieldName).addAll(l);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return objectNode;
    }
}
