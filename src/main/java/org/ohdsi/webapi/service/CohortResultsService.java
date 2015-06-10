package org.ohdsi.webapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.CohortAttribute;
import org.ohdsi.webapi.cohortresults.CohortConditionDrilldown;
import org.ohdsi.webapi.cohortresults.CohortConditionEraDrilldown;
import org.ohdsi.webapi.cohortresults.CohortDashboard;
import org.ohdsi.webapi.cohortresults.CohortDataDensity;
import org.ohdsi.webapi.cohortresults.CohortDeathData;
import org.ohdsi.webapi.cohortresults.CohortDrugDrilldown;
import org.ohdsi.webapi.cohortresults.CohortDrugEraDrilldown;
import org.ohdsi.webapi.cohortresults.CohortMeasurementDrilldown;
import org.ohdsi.webapi.cohortresults.CohortObservationDrilldown;
import org.ohdsi.webapi.cohortresults.CohortObservationPeriod;
import org.ohdsi.webapi.cohortresults.CohortPersonSummary;
import org.ohdsi.webapi.cohortresults.CohortProceduresDrillDown;
import org.ohdsi.webapi.cohortresults.CohortSpecificSummary;
import org.ohdsi.webapi.cohortresults.CohortSpecificTreemap;
import org.ohdsi.webapi.cohortresults.CohortVisitsDrilldown;
import org.ohdsi.webapi.cohortresults.ConceptCountRecord;
import org.ohdsi.webapi.cohortresults.ConceptDecileRecord;
import org.ohdsi.webapi.cohortresults.ConceptQuartileRecord;
import org.ohdsi.webapi.cohortresults.HierarchicalConceptRecord;
import org.ohdsi.webapi.cohortresults.PrevalenceRecord;
import org.ohdsi.webapi.cohortresults.ScatterplotRecord;
import org.ohdsi.webapi.cohortresults.mapper.CohortAttributeMapper;
import org.ohdsi.webapi.cohortresults.mapper.CohortStatsMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptConditionCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDecileCountsMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDecileMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptDistributionMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptObservationCountMapper;
import org.ohdsi.webapi.cohortresults.mapper.ConceptQuartileMapper;
import org.ohdsi.webapi.cohortresults.mapper.CumulativeObservationMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptEraMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptMapper;
import org.ohdsi.webapi.cohortresults.mapper.HierarchicalConceptPrevalenceMapper;
import org.ohdsi.webapi.cohortresults.mapper.MonthObservationMapper;
import org.ohdsi.webapi.cohortresults.mapper.ObservationPeriodMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceConceptMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceConceptNameMapper;
import org.ohdsi.webapi.cohortresults.mapper.PrevalanceMapper;
import org.ohdsi.webapi.cohortresults.mapper.ScatterplotMapper;
import org.ohdsi.webapi.cohortresults.mapper.SeriesPerPersonMapper;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.stereotype.Component;

/**
 *
 * Services related to viewing Heracles analyses
 *
 */
@Path("{sourceKey}/cohortresults/")
@Component
public class CohortResultsService extends AbstractDaoService {

  private static final String MIN_COVARIATE_PERSON_COUNT = "500";
  private static final String MIN_INTERVAL_PERSON_COUNT = "1000";

  public static final String BASE_SQL_PATH = "/resources/cohortresults/sql";

  private static final String[] STANDARD_COLUMNS = new String[]{"cdm_database_schema",
    "ohdsi_database_schema", "cohortDefinitionId",
    "minCovariatePersonCount", "minIntervalPersonCount"};
  private static final String[] DRILLDOWN_COLUMNS = new String[]{"cdm_database_schema",
    "ohdsi_database_schema", "cohortDefinitionId",
    "minCovariatePersonCount", "minIntervalPersonCount", "conceptId"};

  /**
   * Queries for cohort analysis results for the given cohort definition id
   *
   * @param id cohort_defintion id
   * @param analysisGroup Name of the analysisGrouping under the
   * /resources/cohortresults/sql/ directory
   * @param analysisName Name of the analysis, currently the same name as the
   * sql file under analysisGroup
   * @return List of key, value pairs
   */
  @GET
  @Path("/{id}/raw/{analysis_group}/{analysis_name}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Map<String, String>> getCohortResultsRaw(@PathParam("id") final int id, @PathParam("analysis_group") final String analysisGroup,
          @PathParam("analysis_name") final String analysisName,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") String sourceKey) {
    List<Map<String, String>> results = null;

    String sql = null;

    try {
      Source source = getSourceRepository().findBySourceKey(sourceKey);      
      String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
      String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
      
      sql = ResourceHelper.GetResourceAsString(BASE_SQL_PATH + "/" + analysisGroup + "/" + analysisName + ".sql");

      sql = SqlRender.renderSql(sql, new String[]{"cdm_database_schema",
        "ohdsi_database_schema", "cohortDefinitionId",
        "minCovariatePersonCount", "minIntervalPersonCount"},
              new String[]{vocabularyTableQualifier,
                resultsTableQualifier, String.valueOf(id),
                minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
                        : minCovariatePersonCountParam,
                minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
                        : minIntervalPersonCountParam});
      sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect());
    } catch (Exception e) {
      log.error(String.format("Unable to translate sql for analysis %s", analysisName), e);
    }

