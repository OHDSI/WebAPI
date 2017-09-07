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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class CDMResultsAnalysisRunner {

    private static final String BASE_SQL_PATH = "/resources/cdmresults/sql";

    private static final Log log = LogFactory.getLog(CDMResultsAnalysisRunner.class);

    private static final String[] STANDARD_COLUMNS = new String[]{"results_database_schema", "vocab_database_schema", "cdm_database_schema"};

    private static final String[] DRILLDOWN_COLUMNS = new String[]{"results_database_schema", "vocab_database_schema", "conceptId"};

    private ObjectMapper mapper;

    private String sourceDialect;

    public CDMResultsAnalysisRunner(String sourceDialect) {

        this.sourceDialect = sourceDialect;
        mapper = new ObjectMapper();
    }

    public CDMDashboard getDashboard(JdbcTemplate jdbcTemplate,
                                     Source source) {

        CDMDashboard dashboard = new CDMDashboard();

        String summarySql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/population.sql", source);
        if (summarySql != null) {
            dashboard.setSummary(jdbcTemplate.query(summarySql, new CDMAttributeMapper()));
        }

        String ageAtFirstObsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/observationperiod/ageatfirst.sql", source);
        if (ageAtFirstObsSql != null) {
            dashboard.setAgeAtFirstObservation(jdbcTemplate.query(ageAtFirstObsSql, new ConceptDistributionMapper()));
        }

        String genderSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/gender.sql", source);
        if (genderSql != null) {
            dashboard.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
        }

        String cumulObsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/observationperiod/cumulativeduration.sql", source);
        if (cumulObsSql != null) {
            dashboard.setCumulativeObservation(jdbcTemplate.query(cumulObsSql, new CumulativeObservationMapper()));
        }

        String obsByMonthSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/observationperiod/observedbymonth.sql", source);
        if (obsByMonthSql != null) {
            dashboard.setObservedByMonth(jdbcTemplate.query(obsByMonthSql, new MonthObservationMapper()));
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

        String summarySql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/population.sql", source);
        if (summarySql != null) {
            person.setSummary(jdbcTemplate.query(summarySql, new CDMAttributeMapper()));
        }

        String genderSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/gender.sql", source);
        if (genderSql != null) {
            person.setGender(jdbcTemplate.query(genderSql, new ConceptCountMapper()));
        }

        String raceSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/race.sql", source);
        if (raceSql != null) {
            person.setRace(jdbcTemplate.query(raceSql, new ConceptCountMapper()));
        }

        String ethnicitySql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/ethnicity.sql", source);
        if (ethnicitySql != null) {
            person.setEthnicity(jdbcTemplate.query(ethnicitySql, new ConceptCountMapper()));
        }

        String yearOfBirthSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/yearofbirth_data.sql", source);
        if (yearOfBirthSql != null) {
            person.setYearOfBirth(jdbcTemplate.query(yearOfBirthSql, new ConceptDistributionMapper()));
        }

        String yearOfBirthStatsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/person/yearofbirth_stats.sql", source);
        if (yearOfBirthStatsSql != null) {
            person.setYearOfBirthStats(jdbcTemplate.query(yearOfBirthStatsSql, new CohortStatsMapper()));
        }

        return person;
    }

    public CDMAchillesHeel getHeelResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMAchillesHeel heel = new CDMAchillesHeel();
        String achillesSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/achillesheel/sqlAchillesHeel.sql", source);
        if (achillesSql != null) {
            heel.setMessages(jdbcTemplate.query(achillesSql, new CDMAttributeMapper()));
        }
        return heel;
    }

    public CDMDataDensity getDataDensityResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMDataDensity cdmDataDensity = new CDMDataDensity();
        String conceptsPerPersonSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/datadensity/conceptsperperson.sql", source);
        if (conceptsPerPersonSql != null) {
            cdmDataDensity.setConceptsPerPerson(jdbcTemplate.query(conceptsPerPersonSql, new ConceptQuartileMapper()));
        }
        String recordsPerPersonSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/datadensity/recordsperperson.sql", source);
        if (recordsPerPersonSql != null) {
            cdmDataDensity.setRecordsPerPerson(jdbcTemplate.query(recordsPerPersonSql, new SeriesPerPersonMapper()));
        }
        String totalRecordsSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/datadensity/totalrecords.sql", source);
        if (totalRecordsSql != null) {
            cdmDataDensity.setTotalRecords(jdbcTemplate.query(totalRecordsSql, new SeriesPerPersonMapper()));
        }
        return cdmDataDensity;
    }

    public CDMDeath getDeathResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMDeath cdmDeath = new CDMDeath();
        String prevalenceByGenderAgeYearSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlPrevalenceByGenderAgeYear.sql", source);
        if (prevalenceByGenderAgeYearSql != null) {
            cdmDeath.setPrevalenceByGenderAgeYear(jdbcTemplate.query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper()));
        }
        String prevalenceByMonthSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlPrevalenceByMonth.sql", source);
        if (prevalenceByMonthSql != null) {
            cdmDeath.setPrevalenceByMonth(jdbcTemplate.query(prevalenceByMonthSql, new PrevalanceMapper()));
        }
        String deathByTypeSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlDeathByType.sql", source);
        if (deathByTypeSql != null) {
            cdmDeath.setDeathByType(jdbcTemplate.query(deathByTypeSql, new ConceptCountMapper()));
        }
        String ageAtDeathSql = this.renderTranslateSql(BASE_SQL_PATH + "/report/death/sqlAgeAtDeath.sql", source);
        if (ageAtDeathSql != null) {
            cdmDeath.setAgeAtDeath(jdbcTemplate.query(ageAtDeathSql, new ConceptQuartileMapper()));
        }
        return cdmDeath;
    }

    private String renderTranslateSql(String sqlPath, Source source) {
        return renderTranslateSql(sqlPath, null, source);
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
                String sql = this.renderTranslateSql(sqlPath, conceptId, source);
                if (sql != null) {
                    List<JsonNode> l = jdbcTemplate.query(sql, new GenericRowMapper(mapper));
                    String analysisName = resource.getFilename().replace(".sql", "");
                    objectNode.putArray(analysisName).addAll(l);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return objectNode;
    }

    private String renderTranslateSql(String sqlPath, Integer conceptId, Source source) {
        String sql = null;

        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
        String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

        try {
            String[] cols;
            String[] colValues;
            if (conceptId != null) {
                cols = DRILLDOWN_COLUMNS;
                colValues = new String[]{resultsTableQualifier, vocabularyTableQualifier, String.valueOf(conceptId)};
            } else {
                cols = STANDARD_COLUMNS;
                colValues = new String[]{resultsTableQualifier, vocabularyTableQualifier, cdmTableQualifier};
            }

            sql = ResourceHelper.GetResourceAsString(sqlPath);
            sql = SqlRender.renderSql(sql, cols, colValues);
            sql = SqlTranslate.translateSql(sql, sourceDialect, source.getSourceDialect());
        } catch (Exception e) {
            log.error(String.format("Unable to translate sql for  %s", sql), e);
        }

        return sql;
    }
}
