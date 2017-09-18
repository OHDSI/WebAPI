package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysis;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.cohortanalysis.CohortSummary;
import org.ohdsi.webapi.cohortresults.CohortAttribute;
import org.ohdsi.webapi.cohortresults.CohortBreakdown;
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
import org.ohdsi.webapi.cohortresults.CohortResultsAnalysisRunner;
import org.ohdsi.webapi.cohortresults.CohortSpecificSummary;
import org.ohdsi.webapi.cohortresults.CohortSpecificTreemap;
import org.ohdsi.webapi.cohortresults.CohortVisitsDrilldown;
import org.ohdsi.webapi.cohortresults.DataCompletenessAttr;
import org.ohdsi.webapi.cohortresults.EntropyAttr;
import org.ohdsi.webapi.cohortresults.HierarchicalConceptRecord;
import org.ohdsi.webapi.cohortresults.ScatterplotRecord;
import org.ohdsi.webapi.cohortresults.VisualizationData;
import org.ohdsi.webapi.cohortresults.VisualizationDataRepository;
import org.ohdsi.webapi.cohortresults.mapper.AnalysisResultsMapper;
import org.ohdsi.webapi.model.results.Analysis;
import org.ohdsi.webapi.model.results.AnalysisResults;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.core.Response;
import org.ohdsi.webapi.cohortresults.ExposureCohortResult;
import org.ohdsi.webapi.cohortresults.ExposureCohortSearch;
import org.ohdsi.webapi.cohortresults.PredictorResult;
import org.ohdsi.webapi.cohortresults.TimeToEventResult;
import org.ohdsi.webapi.person.CohortPerson;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;

/**
 *
 * Services related to cohort level analysis results
 *
 */
@Path("/cohortresults")
@Component
public class CohortResultsService extends AbstractDaoService {

  public static final String MIN_COVARIATE_PERSON_COUNT = "10";
  public static final String MIN_INTERVAL_PERSON_COUNT = "10";

  public static final String BASE_SQL_PATH = "/resources/cohortresults/sql";

  @Autowired
  private VisualizationDataRepository visualizationDataRepository;

  @Autowired
  private CohortDefinitionService cohortDefinitionService;

  private ObjectMapper mapper = new ObjectMapper();
  private CohortResultsAnalysisRunner queryRunner = null;

  @PostConstruct
  public void init() {
    queryRunner = new CohortResultsAnalysisRunner(this.getSourceDialect(), this.visualizationDataRepository);
  }

  /**
   * Queries for cohort analysis results for the given cohort definition id
   *
   * @param id cohort_defintion id
   * @param analysisGroup Name of the analysisGrouping under the
   * /resources/cohortresults/sql/ directory
   * @param analysisName Name of the analysis, currently the same name as the
   * sql file under analysisGroup
   * @param sourceKey the source to retrieve results
   * @return List of key, value pairs
   */
  @GET
  @Path("{sourceKey}/{id}/raw/{analysis_group}/{analysis_name}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Map<String, String>> getCohortResultsRaw(@PathParam("id") final int id, @PathParam("analysis_group") final String analysisGroup,
          @PathParam("analysis_name") final String analysisName,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") String sourceKey) {
    List<Map<String, String>> results = null;

    String sql = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    try {

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
      results = genericResultSetLoader(sql, source);
    }

    return results;
  }

