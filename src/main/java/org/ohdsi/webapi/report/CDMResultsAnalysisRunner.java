package org.ohdsi.webapi.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ohdsi.webapi.report.mapper.*;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class CDMResultsAnalysisRunner {

    private static final String BASE_SQL_PATH = "/resources/cdmresults/sql";

    private static final Logger log = LoggerFactory.getLogger(CDMResultsAnalysisRunner.class);

    private static final String[] STANDARD_TABLE = new String[]{"results_database_schema", "vocab_database_schema", "cdm_database_schema"};

    private static final String[] DRILLDOWN_COLUMNS = new String[]{"conceptId"};
    private static final String[] DRILLDOWN_TABLE = new String[]{"results_database_schema", "vocab_database_schema"};

    private String sourceDialect;
    private ObjectMapper objectMapper;


    public void init(String sourceDialect, ObjectMapper objectMapper) {

        this.sourceDialect = sourceDialect;
        this.objectMapper = objectMapper;
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

    public CDMObservationPeriod getObservationPeriodResults(JdbcTemplate jdbcTemplate, Source source) {
        CDMObservationPeriod obsPeriod = new CDMObservationPeriod();
        PreparedStatementRenderer ageAtFirstSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/ageatfirst.sql", source);
        if (ageAtFirstSql != null) {
            obsPeriod.setAgeAtFirst(jdbcTemplate.query(ageAtFirstSql.getSql(), ageAtFirstSql.getSetter(),
                    new ConceptDistributionMapper()));
        }

        PreparedStatementRenderer obsLengthSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observationlength_data.sql", source);
        if (obsLengthSql != null) {
            obsPeriod.setObservationLength(jdbcTemplate.query(obsLengthSql.getSql(), obsLengthSql.getSetter(),
                    new ConceptDistributionMapper()));
        }

        PreparedStatementRenderer obsLengthStatsSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observationlength_stats.sql", source);
        if (obsLengthStatsSql != null) {
            obsPeriod.setObservationLengthStats(jdbcTemplate.query(obsLengthStatsSql.getSql(),
                    obsLengthStatsSql.getSetter(), new CohortStatsMapper()));
        }

        PreparedStatementRenderer obsYearStatsSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observedbyyear_stats.sql",
                source);
        if (obsYearStatsSql != null) {
            obsPeriod.setPersonsWithContinuousObservationsByYearStats(jdbcTemplate.query(obsYearStatsSql.getSql(),
                    obsYearStatsSql.getSetter(), new CohortStatsMapper()));
        }

        PreparedStatementRenderer personsWithContObsSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observedbyyear_data.sql", source);
        if (personsWithContObsSql != null) {
            obsPeriod.setPersonsWithContinuousObservationsByYear(jdbcTemplate.query(personsWithContObsSql.getSql(),
                    personsWithContObsSql.getSetter(), new ConceptDistributionMapper()));
        }

        PreparedStatementRenderer ageByGenderSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/agebygender.sql", source);
        if (ageByGenderSql != null) {
            obsPeriod.setAgeByGender(jdbcTemplate.query(ageByGenderSql.getSql(), ageByGenderSql.getSetter(),
                    new ConceptQuartileMapper()));
        }

        PreparedStatementRenderer durationByGenderSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observationlengthbygender.sql", source);
        if (durationByGenderSql != null) {
            obsPeriod.setDurationByGender(jdbcTemplate.query(durationByGenderSql.getSql(), durationByGenderSql.getSetter(),
                    new ConceptQuartileMapper()));
        }

        PreparedStatementRenderer durationByAgeSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observationlengthbyage.sql", source);
        if (durationByAgeSql != null) {
            obsPeriod.setDurationByAgeDecile(jdbcTemplate.query(durationByAgeSql.getSql(), durationByAgeSql.getSetter(),
                    new ConceptQuartileMapper()));
        }

        PreparedStatementRenderer cumulObsSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/cumulativeduration.sql", source);
        if (cumulObsSql != null) {
            obsPeriod.setCumulativeObservation(jdbcTemplate.query(cumulObsSql.getSql(), cumulObsSql.getSetter(),
                    new CumulativeObservationMapper()));
        }

        PreparedStatementRenderer obsByMonthSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/observedbymonth.sql", source);
        if (obsByMonthSql != null) {
            obsPeriod.setObservedByMonth(jdbcTemplate.query(obsByMonthSql.getSql(), obsByMonthSql.getSetter(),
                    new MonthObservationMapper()));
        }

        PreparedStatementRenderer obsPeriodsPerPersonSql = renderTranslateSql(
                BASE_SQL_PATH + "/report/observationperiod/periodsperperson.sql", source);
        if (obsPeriodsPerPersonSql != null) {
            obsPeriod.setObservationPeriodsPerPerson(jdbcTemplate.query(obsPeriodsPerPersonSql.getSql(),
                    obsPeriodsPerPersonSql.getSetter(), new ConceptCountMapper()));
        }
        return obsPeriod;
    }

    private PreparedStatementRenderer renderTranslateSql(String sqlPath, Source source) {
        return renderTranslateSql(sqlPath, null, source);
  }



    public ArrayNode getTreemap(JdbcTemplate jdbcTemplate,
                                String domain,
                                Source source) {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        String sqlPath = BASE_SQL_PATH + "/report/" + domain.toLowerCase() + "/treemap.sql";
        PreparedStatementRenderer sql = this.renderTranslateSql(sqlPath, source);
        if (sql != null) {
            List<JsonNode> list = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new GenericRowMapper(objectMapper));
            arrayNode.addAll(list);
        }
        return arrayNode;
    }

    public JsonNode getDrilldown(JdbcTemplate jdbcTemplate,
                                 String domain,
                                 Integer conceptId,
                                 Source source) {
        ObjectNode objectNode = objectMapper.createObjectNode();

        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        String folder = Objects.nonNull(conceptId) ? "/drilldown/" : "/drilldownsummary/";
        String pattern = BASE_SQL_PATH + "/report/" + domain.toLowerCase() + folder + "*.sql";
        try {
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                String fullSqlPath = resource.getURL().getPath();
                int startIndex = fullSqlPath.indexOf(BASE_SQL_PATH);
                String sqlPath = fullSqlPath.substring(startIndex);
                PreparedStatementRenderer sql = this.renderTranslateSql(sqlPath, conceptId, source);
                if (sql != null) {
                    List<JsonNode> l = jdbcTemplate.query(sql.getSql(), sql.getSetter(), new GenericRowMapper(objectMapper));
                    String analysisName = resource.getFilename().replace(".sql", "");
                    objectNode.putArray(analysisName).addAll(l);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
        return psr;
    }
}
