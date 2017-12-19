package org.ohdsi.webapi.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.report.mapper.*;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class CDMResultsAnalysisRunner {

    private static final String BASE_SQL_PATH = "/resources/cdmresults/sql";

    private static final Log log = LogFactory.getLog(CDMResultsAnalysisRunner.class);

    private static final String[] STANDARD_TABLE = new String[]{"results_database_schema", "vocab_database_schema", "cdm_database_schema"};

    private static final String[] DRILLDOWN_COLUMNS = new String[]{"conceptId"};
    private static final String[] DRILLDOWN_TABLE = new String[]{"results_database_schema", "vocab_database_schema"};

    private ObjectMapper mapper;

    private String sourceDialect;

    public CDMResultsAnalysisRunner(String sourceDialect) {

        this.sourceDialect = sourceDialect;
        mapper = new ObjectMapper();
    }

    public CDMDashboard getDashboard(JdbcTemplate jdbcTemplate,
                                     Source source) {

        CDMDashboard dashboard = new CDMDashboard();

        PreparedStatementRenderer summarySql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/population.sql", source);
        if (summarySql != null) {
            dashboard.setSummary(jdbcTemplate.query(summarySql.getSql(), summarySql.getSetter(), new CDMAttributeMapper()));
        }

        PreparedStatementRenderer ageAtFirstObsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/observationperiod/ageatfirst.sql", source);
        if (ageAtFirstObsSql != null) {
            dashboard.setAgeAtFirstObservation(jdbcTemplate.query(ageAtFirstObsSql.getSql(), ageAtFirstObsSql.getSetter(), new ConceptDistributionMapper()));
        }

        PreparedStatementRenderer genderSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/gender.sql", source);
        if (genderSql != null) {
            dashboard.setGender(jdbcTemplate.query(genderSql.getSql(), genderSql.getSetter(), new ConceptCountMapper()));
        }

        PreparedStatementRenderer cumulObsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/observationperiod/cumulativeduration.sql", source);
        if (cumulObsSql != null) {
            dashboard.setCumulativeObservation(jdbcTemplate.query(cumulObsSql.getSql(), cumulObsSql.getSetter(), new CumulativeObservationMapper()));
        }

        PreparedStatementRenderer obsByMonthSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/observationperiod/observedbymonth.sql", source);
        if (obsByMonthSql != null) {
            dashboard.setObservedByMonth(jdbcTemplate.query(obsByMonthSql.getSql(), obsByMonthSql.getSetter(), new MonthObservationMapper()));
        }

        return dashboard;

    }

    /**
     * Queries for CDM person results for the given source
     *
     * @param jdbcTemplate JDBCTemplate
     * @return CDMPersonSummary
     */
    public CDMPersonSummary getPersonResults(JdbcTemplate jdbcTemplate,
                                             final Source source) {

        CDMPersonSummary person = new CDMPersonSummary();

        PreparedStatementRenderer summarySql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/population.sql", source);
        if (summarySql != null) {
            person.setSummary(jdbcTemplate.query(summarySql.getSql(), summarySql.getSetter(), new CDMAttributeMapper()));
        }

        PreparedStatementRenderer genderSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/gender.sql", source);
        if (genderSql != null) {
            person.setGender(jdbcTemplate.query(genderSql.getSql(), genderSql.getSetter(), new ConceptCountMapper()));
        }

        PreparedStatementRenderer raceSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/race.sql", source);
        if (raceSql != null) {
            person.setRace(jdbcTemplate.query(raceSql.getSql(), raceSql.getSetter(), new ConceptCountMapper()));
        }

        PreparedStatementRenderer ethnicitySql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/ethnicity.sql", source);
        if (ethnicitySql != null) {
            person.setEthnicity(jdbcTemplate.query(ethnicitySql.getSql(), ethnicitySql.getSetter(), new ConceptCountMapper()));
        }

        PreparedStatementRenderer yearOfBirthSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/yearofbirth_data.sql", source);
        if (yearOfBirthSql != null) {
            person.setYearOfBirth(jdbcTemplate.query(yearOfBirthSql.getSql(), yearOfBirthSql.getSetter(), new ConceptDistributionMapper()));
        }

        PreparedStatementRenderer yearOfBirthStatsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/yearofbirth_stats.sql", source);
        if (yearOfBirthStatsSql != null) {
            person.setYearOfBirthStats(jdbcTemplate.query(yearOfBirthStatsSql.getSql(), yearOfBirthStatsSql.getSetter(), new CohortStatsMapper()));
        }

        return person;
    }

    public CDMAchillesHeel getHeelResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMAchillesHeel heel = new CDMAchillesHeel();
        PreparedStatementRenderer achillesSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/achillesheel/sqlAchillesHeel.sql", source);
        if (achillesSql != null) {
            heel.setMessages(jdbcTemplate.query(achillesSql.getSql(), achillesSql.getSetter(), new CDMAttributeMapper()));
        }
        return heel;
    }

    public CDMDataDensity getDataDensityResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMDataDensity cdmDataDensity = new CDMDataDensity();
        PreparedStatementRenderer conceptsPerPersonSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/datadensity/conceptsperperson.sql", source);
        if (conceptsPerPersonSql != null) {
            cdmDataDensity.setConceptsPerPerson(jdbcTemplate.query(conceptsPerPersonSql.getSql(), conceptsPerPersonSql.getSetter(), new ConceptQuartileMapper()));
        }
        PreparedStatementRenderer recordsPerPersonSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/datadensity/recordsperperson.sql", source);
        if (recordsPerPersonSql != null) {
            cdmDataDensity.setRecordsPerPerson(jdbcTemplate.query(recordsPerPersonSql.getSql(), recordsPerPersonSql.getSetter(), new SeriesPerPersonMapper()));
        }
        PreparedStatementRenderer totalRecordsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/datadensity/totalrecords.sql", source);
        if (totalRecordsSql != null) {
            cdmDataDensity.setTotalRecords(jdbcTemplate.query(totalRecordsSql.getSql(), totalRecordsSql.getSetter(), new SeriesPerPersonMapper()));
        }
        return cdmDataDensity;
    }

    public CDMDeath getDeathResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMDeath cdmDeath = new CDMDeath();
        PreparedStatementRenderer prevalenceByGenderAgeYearSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlPrevalenceByGenderAgeYear.sql", source);
        if (prevalenceByGenderAgeYearSql != null) {
            cdmDeath.setPrevalenceByGenderAgeYear(jdbcTemplate.query(prevalenceByGenderAgeYearSql.getSql(), prevalenceByGenderAgeYearSql.getSetter(), new ConceptDecileMapper()));
        }
        PreparedStatementRenderer prevalenceByMonthSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlPrevalenceByMonth.sql", source);
        if (prevalenceByMonthSql != null) {
            cdmDeath.setPrevalenceByMonth(jdbcTemplate.query(prevalenceByMonthSql.getSql(), prevalenceByMonthSql.getSetter(), new PrevalanceMapper()));
        }
        PreparedStatementRenderer deathByTypeSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlDeathByType.sql", source);
        if (deathByTypeSql != null) {
            cdmDeath.setDeathByType(jdbcTemplate.query(deathByTypeSql.getSql(), deathByTypeSql.getSetter(), new ConceptCountMapper()));
        }
        PreparedStatementRenderer ageAtDeathSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlAgeAtDeath.sql", source);
        if (ageAtDeathSql != null) {
            cdmDeath.setAgeAtDeath(jdbcTemplate.query(ageAtDeathSql.getSql(), ageAtDeathSql.getSetter(), new ConceptQuartileMapper()));
        }
        return cdmDeath;
    }

    private PreparedStatementRenderer renderTranslateSql(String sqlPath, Source source) {
        return renderTranslateSql(sqlPath, null, source);
  }



    public ArrayNode getTreemap(JdbcTemplate jdbcTemplate,
                                String domain,
                                Source source) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        String sqlPath = BASE_SQL_PATH + "/report/" + domain.toLowerCase() + "/treemap.sql";
        PreparedStatementRenderer sql = this.renderTranslateSql(sqlPath, source);
        if (sql != null) {
            List<JsonNode> list = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new GenericRowMapper(mapper));
            arrayNode.addAll(list);
        }
        return arrayNode;
    }

    public JsonNode getDrilldown(JdbcTemplate jdbcTemplate,
                                 final String domain,
                                 final int conceptId,
                                 Source source) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();

        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        String pattern = BASE_SQL_PATH + "/report/" + domain.toLowerCase() + "/drilldown/*.sql";
        try {
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                String fullSqlPath = resource.getURL().getPath();
                int startIndex = fullSqlPath.indexOf(BASE_SQL_PATH);
                String sqlPath = fullSqlPath.substring(startIndex);
                PreparedStatementRenderer sql = this.renderTranslateSql(sqlPath, conceptId, source);
                if (sql != null) {
                    List<JsonNode> l = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new GenericRowMapper(mapper));
                    String analysisName = resource.getFilename().replace(".sql", "");
                    objectNode.putArray(analysisName).addAll(l);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return objectNode;
    }

    private PreparedStatementRenderer renderTranslateSql(String sqlPath, Integer conceptId, Source source) {

        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
        String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

        PreparedStatementRenderer psr;
        String[] tableQualifierValues;

        if (conceptId != null) {
            tableQualifierValues = new String[]{resultsTableQualifier, vocabularyTableQualifier};
            psr = new PreparedStatementRenderer(source, sqlPath, DRILLDOWN_TABLE, tableQualifierValues, DRILLDOWN_COLUMNS,  new Integer[]{conceptId});
        } else {
            tableQualifierValues = new String[]{resultsTableQualifier, vocabularyTableQualifier, cdmTableQualifier};
            psr = new PreparedStatementRenderer(source, sqlPath, STANDARD_TABLE, tableQualifierValues, (String) null, null);
        }
        psr.setTargetDialect(source.getSourceDialect());
        return psr;
    }
}