  @GET
  @Path("{sourceKey}/{id}/export.zip")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportCohortResults(@PathParam("id") int id, @PathParam("sourceKey") String sourceKey) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    try {
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
      String sql = null;
      final StringBuilder resultData = new StringBuilder();
      final StringBuilder resultDistributionData = new StringBuilder();

      // results export
      sql = ResourceHelper.GetResourceAsString(BASE_SQL_PATH + "/raw/getAllResults.sql");
      sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "cohortDefinitionId"},
              new String[]{resultsTableQualifier, String.valueOf(id)});
      sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect());

      getSourceJdbcTemplate(source).query(sql, new RowMapper<Void>() {
        @Override
        public Void mapRow(ResultSet rs, int arg1) throws SQLException {
          ResultSetMetaData metaData = rs.getMetaData();
          int colCount = metaData.getColumnCount();
          for (int i = 1; i <= colCount; i++) {
            if (i > 1) {
              resultData.append("\t");
            }
            resultData.append(String.valueOf(rs.getObject(i)));
          }
          resultData.append("\r\n");
          return null;
        }
      });

      ZipEntry resultsEntry = new ZipEntry("cohort_" + String.valueOf(id) + "_results.tsv");

      zos.putNextEntry(resultsEntry);
      zos.write(resultData.toString().getBytes());
      zos.closeEntry();

      // result distribution export
      sql = ResourceHelper.GetResourceAsString(BASE_SQL_PATH + "/raw/getAllResultDistributions.sql");
      sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "cohortDefinitionId"},
              new String[]{resultsTableQualifier, String.valueOf(id)});
      sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect());

      getSourceJdbcTemplate(source).query(sql, new RowMapper<Void>() {
        @Override
        public Void mapRow(ResultSet rs, int arg1) throws SQLException {
          ResultSetMetaData metaData = rs.getMetaData();
          int colCount = metaData.getColumnCount();
          for (int i = 1; i <= colCount; i++) {
            if (i > 1) {
              resultDistributionData.append("\t");
            }
            resultDistributionData.append(String.valueOf(rs.getObject(i)));
          }
          resultDistributionData.append("\r\n");
          return null;
        }
      });

      ZipEntry resultsDistEntry = new ZipEntry("cohort_" + String.valueOf(id) + "_results_dist.tsv");
      zos.putNextEntry(resultsDistEntry);
      zos.write(resultDistributionData.toString().getBytes());
      zos.closeEntry();

      // include cohort definition in export
      CohortDefinitionDTO cohortDefinition = cohortDefinitionService.getCohortDefinition(id);
      ByteArrayOutputStream cohortDefinitionStream = new ByteArrayOutputStream();
      mapper.writeValue(cohortDefinitionStream, cohortDefinition);
      cohortDefinitionStream.flush();

      ZipEntry cohortDefinitionEntry = new ZipEntry("cohort_" + String.valueOf(id) + "_definition.json");
      zos.putNextEntry(cohortDefinitionEntry);
      zos.write(cohortDefinitionStream.toByteArray());
      zos.closeEntry();

      zos.close();
      baos.flush();
      baos.close();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    Response response = Response
            .ok(baos)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .build();

    return response;
  }

  @POST
  @Path("{id}/warmup")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public int warmUpVisualizationData(CohortAnalysisTask task) {
    return this.queryRunner.warmupData(this.getSourceJdbcTemplate(task.getSource()), task);

  }

  @GET
  @Path("{sourceKey}/{id}/completed")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<String> getCompletedVisualiztion(@PathParam("id") final int id,
          @PathParam("sourceKey") final String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<VisualizationData> vizData = this.visualizationDataRepository.findByCohortDefinitionIdAndSourceId(id, source.getSourceId());
    Set<String> completed = new HashSet<String>();
    if (CollectionUtils.isNotEmpty(vizData)) {
      for (VisualizationData viz : vizData) {
        completed.add(viz.getVisualizationKey());
      }
    }
    return completed;
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
  @Path("{sourceKey}/{id}/dashboard")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDashboard getDashboard(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @QueryParam("demographics_only") final boolean demographicsOnly,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    final String key = CohortResultsAnalysisRunner.DASHBOARD;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    CohortDashboard dashboard = null;

    if (refresh || data == null) {
      dashboard = queryRunner.getDashboard(getSourceJdbcTemplate(source), id, source,
              minCovariatePersonCountParam, minIntervalPersonCountParam, demographicsOnly, true);

    } else {
      try {
        dashboard = mapper.readValue(data.getData(), CohortDashboard.class);
      } catch (Exception e) {
        log.error(e);
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
  @Path("{sourceKey}/{id}/condition/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getConditionTreemap(@PathParam("sourceKey") String sourceKey, @PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.CONDITION;
    List<HierarchicalConceptRecord> res = null;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      res = this.queryRunner.getConditionTreemap(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
    }

    return res;
  }

  @GET
  @Path("{sourceKey}/{id}/distinctPersonCount/")
  @Produces(MediaType.APPLICATION_JSON)
  public Integer getRawDistinctPersonCount(@PathParam("sourceKey") String sourceKey,
          @PathParam("id") String id,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    String sql = ResourceHelper.GetResourceAsString(BASE_SQL_PATH + "/raw/getTotalDistinctPeople.sql");
    sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "id"}, new String[]{tableQualifier, id});
    sql = SqlTranslate.translateSql(sql, "sql server", source.getSourceDialect());
    Integer result = getSourceJdbcTemplate(source).queryForObject(sql, Integer.class);

    return result;
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
  @Path("{sourceKey}/{id}/condition/{conditionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortConditionDrilldown getConditionResults(@PathParam("sourceKey") String sourceKey,
          @PathParam("id") final int id,
          @PathParam("conditionId") final int conditionId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortConditionDrilldown drilldown = null;
    final String key = CohortResultsAnalysisRunner.CONDITION_DRILLDOWN;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conditionId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getConditionResults(this.getSourceJdbcTemplate(source), id, conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortConditionDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

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
  @Path("{sourceKey}/{id}/conditionera/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getConditionEraTreemap(@PathParam("sourceKey") final String sourceKey,
          @PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.CONDITION_ERA;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    List<HierarchicalConceptRecord> res = null;

    if (data == null || refresh) {
      res = this.queryRunner.getConditionEraTreemap(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
    }

    return res;
  }

  @GET
  @Path("{sourceKey}/{id}/analyses")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Integer> getCompletedAnalyses(@PathParam("sourceKey") String sourceKey, @PathParam("id") String id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql = ResourceHelper.GetResourceAsString(BASE_SQL_PATH + "/raw/getCompletedAnalyses.sql");
    sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "id"}, new String[]{tableQualifier, id});
    sql = SqlTranslate.translateSql(sql, "sql server", source.getSourceDialect());
    return getSourceJdbcTemplate(source).queryForList(sql, Integer.class);
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
  @Path("{sourceKey}/{id}/conditionera/{conditionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortConditionEraDrilldown getConditionEraDrilldown(@PathParam("id") final int id,
          @PathParam("conditionId") final int conditionId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortConditionEraDrilldown drilldown = null;
    final String key = CohortResultsAnalysisRunner.CONDITION_ERA_DRILLDOWN;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository
            .findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conditionId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getConditionEraDrilldown(this.getSourceJdbcTemplate(source), id, conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortConditionEraDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

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
  @Path("{sourceKey}/{id}/drug/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getDrugTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.DRUG;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    List<HierarchicalConceptRecord> res = null;
    if (refresh || data == null) {
      res = this.queryRunner.getDrugTreemap(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/drug/{drugId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDrugDrilldown getDrugResults(@PathParam("id") final int id, @PathParam("drugId") final int drugId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortDrugDrilldown drilldown = null;
    final String key = CohortResultsAnalysisRunner.DRUG_DRILLDOWN;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, drugId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getDrugResults(this.getSourceJdbcTemplate(source), id, drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortDrugDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/drugera/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getDrugEraTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<HierarchicalConceptRecord> res = null;
    final String key = CohortResultsAnalysisRunner.DRUG_ERA;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      res = this.queryRunner.getDrugEraTreemap(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/drugera/{drugId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDrugEraDrilldown getDrugEraResults(@PathParam("id") final int id, @PathParam("drugId") final int drugId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortDrugEraDrilldown drilldown = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.DRUG_ERA_DRILLDOWN;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, drugId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getDrugEraResults(this.getSourceJdbcTemplate(source), id, drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortDrugEraDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

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
  @Path("{sourceKey}/{id}/person")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortPersonSummary getPersonResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortPersonSummary person = null;
    final String key = CohortResultsAnalysisRunner.PERSON;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      person = this.queryRunner.getPersonResults(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        person = mapper.readValue(data.getData(), CohortPersonSummary.class);
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/cohortspecific")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSpecificSummary getCohortSpecificResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortSpecificSummary summary = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.COHORT_SPECIFIC;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      summary = queryRunner.getCohortSpecificSummary(getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        summary = mapper.readValue(data.getData(), CohortSpecificSummary.class);
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/cohortspecifictreemap")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSpecificTreemap getCohortSpecificTreemapResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    CohortSpecificTreemap summary = null;
    final String key = CohortResultsAnalysisRunner.COHORT_SPECIFIC_TREEMAP;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      summary = queryRunner.getCohortSpecificTreemapResults(getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        summary = mapper.readValue(data.getData(), CohortSpecificTreemap.class);
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/cohortspecificprocedure/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortProcedureDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.COHORT_SPECIFIC_PROCEDURE_DRILLDOWN;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      records = this.queryRunner.getCohortProcedureDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        records = mapper.readValue(data.getData(), new TypeReference<List<ScatterplotRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/cohortspecificdrug/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortDrugDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    List<ScatterplotRecord> records = new ArrayList<ScatterplotRecord>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.COHORT_SPECIFIC_DRUG_DRILLDOWN;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      records = this.queryRunner.getCohortDrugDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        records = mapper.readValue(data.getData(), new TypeReference<List<ScatterplotRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/cohortspecificcondition/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortConditionDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    List<ScatterplotRecord> records = null;

    final String key = CohortResultsAnalysisRunner.COHORT_SPECIFIC_CONDITION_DRILLDOWN;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository
            .findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      records = this.queryRunner.getCohortConditionDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        records = mapper.readValue(data.getData(), new TypeReference<List<ScatterplotRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/observation")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getCohortObservationResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    List<HierarchicalConceptRecord> res = null;

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.OBSERVATION;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      res = this.queryRunner.getCohortObservationResults(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/observation/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortObservationDrilldown getCohortObservationResultsDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortObservationDrilldown drilldown = new CohortObservationDrilldown();
    final String key = CohortResultsAnalysisRunner.OBSERVATION_DRILLDOWN;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getCohortObservationResultsDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortObservationDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

    return drilldown;

  }

  /**
   * Queries for cohort analysis for measurement treemap
   *
   * @param id cohort_defintion id
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/measurement")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getCohortMeasurementResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.MEASUREMENT;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      res = this.queryRunner.getCohortMeasurementResults(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/measurement/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortMeasurementDrilldown getCohortMeasurementResultsDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortMeasurementDrilldown drilldown = new CohortMeasurementDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.MEASUREMENT_DRILLDOWN;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getCohortMeasurementResultsDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortMeasurementDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

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
  @Path("{sourceKey}/{id}/observationperiod")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortObservationPeriod getCohortObservationPeriod(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortObservationPeriod obsPeriod = new CohortObservationPeriod();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.OBSERVATION_PERIOD;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      obsPeriod = this.queryRunner.getCohortObservationPeriod(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        obsPeriod = mapper.readValue(data.getData(), CohortObservationPeriod.class);
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/datadensity")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDataDensity getCohortDataDensity(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    CohortDataDensity data = new CohortDataDensity();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.DATA_DENSITY;
    VisualizationData vizData = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || vizData == null) {
      data = this.queryRunner.getCohortDataDensity(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        data = mapper.readValue(vizData.getData(), CohortDataDensity.class);
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/procedure/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getProcedureTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.PROCEDURE;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      res = this.queryRunner.getProcedureTreemap(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/procedure/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortProceduresDrillDown getCohortProceduresDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortProceduresDrillDown drilldown = new CohortProceduresDrillDown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.PROCEDURE_DRILLDOWN;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getCohortProceduresDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortProceduresDrillDown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

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
  @Path("{sourceKey}/{id}/visit/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getVisitTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    List<HierarchicalConceptRecord> res = null;
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.VISIT;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || data == null) {
      res = queryRunner.getVisitTreemap(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        res = mapper.readValue(data.getData(), new TypeReference<List<HierarchicalConceptRecord>>() {
        });
      } catch (Exception e) {
        log.error(e);
      }
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
  @Path("{sourceKey}/{id}/visit/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortVisitsDrilldown getCohortVisitsDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortVisitsDrilldown drilldown = new CohortVisitsDrilldown();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.VISIT_DRILLDOWN;
    VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      drilldown = this.queryRunner.getCohortVisitsDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        drilldown = mapper.readValue(data.getData(), CohortVisitsDrilldown.class);
      } catch (Exception e) {
        log.error(e);
      }
    }
    return drilldown;
  }

  /**
   * Returns the summary for the cohort
   *
   * @param id - the cohort_defintion id
   * @return Summary data including top summary visualization data this cohort
   *
   */
  @GET
  @Path("{sourceKey}/{id}/summarydata")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSummary getCohortSummaryData(@PathParam("id") final int id,
          @PathParam("sourceKey") String sourceKey) {

    CohortSummary summary = new CohortSummary();

    try {
      // total patients
      Integer persons = this.getRawDistinctPersonCount(sourceKey, String.valueOf(id), false);
      summary.setTotalPatients(String.valueOf(persons));

      // median age
      CohortSpecificSummary cohortSpecific = this.getCohortSpecificResults(id, null, null, sourceKey, false);
      if (cohortSpecific != null && cohortSpecific.getAgeAtIndexDistribution() != null && cohortSpecific.getAgeAtIndexDistribution().size() > 0) {
        summary.setMeanAge(String.valueOf(cohortSpecific.getAgeAtIndexDistribution().get(0).getMedianValue()));
      }

      // TODO mean obs period
      CohortDashboard dashboard = this.getDashboard(id, null, null, true, sourceKey, false);
      if (dashboard != null) {
        summary.setGenderDistribution(dashboard.getGender());
        summary.setAgeDistribution(dashboard.getAgeAtFirstObservation());
      }
    } catch (Exception e) {
      log.error(e);
    }

    return summary;
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
  @Path("{sourceKey}/{id}/death")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDeathData getCohortDeathData(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final String minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final String minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    CohortDeathData data = new CohortDeathData();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.DEATH;
    VisualizationData vizData = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

    if (refresh || vizData == null) {
      data = this.queryRunner.getCohortDeathData(this.getSourceJdbcTemplate(source), id, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        data = mapper.readValue(vizData.getData(), CohortDeathData.class);
      } catch (Exception e) {
        log.error(e);
      }
    }

    return data;
  }

  /**
   * Returns the summary for the cohort
   *
   * @param id - the cohort_defintion id
   * @return Summary which includes analyses with complete time
   */
  @GET
  @Path("{sourceKey}/{id}/summaryanalyses")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSummary getCohortSummaryAnalyses(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {

    CohortSummary summary = new CohortSummary();
    try {
      summary.setAnalyses(getCohortAnalysesForCohortDefinition(id, sourceKey, true));
    } catch (Exception e) {
      log.error("unable to get cohort summary", e);
    }

    return summary;
  }

  /**
   * Returns the person identifiers of all members of a generated cohort
   * definition identifier
   *
   * @param id
   * @param sourceKey
   * @param min
   * @param max
   * @return List of all members of a generated cohort definition identifier
   */
  @GET
  @Path("{sourceKey}/{id}/members/{min}-{max}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CohortPerson> getCohortMembers(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey, @PathParam("min") final int min, @PathParam("max") final int max) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/raw/getMembers.sql");
    sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "cohortDefinitionId","min","max"}, new String[]{
      resultsTableQualifier, String.valueOf(id), String.valueOf(min), String.valueOf(max)});
    sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect(), SessionUtils.sessionId(),
            resultsTableQualifier);

    return getSourceJdbcTemplate(source).query(sql, this.cohortMemberMapper);
  }

  /**
   * Returns breakdown with counts about people in cohort
   *
   * @param id
   * @param sourceKey
   * @return List of all members of a generated cohort definition identifier
   */
  @GET
  @Path("{sourceKey}/{id}/breakdown")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CohortBreakdown> getCohortBreakdown(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/raw/getCohortBreakdown.sql");
    sql = SqlRender.renderSql(sql, new String[]{"tableQualifier","resultsTableQualifier", "cohortDefinitionId"}, new String[]{
      cdmTableQualifier, resultsTableQualifier, String.valueOf(id)});
    sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect(), SessionUtils.sessionId(),
            resultsTableQualifier);

    return getSourceJdbcTemplate(source).query(sql, this.cohortBreakdownMapper);
  }
  /**
   * Returns the person identifiers of all members of a cohort breakdown from above
   *
   * @param id
   * @param sourceKey
   * @param gender
   * @param age
   * @param conditions
   * @param drugs
   * @param rows
   * @return List of all members of a generated cohort definition identifier
   */
  @GET
  @Path("{sourceKey}/{id}/breakdown/{gender}/{age}/{conditions}/{drugs}/{rows}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CohortPerson> getCohortMembers(
                                        @PathParam("id") final int id, 
                                        @PathParam("sourceKey") String sourceKey, 
                                        @PathParam("gender") String gender, 
                                        @PathParam("age") String age, 
                                        @PathParam("conditions") String conditions, 
                                        @PathParam("drugs") String drugs,
                                        @PathParam("rows") final int rows) {
    List<String> params = new ArrayList<>();
    List<String> wherecols = new ArrayList<>();
    int groups = 1;
    if (gender.length() > 0 && !gender.equals("''")) {
        params.add(" gender in (@gender) ");
        wherecols.add("gender");
        groups = groups * gender.split(",").length;
    }
    if (age.length() > 0 && !age.equals("''")) {
        params.add(" age in (@age) ");
        wherecols.add("age");
        groups = groups * age.split(",").length;
    }
    if (conditions.length() > 0 && !conditions.equals("''")) {
        params.add(" conditions in (@conditions) ");
        wherecols.add("conditions");
        groups = groups * conditions.split(",").length;
    }
    if (drugs.length() > 0 && !drugs.equals("''")) {
        params.add(" drugs in (@drugs) ");
        wherecols.add("drugs");
        groups = groups * drugs.split(",").length;
    }
    String clause = " where 1=1\n";
    for (String param: params) {
        clause += (" and " + param + "\n");
    }
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    
    String sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/raw/getCohortBreakdownPeople.sql");
    sql = sql.replace("/*whereclause*/", clause);
    String wherecolsStr = "";
    if (wherecols.isEmpty()) {
        sql = sql.replace("partition by", "");
    } else {
        wherecolsStr += wherecols.get(0);
        for (int i=1; i < wherecols.size(); i++) {
            wherecolsStr += (',' + wherecols.get(i));
        }
    }
    sql = SqlRender.renderSql(sql, 
            new String[]{"tableQualifier", "resultsTableQualifier", "cohortDefinitionId","gender", "age", "conditions", "drugs", "rows","wherecols","groups"}, 
            new String[]{cdmTableQualifier, resultsTableQualifier, String.valueOf(id), 
                String.valueOf(gender), 
                String.valueOf(age), 
                String.valueOf(conditions), 
                String.valueOf(drugs), 
                String.valueOf(rows),
                String.valueOf(wherecolsStr),
                String.valueOf(groups)});
    sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect(), SessionUtils.sessionId(),
            resultsTableQualifier);

    return getSourceJdbcTemplate(source).query(sql, this.cohortMemberMapper);
  }
  
  /**
   * Returns the person identifiers of all members of a generated cohort
   * definition identifier
   *
   * @param id
   * @param sourceKey
   * @return List of all members of a generated cohort definition identifier
   */
  @GET
  @Path("{sourceKey}/{id}/members/count")
  @Produces(MediaType.APPLICATION_JSON)
  public Long getCohortMemberCount(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/raw/getMemberCount.sql");
    sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "cohortDefinitionId"}, new String[]{
      resultsTableQualifier, String.valueOf(id)});
    sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect(), SessionUtils.sessionId(),
            resultsTableQualifier);

    return getSourceJdbcTemplate(source).queryForObject(sql, Long.class);
  }

  /**
   * Returns all cohort analyses in the results/OHDSI schema for the given
   * cohort_definition_id
   *
   * @param sourceKey
   * @return List of all cohort analyses and their statuses for the given
   * cohort_defintion_id
   */
  @GET
  @Path("{sourceKey}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CohortAnalysis> getCohortAnalysesForCohortDefinition(@PathParam("id") final int id,
          @PathParam("sourceKey") String sourceKey,
          @DefaultValue("true") @QueryParam("fullDetail") boolean retrieveFullDetail) {

    String sql = null;
    if (retrieveFullDetail) {
      sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalysesForCohortFull.sql");
    } else {
      sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalysesForCohort.sql");
    }

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    sql = SqlRender.renderSql(sql, new String[]{"ohdsi_database_schema", "cohortDefinitionId"}, new String[]{
      resultsTableQualifier, String.valueOf(id)});
    sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect(), SessionUtils.sessionId(),
            resultsTableQualifier);

    return getSourceJdbcTemplate(source).query(sql, this.cohortAnalysisMapper);
  }

  @POST
  @Path("{sourceKey}/exposurecohortrates")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<ExposureCohortResult> getExposureOutcomeCohortRates(@PathParam("sourceKey") String sourceKey, ExposureCohortSearch search) {
    Source dbsource = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = dbsource.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/cohortSpecific/getExposureOutcomeCohortRates.sql");          
    String exposureCohortList = this.JoinArray(search.exposureCohortList);
    String outcomeCohortList = this.JoinArray(search.outcomeCohortList);

    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"exposure_cohort_definition_id","outcome_cohort_definition_id","ohdsi_database_schema"},
              new String[]{exposureCohortList, outcomeCohortList, resultsTableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dbsource.getSourceDialect());
	  
    final List<ExposureCohortResult> results = new ArrayList<ExposureCohortResult>();
    List<Map<String, Object>> rows = getSourceJdbcTemplate(dbsource).queryForList(sql_statement);
    for (Map rs : rows) {	
        ExposureCohortResult e = new ExposureCohortResult();
        e.exposureCohortDefinitionId = String.valueOf(rs.get("exposure_cohort_definition_id"));
        e.incidenceRate1000py = Float.valueOf(String.valueOf(rs.get("incidence_rate_1000py")));
        e.numPersonsExposed = Long.valueOf(String.valueOf(rs.get("num_persons_exposed")));
        e.numPersonsWithOutcomePostExposure = Long.valueOf(String.valueOf(rs.get("num_persons_w_outcome_post_exposure")));
        e.numPersonsWithOutcomePreExposure = Long.valueOf(String.valueOf(rs.get("num_persons_w_outcome_pre_exposure")));
        e.outcomeCohortDefinitionId = String.valueOf(rs.get("outcome_cohort_definition_id"));
        e.timeAtRisk = Float.valueOf(String.valueOf(rs.get("time_at_risk")));

        results.add(e);
      }
          
    return results;
  }
 
  @POST
  @Path("{sourceKey}/timetoevent")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<TimeToEventResult> getTimeToEventDrilldown(@PathParam("sourceKey") String sourceKey, ExposureCohortSearch search) {
    Source dbsource = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = dbsource.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/cohortSpecific/getTimeToEventDrilldown.sql");          
    String exposureCohortList = this.JoinArray(search.exposureCohortList);
    String outcomeCohortList = this.JoinArray(search.outcomeCohortList);

    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"exposure_cohort_definition_id","outcome_cohort_definition_id","ohdsi_database_schema"},
              new String[]{exposureCohortList, outcomeCohortList, resultsTableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dbsource.getSourceDialect());
	  
    final List<TimeToEventResult> results = new ArrayList<TimeToEventResult>();
    List<Map<String, Object>> rows = getSourceJdbcTemplate(dbsource).queryForList(sql_statement);
    for (Map rs : rows) {	
        TimeToEventResult e = new TimeToEventResult();
        e.countValue = Long.valueOf(String.valueOf(rs.get("count_value")));
        e.duration = Long.valueOf(String.valueOf(rs.get("duration")));
        e.exposureCohortDefinitionId = String.valueOf(rs.get("exposure_cohort_definition_id"));
        e.outcomeCohortDefinitionId = String.valueOf(rs.get("outcome_cohort_definition_id"));
        e.pctPersons = Double.valueOf(String.valueOf(rs.get("pct_persons")));
        e.recordType = String.valueOf(rs.get("record_type"));

        results.add(e);
      }
          
    return results;
  }

  @POST
  @Path("{sourceKey}/predictors")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<PredictorResult> getExposureOutcomeCohortPredictors(@PathParam("sourceKey") String sourceKey, ExposureCohortSearch search) {
    Source dbsource = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = dbsource.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = dbsource.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/cohortSpecific/getExposureOutcomePredictors.sql");          
    String exposureCohortList = this.JoinArray(search.exposureCohortList);
    String outcomeCohortList = this.JoinArray(search.outcomeCohortList);
    String minCellCount = String.valueOf(search.minCellCount);

    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"exposure_cohort_definition_id","outcome_cohort_definition_id","minCellCount","ohdsi_database_schema", "cdm_schema"},
              new String[]{exposureCohortList, outcomeCohortList, minCellCount, resultsTableQualifier, cdmTableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dbsource.getSourceDialect());
	  
    final List<PredictorResult> results = new ArrayList<PredictorResult>();
    List<Map<String, Object>> rows = getSourceJdbcTemplate(dbsource).queryForList(sql_statement);
    for (Map rs : rows) {	
        PredictorResult e = new PredictorResult();
        e.absStdDiff = String.valueOf(rs.get("abs_std_diff"));
        e.conceptId = String.valueOf(rs.get("concept_id"));
        e.conceptName = String.valueOf(rs.get("concept_name"));
        e.conceptWithOutcome = String.valueOf(rs.get("concept_w_outcome"));
        e.domainId = String.valueOf(rs.get("domain_id"));
        e.pctOutcomeWithConcept = String.valueOf(rs.get("pct_outcome_w_concept"));
        e.pctNoOutcomeWithConcept = String.valueOf(rs.get("pct_nooutcome_w_concept"));
        e.exposureCohortDefinitionId = String.valueOf(rs.get("exposure_cohort_definition_id"));
        e.outcomeCohortDefinitionId = String.valueOf(rs.get("outcome_cohort_definition_id"));

        results.add(e);
      }
          
    return results;
  }

  /**
   * Returns heracles heel results (data quality issues) for the given cohort
   * definition id
   *
   * @param id cohort definition id
   * @return List<CohortAttribute>
   */
  @GET
  @Path("{sourceKey}/{id}/heraclesheel")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CohortAttribute> getHeraclesHeel(@PathParam("id") final int id, 
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
      List<CohortAttribute> attrs = new ArrayList<CohortAttribute>();
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      final String key = CohortResultsAnalysisRunner.HERACLES_HEEL;
      VisualizationData data = refresh ? null : this.visualizationDataRepository.findByCohortDefinitionIdAndSourceIdAndVisualizationKey(id, source.getSourceId(), key);

      if (refresh || data == null) {
          attrs = this.queryRunner.getHeraclesHeel(this.getSourceJdbcTemplate(source), id, source, true);
      } else {
          try {
              attrs = mapper.readValue(data.getData(), new TypeReference<List<CohortAttribute>>(){});
          } catch (Exception e) {
              log.error(e);
          }
      }

      return attrs;
  }
  
  public List<AnalysisResults> getCohortAnalysesForDataCompleteness(@PathParam("id") final int id,
          @PathParam("sourceKey") String sourceKey) {

    String sql = null;
    sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/datacompleteness/getCohortDataCompleteness.sql");

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    sql = SqlRender.renderSql(sql, new String[]{"tableQualifier", "cohortDefinitionId"},
            new String[]{resultsTableQualifier, String.valueOf(id)});
    sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect());


    AnalysisResultsMapper arm = new AnalysisResultsMapper();
    
    return getSourceJdbcTemplate(source).query(sql, arm);
  }

  @GET
  @Path("{sourceKey}/{id}/datacompleteness")
  @Produces(MediaType.APPLICATION_JSON)
  public List<DataCompletenessAttr> getDataCompleteness(@PathParam("id") final int id,
          @PathParam("sourceKey") String sourceKey) {
      List<AnalysisResults> arl = this.getCohortAnalysesForDataCompleteness(id, sourceKey);
      
      List<DataCompletenessAttr> dcal = new ArrayList<DataCompletenessAttr>();
      
      Map<Integer, AnalysisResults> resultMap = new HashMap<Integer, AnalysisResults>();

      for(AnalysisResults ar : arl){
          resultMap.put(ar.getAnalysisId(), ar);
      }
      
      DataCompletenessAttr aca = new DataCompletenessAttr();
      aca.setCovariance("0~10");
      aca.setGenderP(Float.parseFloat(resultMap.get(2001).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2011).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2021).getStratum1()));
      dcal.add(aca);
      
      aca = new DataCompletenessAttr();
      aca.setCovariance("10~20");
      aca.setGenderP(Float.parseFloat(resultMap.get(2002).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2012).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2022).getStratum1()));
      dcal.add(aca);
      
      aca = new DataCompletenessAttr();
      aca.setCovariance("20~30");
      aca.setGenderP(Float.parseFloat(resultMap.get(2003).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2013).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2023).getStratum1()));
      dcal.add(aca);
      
      aca = new DataCompletenessAttr();
      aca.setCovariance("30~40");
      aca.setGenderP(Float.parseFloat(resultMap.get(2004).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2014).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2024).getStratum1()));
      dcal.add(aca);

      aca = new DataCompletenessAttr();
      aca.setCovariance("40~50");
      aca.setGenderP(Float.parseFloat(resultMap.get(2005).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2015).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2025).getStratum1()));
      dcal.add(aca);

      aca = new DataCompletenessAttr();
      aca.setCovariance("50~60");
      aca.setGenderP(Float.parseFloat(resultMap.get(2006).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2016).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2026).getStratum1()));
      dcal.add(aca);

      aca = new DataCompletenessAttr();
      aca.setCovariance("60+");
      aca.setGenderP(Float.parseFloat(resultMap.get(2007).getStratum1()));
      aca.setRaceP(Float.parseFloat(resultMap.get(2017).getStratum1()));
      aca.setEthP(Float.parseFloat(resultMap.get(2027).getStratum1()));
      dcal.add(aca);

      return dcal;
  }

    public List<AnalysisResults> getCohortAnalysesEntropy(final int id, String sourceKey) {
        
        String sql = null;
        sql = ResourceHelper
                .GetResourceAsString("/resources/cohortresults/sql/entropy/getEntropy.sql");
        
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        
        sql = SqlRender.renderSql(sql, new String[] { "tableQualifier", "cohortDefinitionId" },
            new String[] { resultsTableQualifier, String.valueOf(id) });
        sql = SqlTranslate.translateSql(sql, getSourceDialect(), source.getSourceDialect());
        
        AnalysisResultsMapper arm = new AnalysisResultsMapper();
        
        return getSourceJdbcTemplate(source).query(sql, arm);
    }

    @GET
    @Path("{sourceKey}/{id}/entropy")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EntropyAttr> getEntropy(@PathParam("id") final int id,
            @PathParam("sourceKey") String sourceKey) {
        List<AnalysisResults> arl = this.getCohortAnalysesEntropy(id, sourceKey);
        
        List<EntropyAttr> el = new ArrayList<EntropyAttr>();
        
        for(AnalysisResults ar : arl){
            EntropyAttr ea = new EntropyAttr();
            ea.setDate(ar.getStratum1());
            ea.setEntropy(Float.parseFloat(ar.getStratum2()));
            el.add(ea);
        }
        
        return el;
    }
    
   private String JoinArray(final String[] array) {
    String result = "";

    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        result += ",";
      }

      result += "'" + array[i] + "'";
    }

    return result;
  }
    
  private final RowMapper<CohortPerson> cohortMemberMapper = new RowMapper<CohortPerson>() {
    @Override
    public CohortPerson mapRow(final ResultSet rs, final int rowNum) throws SQLException {
      CohortPerson person = new CohortPerson();
      person.personId = rs.getLong("subject_id");
      person.startDate = rs.getTimestamp("cohort_start_date");
      person.endDate = rs.getTimestamp("cohort_end_date");
      return person;
    }
  };

  private final RowMapper<CohortBreakdown> cohortBreakdownMapper = new RowMapper<CohortBreakdown>() {
    @Override
    public CohortBreakdown mapRow(final ResultSet rs, final int rowNum) throws SQLException {
      CohortBreakdown group = new CohortBreakdown();
      group.people = rs.getLong("people");
      group.gender = rs.getString("gender");
      group.age = rs.getString("age");
      group.conditions = rs.getLong("conditions");
      group.drugs = rs.getLong("drugs");
      return group;
    }
  };

  private final RowMapper<CohortAnalysis> cohortAnalysisMapper = new RowMapper<CohortAnalysis>() {

    @Override
    public CohortAnalysis mapRow(final ResultSet rs, final int rowNum) throws SQLException {
      final CohortAnalysis cohortAnalysis = new CohortAnalysis();
      mapAnalysis(cohortAnalysis, rs, rowNum);
      cohortAnalysis.setAnalysisComplete(rs.getInt(CohortAnalysis.ANALYSIS_COMPLETE) == 1);
      cohortAnalysis.setCohortDefinitionId(rs.getInt(CohortAnalysis.COHORT_DEFINITION_ID));
      cohortAnalysis.setLastUpdateTime(rs.getTimestamp(CohortAnalysis.LAST_UPDATE_TIME));
      return cohortAnalysis;
    }
  };

  private void mapAnalysis(final Analysis analysis, final ResultSet rs, final int rowNum) throws SQLException {
    analysis.setAnalysisId(rs.getInt(Analysis.ANALYSIS_ID));
    analysis.setAnalysisName(rs.getString(Analysis.ANALYSIS_NAME));
    analysis.setStratum1Name(rs.getString(Analysis.STRATUM_1_NAME));
    analysis.setStratum2Name(rs.getString(Analysis.STRATUM_2_NAME));
    analysis.setStratum3Name(rs.getString(Analysis.STRATUM_3_NAME));
    analysis.setStratum4Name(rs.getString(Analysis.STRATUM_4_NAME));
    analysis.setStratum5Name(rs.getString(Analysis.STRATUM_5_NAME));
    analysis.setAnalysisType(rs.getString(Analysis.ANALYSIS_TYPE));
  }
};