    if (sql != null) {
      results = genericResultSetLoader(sql);
    }

    return results;
  }

  /**
   * Queries for cohort analysis dashboard for the given cohort definition id
   *
   * @param id cohort_defintion id
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @param demographicsOnly only render gender and age
   * @return CohortDashboard
   */
  @GET
  @Path("/{id}/dashboard")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDashboard getDashboard(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @QueryParam("demographics_only") final boolean demographicsOnly,
          @PathParam("sourceKey") final String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    CohortDashboard dashboard = new CohortDashboard();

    String ageAtFirstObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstObsSql != null) {
      dashboard.setAgeAtFirstObservation(this.getSourceJdbcTemplate(source).query(ageAtFirstObsSql, new ConceptDistributionMapper()));
    }

    String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam,
            minIntervalPersonCountParam, source);
    if (genderSql != null) {
      dashboard.setGender(this.getSourceJdbcTemplate(source).query(genderSql, new ConceptCountMapper()));
    }

    if (!demographicsOnly) {
      String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
              minCovariatePersonCountParam, minIntervalPersonCountParam, source);
      if (cumulObsSql != null) {
        dashboard.setCumulativeObservation(this.getSourceJdbcTemplate(source).query(cumulObsSql, new CumulativeObservationMapper()));
      }

      String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
              minCovariatePersonCountParam, minIntervalPersonCountParam, source);
      if (obsByMonthSql != null) {
        dashboard.setObservedByMonth(this.getSourceJdbcTemplate(source).query(obsByMonthSql, new MonthObservationMapper()));
      }
    }

    return dashboard;

  }

  /**
   * Queries for cohort analysis condition treemap results for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/condition/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getConditionTreemap(@PathParam("sourceKey") String sourceKey, @PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<HierarchicalConceptRecord> res = null;
    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/condition/sqlConditionTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis condition drilldown results for the given
   * cohort definition id and condition id
   *
   * @param id cohort_defintion id
   * @param conditionId condition_id (from concept)
   * @return CohortConditionDrilldown
   */
  @GET
  @Path("/{id}/condition/{conditionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortConditionDrilldown getConditionResults(@PathParam("sourceKey") String sourceKey,
          @PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {
    CohortConditionDrilldown drilldown = new CohortConditionDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    List<ConceptQuartileRecord> ageAtFirstDiagnosis = null;
    String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstDiagnosis", "condition", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstSql != null) {
      ageAtFirstDiagnosis = getSourceJdbcTemplate(source).query(ageAtFirstSql, new ConceptQuartileMapper());
    }
    drilldown.setAgeAtFirstDiagnosis(ageAtFirstDiagnosis);

    List<ConceptCountRecord> conditionsByType = null;
    String conditionsSql = this.renderDrillDownCohortSql("sqlConditionsByType", "condition", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (conditionsSql != null) {
      conditionsByType = getSourceJdbcTemplate(source).query(conditionsSql, new ConceptConditionCountMapper());
    }
    drilldown.setConditionsByType(conditionsByType);

    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "condition", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceGenderAgeSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceGenderAgeSql, new ConceptDecileMapper());
    }
    drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "condition", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalanceMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalanceMonthSql, new PrevalanceConceptMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);

    return drilldown;

  }

  /**
   * Queries for cohort analysis condition era treemap results for the given
   * cohort definition id
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/conditionera/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getConditionEraTreemap(@PathParam("sourceKey") final String sourceKey,
          @PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<HierarchicalConceptRecord> res = null;
    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/conditionera/sqlConditionEraTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptEraMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis condition era drilldown results for the given
   * cohort definition id and condition id
   *
   * @param id cohort_defintion id
   * @param conditionId condition_id (from concept)
   * @return CohortConditionEraDrilldown
   */
  @GET
  @Path("/{id}/conditionera/{conditionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortConditionEraDrilldown getConditionEraResults(@PathParam("id") final int id, @PathParam("conditionId") final int conditionId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortConditionEraDrilldown drilldown = new CohortConditionEraDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    // age at first diagnosis
    List<ConceptQuartileRecord> ageAtFirst = null;
    String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstDiagnosis", "conditionera", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstSql != null) {
      ageAtFirst = getSourceJdbcTemplate(source).query(ageAtFirstSql, new ConceptQuartileMapper());
    }
    drilldown.setAgeAtFirstDiagnosis(ageAtFirst);

    // length of era
    List<ConceptQuartileRecord> lengthOfEra = null;
    String lengthOfEraSql = this.renderDrillDownCohortSql("sqlLengthOfEra", "conditionera", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (lengthOfEraSql != null) {
      lengthOfEra = getSourceJdbcTemplate(source).query(lengthOfEraSql, new ConceptQuartileMapper());
    }
    drilldown.setLengthOfEra(lengthOfEra);

    // prevalence by gender age year
    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "conditionera", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceByGenderAgeYearSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper());
    }
    drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

    // prevalence by month
    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "conditionera", id,
            conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceByMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalenceByMonthSql, new PrevalanceConceptMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);

    return drilldown;

  }

  /**
   * Queries for drug analysis condition treemap results for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/drug/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getDrugTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);

    List<HierarchicalConceptRecord> res = null;
    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/drug/sqlDrugTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis drug drilldown results for the given cohort
   * definition id and condition id
   *
   * @param id cohort_defintion id
   * @param drugId drug_id (from concept)
   * @return CohortDrugDrilldown
   */
  @GET
  @Path("/{id}/drug/{drugId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDrugDrilldown getDrugResults(@PathParam("id") final int id, @PathParam("drugId") final int drugId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortDrugDrilldown drilldown = new CohortDrugDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String ageAtFirstExposureSql = this.renderDrillDownCohortSql("sqlAgeAtFirstExposure", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstExposureSql != null) {
      drilldown.setAgeAtFirstExposure(this.getSourceJdbcTemplate(source).query(ageAtFirstExposureSql, new ConceptQuartileMapper()));
    }

    String daysSupplySql = this.renderDrillDownCohortSql("sqlDaysSupplyDistribution", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (daysSupplySql != null) {
      drilldown.setDaysSupplyDistribution(this.getSourceJdbcTemplate(source).query(daysSupplySql, new ConceptQuartileMapper()));
    }

    String drugsByTypeSql = this.renderDrillDownCohortSql("sqlDrugsByType", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (drugsByTypeSql != null) {
      drilldown.setDrugsByType(this.getSourceJdbcTemplate(source).query(drugsByTypeSql, new ConceptCountMapper()));
    }

    String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceByGenderAgeYearSql != null) {
      drilldown.setPrevalenceByGenderAgeYear(this.getSourceJdbcTemplate(source).query(prevalenceByGenderAgeYearSql,
              new ConceptDecileMapper()));
    }

    String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceByMonthSql != null) {
      drilldown.setPrevalenceByMonth(this.getSourceJdbcTemplate(source).query(prevalenceByMonthSql, new PrevalanceConceptMapper()));
    }

    String quantityDistributionSql = this.renderDrillDownCohortSql("sqlQuantityDistribution", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (quantityDistributionSql != null) {
      drilldown.setQuantityDistribution(this.getSourceJdbcTemplate(source).query(quantityDistributionSql, new ConceptQuartileMapper()));
    }

    String refillsDistributionSql = this.renderDrillDownCohortSql("sqlRefillsDistribution", "drug", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (refillsDistributionSql != null) {
      drilldown.setRefillsDistribution(this.getSourceJdbcTemplate(source).query(refillsDistributionSql, new ConceptQuartileMapper()));
    }

    return drilldown;

  }

  /**
   * Queries for cohort analysis drug era treemap results for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/drugera/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getDrugEraTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<HierarchicalConceptRecord> res = null;
    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/drugera/sqlDrugEraTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptEraMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis drug era drilldown results for the given cohort
   * definition id and condition id
   *
   * @param id cohort_defintion id
   * @param drugId drug_id (from concept)
   * @return CohortDrugEraDrilldown
   */
  @GET
  @Path("/{id}/drugera/{drugId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDrugEraDrilldown getDrugEraResults(@PathParam("id") final int id, @PathParam("drugId") final int drugId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortDrugEraDrilldown drilldown = new CohortDrugEraDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    // age at first exposure
    List<ConceptQuartileRecord> ageAtFirstExposure = null;
    String ageAtFirstExposureSql = this.renderDrillDownCohortSql("sqlAgeAtFirstExposure", "drugera", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstExposureSql != null) {
      ageAtFirstExposure = getSourceJdbcTemplate(source).query(ageAtFirstExposureSql, new ConceptQuartileMapper());
    }
    drilldown.setAgeAtFirstExposure(ageAtFirstExposure);

    // length of era
    List<ConceptQuartileRecord> lengthOfEra = null;
    String lengthOfEraSql = this.renderDrillDownCohortSql("sqlLengthOfEra", "drugera", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (lengthOfEraSql != null) {
      lengthOfEra = getSourceJdbcTemplate(source).query(lengthOfEraSql, new ConceptQuartileMapper());
    }
    drilldown.setLengthOfEra(lengthOfEra);

    // prevalence by gender age year
    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceByGenderAgeYearSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "drugera", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceByGenderAgeYearSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceByGenderAgeYearSql, new ConceptDecileMapper());
    }
    drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

    // prevalence by month
    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalenceByMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "drugera", id,
            drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceByMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalenceByMonthSql, new PrevalanceConceptMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);

    return drilldown;

  }

  /**
   * Queries for cohort analysis person results for the given cohort definition
   * id
   *
   * @param id cohort_defintion id
   * @return CohortPersonSummary
   */
  @GET
  @Path("/{id}/person")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortPersonSummary getPersonResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortPersonSummary person = new CohortPersonSummary();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String yobSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_data.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (yobSql != null) {
      person.setYearOfBirth(this.getSourceJdbcTemplate(source).query(yobSql, new ConceptDistributionMapper()));
    }

    String yobStatSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/yearofbirth_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (yobStatSql != null) {
      person.setYearOfBirthStats(this.getSourceJdbcTemplate(source).query(yobStatSql, new CohortStatsMapper()));
    }

    String genderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/gender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (genderSql != null) {
      person.setGender(this.getSourceJdbcTemplate(source).query(genderSql, new ConceptCountMapper()));
    }

    String raceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/race.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (raceSql != null) {
      person.setRace(this.getSourceJdbcTemplate(source).query(raceSql, new ConceptCountMapper()));
    }

    String ethnicitySql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/person/ethnicity.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ethnicitySql != null) {
      person.setEthnicity(this.getSourceJdbcTemplate(source).query(ethnicitySql, new ConceptCountMapper()));
    }

    return person;
  }

  /**
   * Queries for cohort analysis cohort specific results for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @return CohortSpecificSummary
   */
  @GET
  @Path("/{id}/cohortspecific")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSpecificSummary getCohortSpecificResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortSpecificSummary summary = new CohortSpecificSummary();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    // 1805, 1806
    String personsByDurationSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/observationPeriodTimeRelativeToIndex.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (personsByDurationSql != null) {
      summary.setPersonsByDurationFromStartToEnd(this.getSourceJdbcTemplate(source).query(personsByDurationSql, new ObservationPeriodMapper()));
    }

    // 1815
    String monthPrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/prevalenceByMonth.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (monthPrevalenceSql != null) {
      summary.setPrevalenceByMonth(this.getSourceJdbcTemplate(source).query(monthPrevalenceSql, new PrevalanceMapper()));
    }

    // 1814
    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceGenderAgeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/prevalenceByYearGenderSex.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceGenderAgeSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceGenderAgeSql, new ConceptDecileCountsMapper());
    }
    summary.setNumPersonsByCohortStartByGenderByAge(prevalenceByGenderAgeYear);

    // 1801
    List<ConceptQuartileRecord> ageAtIndex = null;
    String ageAtIndexSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/ageAtIndexDistribution.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtIndexSql != null) {
      ageAtIndex = getSourceJdbcTemplate(source).query(ageAtIndexSql, new ConceptQuartileMapper());
    }
    summary.setAgeAtIndexDistribution(ageAtIndex);

    // 1803
    List<ConceptQuartileRecord> distributionAgeCohortStartByCohortStartYear = null;
    String distributionAgeCohortStartByCohortStartYearSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/distributionOfAgeAtCohortStartByCohortStartYear.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtIndexSql != null) {
      distributionAgeCohortStartByCohortStartYear = getSourceJdbcTemplate(source).query(distributionAgeCohortStartByCohortStartYearSql, new ConceptQuartileMapper());
    }
    summary.setDistributionAgeCohortStartByCohortStartYear(distributionAgeCohortStartByCohortStartYear);

    // 1802
    List<ConceptQuartileRecord> distributionAgeCohortStartByGender = null;
    String distributionAgeCohortStartByGenderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/distributionOfAgeAtCohortStartByGender.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtIndexSql != null) {
      distributionAgeCohortStartByGender = getSourceJdbcTemplate(source).query(distributionAgeCohortStartByGenderSql, new ConceptQuartileMapper());
    }
    summary.setDistributionAgeCohortStartByGender(distributionAgeCohortStartByGender);

    // 1804
    String personsInCohortFromCohortStartToEndSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/personsInCohortFromCohortStartToEnd.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (personsInCohortFromCohortStartToEndSql != null) {
      summary.setPersonsInCohortFromCohortStartToEnd(this.getSourceJdbcTemplate(source).query(personsInCohortFromCohortStartToEndSql, new MonthObservationMapper()));
    }

    return summary;
  }

  /**
   * Queries for cohort analysis cohort specific treemap results for the given
   * cohort definition id
   *
   * @param id cohort_definition id
   * @return CohortSpecificSummary
   */
  @GET
  @Path("/{id}/cohortspecifictreemap")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSpecificTreemap getCohortSpecificTreemapResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortSpecificTreemap summary = new CohortSpecificTreemap();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    // 1820
    String conditionOccurrencePrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/conditionOccurrencePrevalenceOfCondition.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (conditionOccurrencePrevalenceSql != null) {
      summary.setConditionOccurrencePrevalence(this.getSourceJdbcTemplate(source).query(conditionOccurrencePrevalenceSql, new HierarchicalConceptPrevalenceMapper()));
    }

    // 1830
    String procedureOccurrencePrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/procedureOccurrencePrevalenceOfDrug.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (procedureOccurrencePrevalenceSql != null) {
      summary.setProcedureOccurrencePrevalence(this.getSourceJdbcTemplate(source).query(procedureOccurrencePrevalenceSql, new HierarchicalConceptPrevalenceMapper()));
    }

    // 1870
    String drugEraPrevalenceSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/cohortSpecific/drugEraPrevalenceOfDrug.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (drugEraPrevalenceSql != null) {
      summary.setDrugEraPrevalence(this.getSourceJdbcTemplate(source).query(drugEraPrevalenceSql, new HierarchicalConceptPrevalenceMapper()));
    }

    return summary;
  }

  /**
   * Queries for cohort analysis procedure drilldown results for the given
   * cohort definition id and concept id
   *
   * @param id cohort_definition id
   * @param conceptId conceptId (from concept)
   * @return List<ScatterplotRecord>
   */
  @GET
  @Path("/{id}/cohortspecificprocedure/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortProcedureDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    final String sql = this.renderDrillDownCohortSql("procedureOccursRelativeToIndex", "cohortSpecific", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      records = this.getSourceJdbcTemplate(source).query(sql, new ScatterplotMapper());
    }

    return records;
  }

  /**
   * Queries for cohort analysis drug drilldown results for the given cohort
   * definition id and concept id
   *
   * @param id cohort_definition id
   * @param conceptId conceptId (from concept)
   * @return List<ScatterplotRecord>
   */
  @GET
  @Path("/{id}/cohortspecificdrug/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortDrugDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String sql = this.renderDrillDownCohortSql("drugOccursRelativeToIndex", "cohortSpecific", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      records = this.getSourceJdbcTemplate(source).query(sql, new ScatterplotMapper());
    }
    return records;
  }

  /**
   * Queries for cohort analysis condition drilldown results for the given
   * cohort definition id and concept id
   *
   * @param id cohort_defintion id
   * @param conceptId conceptId (from concept)
   * @return List<ScatterplotRecord>
   */
  @GET
  @Path("/{id}/cohortspecificcondition/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortConditionDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String sql = this.renderDrillDownCohortSql("firstConditionRelativeToIndex", "cohortSpecific", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      records = this.getSourceJdbcTemplate(source).query(sql, new ScatterplotMapper());
    }

    return records;
  }

  /**
   * Queries for cohort analysis for observation treemap
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/observation")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getCohortObservationResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observation/sqlObservationTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis observation drilldown results for the given
   * cohort definition id and condition id
   *
   * @param id cohort_defintion id
   * @param conceptId conceptId (from concept)
   * @return CohortObservationDrilldown
   */
  @GET
  @Path("/{id}/observation/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortObservationDrilldown getCohortObservationResultsDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortObservationDrilldown drilldown = new CohortObservationDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String ageAtFirstOccurrenceSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", "observation", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstOccurrenceSql != null) {
      drilldown.setAgeAtFirstOccurrence(this.getSourceJdbcTemplate(source).query(ageAtFirstOccurrenceSql, new ConceptQuartileMapper()));
    }

    String sqlObservationValueDistribution = this.renderDrillDownCohortSql("sqlObservationValueDistribution", "observation", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlObservationValueDistribution != null) {
      drilldown.setObservationValueDistribution(this.getSourceJdbcTemplate(source).query(sqlObservationValueDistribution, new ConceptQuartileMapper()));
    }

    String sqlObservationsByType = this.renderDrillDownCohortSql("sqlObservationsByType", "observation", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlObservationsByType != null) {
      drilldown.setObservationsByType(this.getSourceJdbcTemplate(source).query(sqlObservationsByType, new ConceptObservationCountMapper()));
    }

    String sqlRecordsByUnit = this.renderDrillDownCohortSql("sqlRecordsByUnit", "observation", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlRecordsByUnit != null) {
      drilldown.setRecordsByUnit(this.getSourceJdbcTemplate(source).query(sqlRecordsByUnit, new ConceptObservationCountMapper()));
    }

    String sqlPrevalenceByGenderAgeYear = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "observation", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlPrevalenceByGenderAgeYear != null) {
      drilldown.setPrevalenceByGenderAgeYear(this.getSourceJdbcTemplate(source).query(sqlPrevalenceByGenderAgeYear, new ConceptDecileMapper()));
    }

    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "observation", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalanceMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalanceMonthSql, new PrevalanceConceptNameMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);

    return drilldown;

  }

  /**
   * Queries for cohort analysis for measurement treemap
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/measurement")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getCohortMeasurementResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/measurement/sqlMeasurementTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis measurement drilldown results for the given
   * cohort definition id and condition id
   *
   * @param id cohort_defintion id
   * @param conceptId conceptId (from concept)
   * @return CohortMeasurementDrilldown
   */
  @GET
  @Path("/{id}/measurement/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortMeasurementDrilldown getCohortMeasurementResultsDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortMeasurementDrilldown drilldown = new CohortMeasurementDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String ageAtFirstOccurrenceSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstOccurrenceSql != null) {
      drilldown.setAgeAtFirstOccurrence(this.getSourceJdbcTemplate(source).query(ageAtFirstOccurrenceSql, new ConceptQuartileMapper()));
    }

    String sqlLowerLimitDistribution = this.renderDrillDownCohortSql("sqlLowerLimitDistribution", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlLowerLimitDistribution != null) {
      drilldown.setLowerLimitDistribution(this.getSourceJdbcTemplate(source).query(sqlLowerLimitDistribution, new ConceptQuartileMapper()));
    }

    String sqlMeasurementValueDistribution = this.renderDrillDownCohortSql("sqlMeasurementValueDistribution", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlMeasurementValueDistribution != null) {
      drilldown.setMeasurementValueDistribution(this.getSourceJdbcTemplate(source).query(sqlMeasurementValueDistribution, new ConceptQuartileMapper()));
    }

    String sqlUpperLimitDistribution = this.renderDrillDownCohortSql("sqlUpperLimitDistribution", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlUpperLimitDistribution != null) {
      drilldown.setUpperLimitDistribution(this.getSourceJdbcTemplate(source).query(sqlUpperLimitDistribution, new ConceptQuartileMapper()));
    }

    String sqlMeasurementsByType = this.renderDrillDownCohortSql("sqlMeasurementsByType", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlMeasurementsByType != null) {
      drilldown.setMeasurementsByType(this.getSourceJdbcTemplate(source).query(sqlMeasurementsByType, new ConceptObservationCountMapper()));
    }

    String sqlRecordsByUnit = this.renderDrillDownCohortSql("sqlRecordsByUnit", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlRecordsByUnit != null) {
      drilldown.setRecordsByUnit(this.getSourceJdbcTemplate(source).query(sqlRecordsByUnit, new ConceptObservationCountMapper()));
    }

    String sqlValuesRelativeToNorm = this.renderDrillDownCohortSql("sqlValuesRelativeToNorm", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlValuesRelativeToNorm != null) {
      drilldown.setValuesRelativeToNorm(this.getSourceJdbcTemplate(source).query(sqlValuesRelativeToNorm, new ConceptObservationCountMapper()));
    }

    String sqlPrevalenceByGenderAgeYear = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sqlPrevalenceByGenderAgeYear != null) {
      drilldown.setPrevalenceByGenderAgeYear(this.getSourceJdbcTemplate(source).query(sqlPrevalenceByGenderAgeYear, new ConceptDecileMapper()));
    }

    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "measurement", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalanceMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalanceMonthSql, new PrevalanceConceptNameMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);

    return drilldown;
  }

  /**
   * Queries for cohort analysis observation period for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @return CohortObservationPeriod
   */
  @GET
  @Path("/{id}/observationperiod")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortObservationPeriod getCohortObservationPeriod(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortObservationPeriod obsPeriod = new CohortObservationPeriod();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String ageAtFirstSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/ageatfirst.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstSql != null) {
      obsPeriod.setAgeAtFirst(getSourceJdbcTemplate(source).query(ageAtFirstSql, new ConceptDistributionMapper()));
    }

    String obsLengthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlength_data.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (obsLengthSql != null) {
      obsPeriod.setObservationLength(getSourceJdbcTemplate(source).query(obsLengthSql, new ConceptDistributionMapper()));
    }

    String obsLengthStatsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlength_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (obsLengthStatsSql != null) {
      obsPeriod.setObservationLengthStats(this.getSourceJdbcTemplate(source).query(obsLengthStatsSql, new CohortStatsMapper()));
    }

    String obsYearStatsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbyyear_stats.sql", id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (obsYearStatsSql != null) {
      obsPeriod.setPersonsWithContinuousObservationsByYearStats(this.getSourceJdbcTemplate(source).query(obsYearStatsSql, new CohortStatsMapper()));
    }

    String personsWithContObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbyyear_data.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (personsWithContObsSql != null) {
      obsPeriod.setPersonsWithContinuousObservationsByYear(getSourceJdbcTemplate(source).query(personsWithContObsSql, new ConceptDistributionMapper()));
    }

    String ageByGenderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/agebygender.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageByGenderSql != null) {
      obsPeriod.setAgeByGender(getSourceJdbcTemplate(source).query(ageByGenderSql, new ConceptQuartileMapper()));
    }

    String durationByGenderSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlengthbygender.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (durationByGenderSql != null) {
      obsPeriod.setDurationByGender(getSourceJdbcTemplate(source).query(durationByGenderSql, new ConceptQuartileMapper()));
    }

    String durationByAgeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observationlengthbyage.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (durationByAgeSql != null) {
      obsPeriod.setDurationByAgeDecile(getSourceJdbcTemplate(source).query(durationByAgeSql, new ConceptQuartileMapper()));
    }

    String cumulObsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/cumulativeduration.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (cumulObsSql != null) {
      obsPeriod.setCumulativeObservation(this.getSourceJdbcTemplate(source).query(cumulObsSql, new CumulativeObservationMapper()));
    }

    String obsByMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/observedbymonth.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (obsByMonthSql != null) {
      obsPeriod.setObservedByMonth(this.getSourceJdbcTemplate(source).query(obsByMonthSql, new MonthObservationMapper()));
    }

    String obsPeriodsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/observationperiod/periodsperperson.sql", id, minCovariatePersonCountParam,
            minIntervalPersonCountParam, source);
    if (obsPeriodsPerPersonSql != null) {
      obsPeriod.setObservationPeriodsPerPerson(this.getSourceJdbcTemplate(source).query(obsPeriodsPerPersonSql, new ConceptCountMapper()));
    }

    return obsPeriod;
  }

  /**
   * Queries for cohort analysis data density for the given cohort definition id
   *
   * @param id cohort_defintion id
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @return CohortDataDensity
   */
  @GET
  @Path("/{id}/datadensity")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDataDensity getCohortDataDensity(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    CohortDataDensity data = new CohortDataDensity();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String recordsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/recordsperperson.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (recordsPerPersonSql != null) {
      data.setRecordsPerPerson(this.getSourceJdbcTemplate(source).query(recordsPerPersonSql, new SeriesPerPersonMapper()));
    }
    String totalRecordsSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/totalrecords.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (totalRecordsSql != null) {
      data.setTotalRecords(this.getSourceJdbcTemplate(source).query(totalRecordsSql, new SeriesPerPersonMapper()));
    }
    String conceptsPerPersonSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/datadensity/conceptsperperson.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (conceptsPerPersonSql != null) {
      data.setConceptsPerPerson(this.getSourceJdbcTemplate(source).query(conceptsPerPersonSql, new ConceptQuartileMapper()));
    }
    return data;
  }

  /**
   * Queries for cohort analysis procedure treemap results for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/procedure/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getProcedureTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/procedure/sqlProcedureTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis procedures for the given cohort definition id
   * and concept id
   *
   * @param id cohort_defintion id
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @return CohortProceduresDrillDown
   */
  @GET
  @Path("/{id}/procedure/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortProceduresDrillDown getCohortProceduresDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortProceduresDrillDown drilldown = new CohortProceduresDrillDown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    List<ConceptQuartileRecord> ageAtFirst = null;
    String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", "procedure", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstSql != null) {
      ageAtFirst = getSourceJdbcTemplate(source).query(ageAtFirstSql, new ConceptQuartileMapper());
    }
    drilldown.setAgeAtFirstOccurrence(ageAtFirst);

    List<ConceptCountRecord> byType = null;
    String byTypeSql = this.renderDrillDownCohortSql("sqlProceduresByType", "procedure", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (byTypeSql != null) {
      byType = getSourceJdbcTemplate(source).query(byTypeSql, new ConceptCountMapper());
    }
    drilldown.setProceduresByType(byType);

    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "procedure", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceGenderAgeSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceGenderAgeSql, new ConceptDecileMapper());
    }
    drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "procedure", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalanceMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalanceMonthSql, new PrevalanceConceptMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);

    return drilldown;
  }

  /**
   * Queries for cohort analysis visit treemap results for the given cohort
   * definition id
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("/{id}/visit/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getVisitTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {

    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/visit/sqlVisitTreemap.sql",
            id, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (sql != null) {
      res = getSourceJdbcTemplate(source).query(sql, new HierarchicalConceptMapper());
    }

    return res;
  }

  /**
   * Queries for cohort analysis visits for the given cohort definition id and
   * concept id
   *
   * @param id cohort_defintion id
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @return CohortVisitsDrilldown
   */
  @GET
  @Path("/{id}/visit/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortVisitsDrilldown getCohortVisitsDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortVisitsDrilldown drilldown = new CohortVisitsDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<ConceptQuartileRecord> ageAtFirst = null;
    String ageAtFirstSql = this.renderDrillDownCohortSql("sqlAgeAtFirstOccurrence", "visit", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageAtFirstSql != null) {
      ageAtFirst = getSourceJdbcTemplate(source).query(ageAtFirstSql, new ConceptQuartileMapper());
    }
    drilldown.setAgeAtFirstOccurrence(ageAtFirst);

    List<ConceptQuartileRecord> byType = null;
    String byTypeSql = this.renderDrillDownCohortSql("sqlVisitDurationByType", "visit", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (byTypeSql != null) {
      byType = getSourceJdbcTemplate(source).query(byTypeSql, new ConceptQuartileMapper());
    }
    drilldown.setVisitDurationByType(byType);

    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceGenderAgeSql = this.renderDrillDownCohortSql("sqlPrevalenceByGenderAgeYear", "visit", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceGenderAgeSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceGenderAgeSql, new ConceptDecileMapper());
    }
    drilldown.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalanceMonthSql = this.renderDrillDownCohortSql("sqlPrevalenceByMonth", "visit", id,
            conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalanceMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalanceMonthSql, new PrevalanceConceptMapper());
    }
    drilldown.setPrevalenceByMonth(prevalenceByMonth);
    return drilldown;
  }

  /**
   * Queries for cohort analysis death data for the given cohort definition id
   *
   * @param id cohort_defintion id
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @return CohortDeathData
   */
  @GET
  @Path("/{id}/death")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDeathData getCohortDeathData(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey) {
    CohortDeathData data = new CohortDeathData();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    List<ConceptQuartileRecord> age = null;
    String ageSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlAgeAtDeath.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (ageSql != null) {
      age = getSourceJdbcTemplate(source).query(ageSql, new ConceptQuartileMapper());
    }
    data.setAgetAtDeath(age);

    List<ConceptCountRecord> byType = null;
    String byTypeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlDeathByType.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (byTypeSql != null) {
      byType = getSourceJdbcTemplate(source).query(byTypeSql, new ConceptCountMapper());
    }
    data.setDeathByType(byType);

    List<ConceptDecileRecord> prevalenceByGenderAgeYear = null;
    String prevalenceGenderAgeSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlPrevalenceByGenderAgeYear.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalenceGenderAgeSql != null) {
      prevalenceByGenderAgeYear = getSourceJdbcTemplate(source).query(prevalenceGenderAgeSql, new ConceptDecileCountsMapper());
    }
    data.setPrevalenceByGenderAgeYear(prevalenceByGenderAgeYear);

    List<PrevalenceRecord> prevalenceByMonth = null;
    String prevalanceMonthSql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/death/sqlPrevalenceByMonth.sql", id,
            minCovariatePersonCountParam, minIntervalPersonCountParam, source);
    if (prevalanceMonthSql != null) {
      prevalenceByMonth = getSourceJdbcTemplate(source).query(prevalanceMonthSql, new PrevalanceConceptMapper());
    }
    data.setPrevalenceByMonth(prevalenceByMonth);

    return data;
  }

  /**
   * Returns heracles heel results (data quality issues) for the given cohort
   * definition id
   *
   * @param id cohort definition id
   * @return List<CohortAttribute>
   */
  @GET
  @Path("/{id}/heraclesheel")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CohortAttribute> getHeraclesHeel(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {
    List<CohortAttribute> attrs = new ArrayList<CohortAttribute>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    String sql = this.renderTranslateCohortSql(BASE_SQL_PATH + "/heraclesHeel/sqlHeraclesHeel.sql", id, null, null, source);
    if (sql != null) {
      attrs = this.getSourceJdbcTemplate(source).query(sql, new CohortAttributeMapper());
    }

    return attrs;
  }

  // HELPER methods	
  /**
   * Renders and Translates drilldown SQL by concept
   *
   * @param analysisName
   * @param analysisType
   * @param id
   * @param conceptId
   * @param minCovariatePersonCountParam
   * @param minIntervalPersonCountParam
   * @return
   */
  private String renderDrillDownCohortSql(String analysisName, String analysisType, int id, int conceptId,
          final String minCovariatePersonCountParam, final String minIntervalPersonCountParam, Source source) {
    return renderTranslateCohortSql(BASE_SQL_PATH + "/" + analysisType + "/byConcept/" + analysisName + ".sql",
            id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
  }

  /**
   * Passes in common params for cohort results, and performs SQL
   * translate/render
   */
  private String renderTranslateCohortSql(String sqlPath, Integer id,
          final String minCovariatePersonCountParam, final String minIntervalPersonCountParam, Source source) {
    return renderTranslateCohortSql(sqlPath, id, null, minCovariatePersonCountParam, minIntervalPersonCountParam, source);
  }

  /**
   * Passes in common params for cohort results, and performs SQL
   * translate/render
   */
  private String renderTranslateCohortSql(String sqlPath, Integer id, Integer conceptId,
          final String minCovariatePersonCountParam, final String minIntervalPersonCountParam,
          Source source) {
    String sql = null;
    
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    
    try {
      String[] cols;
      String[] colValues;
      if (conceptId != null) {
        cols = DRILLDOWN_COLUMNS;
        colValues = new String[]{vocabularyTableQualifier,
          resultsTableQualifier, String.valueOf(id),
          minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
          : minCovariatePersonCountParam,
          minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
          : minIntervalPersonCountParam,
          String.valueOf(conceptId)};
      } else {
        cols = STANDARD_COLUMNS;
        colValues = new String[]{vocabularyTableQualifier,
          resultsTableQualifier, String.valueOf(id),
          minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT
          : minCovariatePersonCountParam,
          minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT
          : minIntervalPersonCountParam};
      }

      sql = ResourceHelper.GetResourceAsString(sqlPath);
      sql = SqlRender.renderSql(sql, cols, colValues);
      sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect());
    } catch (Exception e) {
      log.error(String.format("Unable to translate sql for  %s", sql), e);
    }

    return sql;
  }
};
