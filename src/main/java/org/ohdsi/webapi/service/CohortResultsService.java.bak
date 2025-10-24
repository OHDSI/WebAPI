package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysis;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.cohortanalysis.CohortSummary;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortresults.*;
import org.ohdsi.webapi.cohortresults.mapper.AnalysisResultsMapper;
import org.ohdsi.webapi.model.results.Analysis;
import org.ohdsi.webapi.model.results.AnalysisResults;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSetMetaData;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.core.Response;

import org.ohdsi.webapi.person.CohortPerson;

/**
 * REST Services related to retrieving
 * cohort analysis (a.k.a Heracles Results) analyses results. 
 * More information on the Heracles project
 * can be found at {@link https://www.ohdsi.org/web/wiki/doku.php?id=documentation:software:heracles}.
 * The implementation found in WebAPI represents a migration of the functionality
 * from the stand-alone HERACLES application to integrate it into WebAPI and
 * ATLAS.
 * 
 * @summary Cohort Analysis Results (a.k.a Heracles Results)
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

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;

  @Autowired
  private ObjectMapper mapper;

  private CohortResultsAnalysisRunner queryRunner = null;

  @PostConstruct
  public void init() {
    queryRunner = new CohortResultsAnalysisRunner(this.getSourceDialect(), this.visualizationDataRepository, mapper);
  }

  /**
   * Queries for cohort analysis results for the given cohort definition id
   *
   * @summary Get results for analysis group
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
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
          @PathParam("sourceKey") String sourceKey) {
    List<Map<String, String>> results;
    String sqlPath = BASE_SQL_PATH + "/" + analysisGroup + "/" + analysisName + ".sql";

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    try {
      PreparedStatementRenderer psr = prepareGetCohortResultsRaw(id, minCovariatePersonCountParam,
        minIntervalPersonCountParam, sqlPath, source);
      return genericResultSetLoader(psr, source);
    } catch (Exception e) {
      log.error("Unable to translate sql for analysis {}", analysisName, e);
      return null;
    }
  }

  protected PreparedStatementRenderer prepareGetCohortResultsRaw(final int id,
                                                                 final Integer minCovariatePersonCountParam,
                                                                 final Integer minIntervalPersonCountParam, String sqlPath,
                                                                 Source source) {

    String resourcePath = sqlPath;
    String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);


    String[] searchStringNames = new String[]{"cdm_database_schema", "ohdsi_database_schema"};
    String[] replacementNames = new String[]{vocabularyTableQualifier, resultsTableQualifier};


    String[] variableNames = new String[]{"cohortDefinitionId", "minCovariatePersonCount", "minIntervalPersonCount"};
    Object[] variableValues = new Object[]
        {id, (minCovariatePersonCountParam == null ? MIN_COVARIATE_PERSON_COUNT : minCovariatePersonCountParam),
            (minIntervalPersonCountParam == null ? MIN_INTERVAL_PERSON_COUNT : minIntervalPersonCountParam)};

    return new PreparedStatementRenderer(source, resourcePath, searchStringNames, replacementNames, variableNames, variableValues);
  }

  /**
   * Export the cohort analysis results to a ZIP file
   * 
   * @summary Export cohort analysis results
   * @param id The cohort ID
   * @param sourceKey The source Key
   * @return A response containing the .ZIP file of results
   */
  @GET
  @Path("{sourceKey}/{id}/export.zip")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportCohortResults(@PathParam("id") int id, @PathParam("sourceKey") String sourceKey) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    try {
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      String sqlPath = BASE_SQL_PATH + "/raw/getAllResults.sql";
      String tqName = "tableQualifier";
      String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
      PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "cohortDefinitionId", whitelist(id));

      final StringBuilder resultData = new StringBuilder();
      final StringBuilder resultDistributionData = new StringBuilder();

      // results export
      getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Void>() {
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
      sqlPath = BASE_SQL_PATH + "/raw/getAllResultDistributions.sql";
      psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "cohortDefinitionId", whitelist(id));
      getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Void>() {
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
      CohortDTO cohortDefinition = cohortDefinitionService.getCohortDefinition(id);
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

  /**
   * Provides a warmup mechanism for the data visualization cache. This
   * endpoint does not appear to be used and may be a hold over from the 
   * original HERACLES implementation
   * 
   * @summary Warmup data visualizations
   * @param task The cohort analysis task
   * @return The number of report visualizations warmed
   */
  @POST
  @Path("/warmup")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public int warmUpVisualizationData(CohortAnalysisTask task) {
    return this.queryRunner.warmupData(this.getSourceJdbcTemplate(task.getSource()), task);

  }

  /**
   * Provides a list of cohort analysis visualizations that are completed
   * 
   * @summary Get completed cohort analysis visualizations
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return A list of visualization keys that are complete
   */
  @GET
  @Path("{sourceKey}/{id}/completed")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<String> getCompletedVisualiztion(@PathParam("id") final int id,
          @PathParam("sourceKey") final String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    List<VisualizationData> vizData = this.visualizationDataRepository.findByCohortDefinitionIdAndSourceId(id, source.getSourceId());
    Set<String> completed = new HashSet<>();
    if (CollectionUtils.isNotEmpty(vizData)) {
      for (VisualizationData viz : vizData) {
        completed.add(viz.getVisualizationKey());
      }
    }
    return completed;
  }


  /**
   * Retrieves the tornado plot
   * 
   * @summary Get the tornado plot
   * @param sourceKey The source key
   * @param cohortDefinitionId The cohort definition id
   * @return The tornado plot data
   */
  @GET
  @Path("{sourceKey}/{id}/tornado")
  @Produces(MediaType.APPLICATION_JSON)
  public TornadoReport getTornadoReport(@PathParam("sourceKey") final String sourceKey, @PathParam("id") final int cohortDefinitionId) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        TornadoReport tornadoReport = new TornadoReport();
        tornadoReport.tornadoRecords = queryRunner.getTornadoRecords(getSourceJdbcTemplate(source), cohortDefinitionId, source);
        tornadoReport.profileSamples = queryRunner.getProfileSampleRecords(getSourceJdbcTemplate(source), cohortDefinitionId, source);
        return tornadoReport;
  }

  /**
   * Queries for cohort analysis dashboard for the given cohort definition id
   *
   * @summary Get the dashboard
   * @param id The cohort definition id
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param demographicsOnly only render gender and age
   * @return CohortDashboard
   */
  @GET
  @Path("{sourceKey}/{id}/dashboard")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDashboard getDashboard(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return dashboard;

  }

  /**
   * Queries for cohort analysis condition treemap results for the given cohort
   * definition id
   * 
   * @summary Get condition treemap
   * @param sourceKey The source key
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/condition/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getConditionTreemap(@PathParam("sourceKey") String sourceKey, @PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   * Get the distinct person count for a cohort
   * 
   * @summary Get distinct person count
   * @param sourceKey The source key
   * @param id The cohort ID
   * @param refresh Boolean - refresh visualization data
   * @return Distinct person count as integer
   */
  @GET
  @Path("{sourceKey}/{id}/distinctPersonCount/")
  @Produces(MediaType.APPLICATION_JSON)
  public Integer getRawDistinctPersonCount(@PathParam("sourceKey") String sourceKey,
          @PathParam("id") String id,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetRawDistinctPersonCount(id, source);
    Integer result = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new ResultSetExtractor<Integer>() {
      @Override
      public Integer extractData(ResultSet rs) throws SQLException {

        while (rs.next()) {
          return rs.getInt(1);
        }
        return null;
      }
    });
    return result;
  }

  protected PreparedStatementRenderer prepareGetRawDistinctPersonCount(String id, Source source) {

    String sqlPath = BASE_SQL_PATH + "/raw/getTotalDistinctPeople.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String tqName = "tableQualifier";
    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "id", Integer.valueOf(id));
  }

  /**
   * Queries for cohort analysis condition drilldown results for the given
   * cohort definition id and condition id
   * 
   * @param sourceKey The source key
   * @param id The cohort ID
   * @param conditionId The condition concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param refresh Boolean - refresh visualization data
   * @return The CohortConditionDrilldown detail object
   */
  @GET
  @Path("{sourceKey}/{id}/condition/{conditionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortConditionDrilldown getConditionResults(@PathParam("sourceKey") String sourceKey,
          @PathParam("id") final int id,
          @PathParam("conditionId") final int conditionId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;

  }

  /**
   * Queries for cohort analysis condition era treemap results for the given
   * cohort definition id
   * 
   * @param sourceKey The source key
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/conditionera/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getConditionEraTreemap(@PathParam("sourceKey") final String sourceKey,
          @PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   * Get the completed analyses IDs for the selected cohort and source key
   * 
   * @summary Get completed analyses IDs
   * @param sourceKey The source key
   * @param id The cohort ID
   * @return A list of completed analysis IDs
   */
  @GET
  @Path("{sourceKey}/{id}/analyses")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Integer> getCompletedAnalyses(@PathParam("sourceKey") String sourceKey, @PathParam("id") String id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
		int sourceId = source.getSourceId();
		
    PreparedStatementRenderer psr = prepareGetCompletedAnalysis(id, sourceId);
    final String sql = psr.getSql();
    return this.getJdbcTemplate().query(sql, psr.getSetter(), new RowMapper<Integer>() {
          @Override
          public Integer mapRow(ResultSet resultSet, int arg1) throws SQLException {

            return resultSet.getInt(1);
          }
        }
    );
  }

  class GenerationInfoDTO {
    private String sourceKey;
    private Integer analysisId;
    private Integer progress;

    public GenerationInfoDTO() {
    }

    public GenerationInfoDTO(String sourceKey, Integer analysisId, Integer progress) {
      this.sourceKey = sourceKey;
      this.analysisId = analysisId;
      this.progress = progress;
    }

    public String getSourceKey() {
      return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
      this.sourceKey = sourceKey;
    }

    public Integer getAnalysisId() {
      return analysisId;
    }

    public void setAnalysisId(Integer analysisId) {
      this.analysisId = analysisId;
    }

    public Integer getProgress() {
      return progress;
    }

    public void setProgress(Integer progress) {
      this.progress = progress;
    }
  }

  /**
   * Get the analysis generation progress
   * 
   * @summary Get analysis progress
   * @param sourceKey The source key
   * @param id The cohort ID
   * @return The generation progress information
   */
  @GET
  @Path("{sourceKey}/{id}/info")
  @Produces(MediaType.APPLICATION_JSON)
  public GenerationInfoDTO getAnalysisProgress(@PathParam("sourceKey") String sourceKey, @PathParam("id") Integer id) {

    return getTransactionTemplateRequiresNew().execute(status -> {
      org.ohdsi.webapi.cohortdefinition.CohortDefinition def = cohortDefinitionRepository.findOne(id);
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      return def.getCohortAnalysisGenerationInfoList().stream()
              .filter(cd -> Objects.equals(cd.getSourceId(), source.getSourceId()))
              .findFirst().map(gen -> new GenerationInfoDTO(sourceKey, id, gen.getProgress()))
              .<RuntimeException>orElseThrow(NotFoundException::new);
    });
  }

  protected PreparedStatementRenderer prepareGetCompletedAnalysis(String id, int sourceId) {

    String sqlPath = BASE_SQL_PATH + "/raw/getCompletedAnalyses.sql";
    PreparedStatementRenderer psr = new PreparedStatementRenderer(getSourceRepository().findBySourceId(sourceId)
			, sqlPath
			, new String[]{"tableQualifier"}, new String[] { this.getOhdsiSchema()}
			, new String[]{"cohort_definition_id", "source_id"}, new Object[]{Integer.valueOf(id), Integer.valueOf(sourceId)});
    return psr;
  }

  /**
   * Queries for cohort analysis condition era drilldown results for the given
   * cohort definition id and condition id
   * 
   * @summary Get condition era drilldown report
   * @param id The cohort ID
   * @param conditionId The condition ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return The CohortConditionEraDrilldown object
   */
  @GET
  @Path("{sourceKey}/{id}/conditionera/{conditionId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortConditionEraDrilldown getConditionEraDrilldown(@PathParam("id") final int id,
                                                              @PathParam("conditionId") final int conditionId,
                                                              @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
                                                              @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;

  }

  /**
   * Queries for drug analysis treemap results for the given cohort
   * definition id
   * 
   * @summary Get drug treemap
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/drug/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getDrugTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   *
   * @param id cohort_defintion id
   * @param drugId drug_id (from concept)
   * @return CohortDrugDrilldown
   */
  /**
   * Queries for cohort analysis drug drilldown results for the given cohort
   * definition id and drug id
   * 
   * @summary Get drug drilldown report
   * @param id The cohort ID
   * @param drugId The drug concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return 
   */
  @GET
  @Path("{sourceKey}/{id}/drug/{drugId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDrugDrilldown getDrugResults(@PathParam("id") final int id, @PathParam("drugId") final int drugId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;

  }

  /**
   * Queries for cohort analysis drug era treemap results for the given cohort
   * definition id
   * 
   * @summary Get drug era treemap report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/drugera/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getDrugEraTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   *
   * @param id cohort_defintion id
   * @param drugId drug_id (from concept)
   * @return CohortDrugEraDrilldown
   */
  /**
   * Queries for cohort analysis drug era drilldown results for the given cohort
   * definition id and drug id
   * 
   * @summary Get drug era drilldown report
   * @param id The cohort ID
   * @param drugId The drug concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortDrugEraDrilldown
   */
  @GET
  @Path("{sourceKey}/{id}/drugera/{drugId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDrugEraDrilldown getDrugEraResults(@PathParam("id") final int id, @PathParam("drugId") final int drugId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;

  }

  /**
   * Queries for cohort analysis person results for the given cohort definition
   * id
   * 
   * @summary Get the person report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return 
   */
  @GET
  @Path("{sourceKey}/{id}/person")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortPersonSummary getPersonResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return person;
  }

  /**
   * Queries for cohort analysis cohort specific results for the given cohort
   * definition id
   * 
   * @summary Get cohort specific results
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortSpecificSummary
   */
  @GET
  @Path("{sourceKey}/{id}/cohortspecific")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSpecificSummary getCohortSpecificResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return summary;
  }

  /**
   * Queries for cohort analysis cohort specific treemap results for the given
   * cohort definition id
   * 
   * @summary Get cohort specific treemap
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortSpecificTreemap
   */
  @GET
  @Path("{sourceKey}/{id}/cohortspecifictreemap")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSpecificTreemap getCohortSpecificTreemapResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return summary;
  }

  /**
   * Queries for cohort analysis procedure drilldown results for the given
   * cohort definition id and concept id
   * 
   * @summary Get procedure drilldown report
   * @param id The cohort ID
   * @param conceptId The procedure concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<ScatterplotRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/cohortspecificprocedure/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortProcedureDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
          @PathParam("sourceKey") final String sourceKey,
          @DefaultValue("false") @QueryParam("refresh") boolean refresh) {

    List<ScatterplotRecord> records = new ArrayList<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    final String key = CohortResultsAnalysisRunner.COHORT_SPECIFIC_PROCEDURE_DRILLDOWN;
    VisualizationData data = refresh ? null : visualizationDataRepository
      .findByCohortDefinitionIdAndSourceIdAndVisualizationKeyAndDrilldownId(id, source.getSourceId(), key, conceptId);

    if (refresh || data == null) {
      records = this.queryRunner.getCohortProcedureDrilldown(this.getSourceJdbcTemplate(source), id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, source, true);
    } else {
      try {
        records = mapper.readValue(data.getData(), new TypeReference<List<ScatterplotRecord>>() {
        });
      } catch (Exception e) {
        log.error(whitelist(e));
      }
    }

    return records;
  }

  /**
   *
   * @param id cohort_definition id
   * @param conceptId conceptId (from concept)
   * @return List<ScatterplotRecord>
   */
  /**
   * Queries for cohort analysis drug drilldown results for the given cohort
   * definition id and concept id
   * 
   * @summary Get drug drilldown report for specific concept
   * @param id The cohort ID
   * @param conceptId The drug concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<ScatterplotRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/cohortspecificdrug/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortDrugDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }
    return records;
  }

  /**
   * Queries for cohort analysis condition drilldown results for the given
   * cohort definition id and concept id
   * 
   * @param id The cohort ID
   * @param conceptId The condition concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<ScatterplotRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/cohortspecificcondition/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ScatterplotRecord> getCohortConditionDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return records;
  }

  /**
   * Queries for cohort analysis for observation treemap
   * 
   * @summary Get observation treemap report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/observation")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getCohortObservationResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   * Queries for cohort analysis observation drilldown results for the given
   * cohort definition id and observation concept id
   * 
   * @summary Get observation drilldown report for a concept ID
   * @param id The cohort ID
   * @param conceptId The observation concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortObservationDrilldown
   */
  @GET
  @Path("{sourceKey}/{id}/observation/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortObservationDrilldown getCohortObservationResultsDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;

  }

  /**
   * Queries for cohort analysis for measurement treemap
   * 
   * @summary Get measurement treemap report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/measurement")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getCohortMeasurementResults(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   *
   * @param id cohort_defintion id
   * @param conceptId conceptId (from concept)
   * @return CohortMeasurementDrilldown
   */
  /**
   * Queries for cohort analysis measurement drilldown results for the given
   * cohort definition id and measurement concept id
   * 
   * @summary Get measurement drilldown report for concept ID
   * @param id The cohort ID
   * @param conceptId The measurement concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortMeasurementDrilldown
   */
  @GET
  @Path("{sourceKey}/{id}/measurement/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortMeasurementDrilldown getCohortMeasurementResultsDrilldown(@PathParam("id") final int id, @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;
  }

  /**
   * Queries for cohort analysis observation period for the given cohort
   * definition id
   * 
   * @summary Get observation period report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortObservationPeriod
   */
  @GET
  @Path("{sourceKey}/{id}/observationperiod")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortObservationPeriod getCohortObservationPeriod(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return obsPeriod;
  }

  /**
   * Queries for cohort analysis data density for the given cohort definition id
   * 
   * @summary Get data density report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortDataDensity
   */
  @GET
  @Path("{sourceKey}/{id}/datadensity")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDataDensity getCohortDataDensity(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return data;
  }

  /**
   * Queries for cohort analysis procedure treemap results for the given cohort
   * definition id
   * 
   * @summary Get procedure treemap report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/procedure/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getProcedureTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   * Queries for cohort analysis procedures for the given cohort definition id
   * and concept id
   * 
   * @summary Get procedure drilldown report by concept ID
   * @param id The cohort ID
   * @param conceptId The procedure concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortProceduresDrillDown
   */
  @GET
  @Path("{sourceKey}/{id}/procedure/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortProceduresDrillDown getCohortProceduresDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return drilldown;
  }

  /**
   * Queries for cohort analysis visit treemap results for the given cohort
   * definition id
   * 
   * @summary Get visit treemap report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GET
  @Path("{sourceKey}/{id}/visit/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HierarchicalConceptRecord> getVisitTreemap(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return res;
  }

  /**
   * Queries for cohort analysis visits for the given cohort definition id and
   * concept id
   * 
   * @summary Get visit drilldown for a visit concept ID
   * @param id The cohort ID
   * @param conceptId The visit concept iD
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return 
   */
  @GET
  @Path("{sourceKey}/{id}/visit/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortVisitsDrilldown getCohortVisitsDrilldown(@PathParam("id") final int id,
          @PathParam("conceptId") final int conceptId,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }
    return drilldown;
  }

  /**
   * Returns the summary for the cohort
   * 
   * @summary Get cohort summary
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return CohortSummary
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
      log.error(whitelist(e));
    }

    return summary;
  }

  /**
   * Queries for cohort analysis death data for the given cohort definition id
   * 
   * @summary Get death report
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return CohortDeathData
   */
  @GET
  @Path("{sourceKey}/{id}/death")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDeathData getCohortDeathData(@PathParam("id") final int id,
          @QueryParam("min_covariate_person_count") final Integer minCovariatePersonCountParam,
          @QueryParam("min_interval_person_count") final Integer minIntervalPersonCountParam,
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
        log.error(whitelist(e));
      }
    }

    return data;
  }

  /**
   * Returns the summary for the cohort
   * 
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return CohortSummary
   */
  @GET
  @Path("{sourceKey}/{id}/summaryanalyses")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortSummary getCohortSummaryAnalyses(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {

    CohortSummary summary = new CohortSummary();
    try {
      summary.setAnalyses(getCohortAnalysesForCohortDefinition(whitelist(id), sourceKey, true));
    } catch (Exception e) {
      log.error("unable to get cohort summary", e);
    }

    return summary;
  }

  /**
   * Returns the person identifiers of all members of a generated cohort
   * definition identifier
   * 
   * @summary Get persons in cohort
   * @param id The cohort ID
   * @param sourceKey The source key
   * @param min The minimum number of persons to return
   * @param max The maximum number of persons to return
   * @return Collection<CohortPerson>
   */
  @GET
  @Path("{sourceKey}/{id}/members/{min}-{max}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CohortPerson> getCohortMembers(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey, @PathParam("min") final int min, @PathParam("max") final int max) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/cohortresults/sql/raw/getMembers.sql";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String[] names = new String[]{"cohortDefinitionId", "min", "max"};
    Object[] values = new Object[]{whitelist(id), whitelist(min), whitelist(max)};
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, names, values, SessionUtils.sessionId());
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), cohortMemberMapper);
  }

  /**
   * Returns breakdown with counts about people in cohort
   * 
   * @summary Get cohort breakdown report
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return Collection<CohortBreakdown>
   */
  @GET
  @Path("{sourceKey}/{id}/breakdown")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CohortBreakdown> getCohortBreakdown(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/cohortresults/sql/raw/getCohortBreakdown.sql";
    String resultsTqName = "resultsTableQualifier";
    String resultsTqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTqName = "tableQualifier";
    String cdmTqValue = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String[] tqNames = new String[]{resultsTqName, cdmTqName};
    String[] tqValues = new String[]{resultsTqValue, cdmTqValue};
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqNames, tqValues, "cohortDefinitionId", id);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), cohortBreakdownMapper);
  }

  /**
   * Returns the person identifiers of all members of a cohort breakdown from above
   * 
   * @summary Get cohort person breakdown
   * @param id The cohort ID
   * @param sourceKey The source key
   * @param gender The string for gender (male/female)
   * @param age The numeric age
   * @param conditions The condition concept IDs
   * @param drugs The drug concept ids
   * @param rows The row limit
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


    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetCohortMembers(id, gender,
        age, conditions, drugs, rows, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(),psr.getSetter(), cohortMemberMapper);
  }


  protected PreparedStatementRenderer prepareGetCohortMembers(final int id,
                                                              String gender, String age, String conditions, String drugs,
                                                              final int rows, Source source) {
    String path =   "/resources/cohortresults/sql/raw/getCohortBreakdownPeople.sql";
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
    if (drugs.length() > 0 && !"''".equals(drugs)) {
      params.add(" drugs in (@drugs) ");
      wherecols.add("drugs");
      groups = groups * drugs.split(",").length;
    }
    String clause = " where 1=1\n";
    for (String param : params) {
      clause += (" and " + param + "\n");
    }

    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

    String wherecolsStr = "";
    if (!wherecols.isEmpty()) {
      wherecolsStr += wherecols.get(0);
      for (int i = 1; i < wherecols.size(); i++) {
        wherecolsStr += (',' + wherecols.get(i));
      }
    }
    String[] searchFor = new String[]{"tableQualifier", "resultsTableQualifier", "whereclause", "wherecols", "partition by"};
    String[] replaceWith = new String[]{cdmTableQualifier, resultsTableQualifier, clause, wherecolsStr, "partition by"};

    if (wherecols.isEmpty()) {
      searchFor = new String[]{"tableQualifier", "resultsTableQualifier", "whereclause", "wherecols", "partition by"};
      replaceWith = new String[]{cdmTableQualifier, resultsTableQualifier, clause, wherecolsStr, ""};
    }

    String[] names = new String[]{"cohortDefinitionId", "gender", "age", "conditions", "drugs", "rows", "groups"};

    String[] genderArray= gender.replaceAll("'", "").split(",");
    String[] ageArray= age.replaceAll("'", "").split(",");

    List<Integer> conditionIds =  Arrays.stream(conditions.split(",")).map(NumberUtils::toInt).collect(Collectors.toList());
    List<Integer> drugIds =  Arrays.stream(drugs.split(",")).map(NumberUtils::toInt).collect(Collectors.toList());

    Object[] values = new Object[]{id, genderArray, ageArray, conditionIds.toArray(), drugIds.toArray(), rows, groups};
    return new PreparedStatementRenderer(source, path, searchFor, replaceWith, names, values);
  }
  
  /**
   * Returns the count of all members of a generated cohort
   * definition identifier
   * 
   * @summary Get cohort member count
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return The cohort count
   */
  @GET
  @Path("{sourceKey}/{id}/members/count")
  @Produces(MediaType.APPLICATION_JSON)
  public Long getCohortMemberCount(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/cohortresults/sql/raw/getMemberCount.sql";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "cohortDefinitionId", whitelist(id), SessionUtils.sessionId());
    return getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), Long.class);
  }

  /**
   * Returns all cohort analyses in the results/OHDSI schema for the given
   * cohort_definition_id
   * 
   * @summary Get the cohort analysis list for a cohort
   * @param id The cohort ID
   * @param sourceKey The source key
   * @param retrieveFullDetail Boolean - when TRUE, the full analysis details are returned
   * @return List of all cohort analyses and their statuses for the given
   * cohort_defintion_id
   */
  @GET
  @Path("{sourceKey}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CohortAnalysis> getCohortAnalysesForCohortDefinition(@PathParam("id") final int id,
          @PathParam("sourceKey") String sourceKey,
          @DefaultValue("true") @QueryParam("fullDetail") boolean retrieveFullDetail) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sql;

    if (retrieveFullDetail) {
      sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalysesForCohortFull.sql");
    } else {
      sql = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/getCohortAnalysesForCohort.sql");
    }
    String tqName = "ohdsi_database_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "cohortDefinitionId", whitelist(id));

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), cohortAnalysisMapper);
  }

  /**
   * Get the exposure cohort incidence rates. This function is not using a 
   * proper incidence rate so this should be viewed as informational only
   * and not as a report
   * 
   * @summary DO NOT USE
   * @deprecated
   * @param sourceKey The source key
   * @param search The exposure cohort search
   * @return List<ExposureCohortResult>
   */
  @POST
  @Path("{sourceKey}/exposurecohortrates")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<ExposureCohortResult> getExposureOutcomeCohortRates(@PathParam("sourceKey") String sourceKey, ExposureCohortSearch search) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetExposureOutcomeCohortRates(search, source);

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

      ExposureCohortResult e = new ExposureCohortResult();
      e.exposureCohortDefinitionId = rs.getString("exposure_cohort_definition_id");
      e.incidenceRate1000py = rs.getFloat("incidence_rate_1000py");
      e.numPersonsExposed = rs.getLong("num_persons_exposed");
      e.numPersonsWithOutcomePostExposure = rs.getLong("num_persons_w_outcome_post_exposure");
      e.numPersonsWithOutcomePreExposure = rs.getLong("num_persons_w_outcome_pre_exposure");
      e.outcomeCohortDefinitionId = rs.getString("outcome_cohort_definition_id");
      e.timeAtRisk = rs.getFloat("time_at_risk");
      return e;
    });

  }

  protected PreparedStatementRenderer prepareGetExposureOutcomeCohortRates(
      ExposureCohortSearch search, Source source) {

    String path = "/resources/cohortresults/sql/cohortSpecific/getExposureOutcomeCohortRates.sql";
    String tqName = "ohdsi_database_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String[] names = new String[]{"exposure_cohort_definition_id", "outcome_cohort_definition_id"};
    Object[] values = new Object[]{search.exposureCohortList, search.outcomeCohortList};
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, path, tqName, tqValue, names, values);
    return psr;
  }
 
  /**
   * Provides a time to event calculation but it is unclear how this works.
   * 
   * @summary DO NOT USE
   * @deprecated
   * @param sourceKey The source key
   * @param search The exposure cohort search
   * @return List<TimeToEventResult>
   */
  @POST
  @Path("{sourceKey}/timetoevent")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<TimeToEventResult> getTimeToEventDrilldown(@PathParam("sourceKey") String sourceKey, ExposureCohortSearch search) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetTimeToEventDrilldown(search, source);

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

      TimeToEventResult e = new TimeToEventResult();
      e.countValue = rs.getLong("count_value");
      e.duration = rs.getLong("duration");
      e.exposureCohortDefinitionId = rs.getString("exposure_cohort_definition_id");
      e.outcomeCohortDefinitionId = rs.getString("outcome_cohort_definition_id");
      e.pctPersons = rs.getDouble("pct_persons");
      e.recordType = rs.getString("record_type");
      return e;
    });

  }

  protected PreparedStatementRenderer prepareGetTimeToEventDrilldown(
      ExposureCohortSearch search, Source source) {

    String path = "/resources/cohortresults/sql/cohortSpecific/getTimeToEventDrilldown.sql";
    String tqName = "ohdsi_database_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String[] names = new String[]{"exposure_cohort_definition_id", "outcome_cohort_definition_id"};
    Object[] values = new Object[]{search.exposureCohortList, search.outcomeCohortList};
    return new PreparedStatementRenderer(source, path, tqName, tqValue, names, values);
  }

  /**
   * Provides a predictor calculation but it is unclear how this works.
   * 
   * @summary DO NOT USE
   * @deprecated
   * @param sourceKey The source key
   * @param search The exposure cohort search
   * @return List<PredictorResult>
   */
  @POST
  @Path("{sourceKey}/predictors")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<PredictorResult> getExposureOutcomeCohortPredictors(@PathParam("sourceKey") String sourceKey, ExposureCohortSearch search) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetExposureOutcomeCohortPredictors(search, source);

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

      PredictorResult e = new PredictorResult();
      e.absStdDiff = rs.getString("abs_std_diff");
      e.conceptId = rs.getString("concept_id");
      e.conceptName = rs.getString("concept_name");
      e.conceptWithOutcome = rs.getString("concept_w_outcome");
      e.domainId = rs.getString("domain_id");
      e.pctOutcomeWithConcept = rs.getString("pct_outcome_w_concept");
      e.pctNoOutcomeWithConcept = rs.getString("pct_nooutcome_w_concept");
      e.exposureCohortDefinitionId = rs.getString("exposure_cohort_definition_id");
      e.outcomeCohortDefinitionId = rs.getString("outcome_cohort_definition_id");
      return e;
    });
  }

  /**
   *
   * @param id cohort definition id
   * @return List<CohortAttribute>
   */
  /**
   * Returns heracles heel results (data quality issues) for the given cohort
   * definition id
   * 
   * @summary Get HERACLES heel report
   * @param id The cohort iD
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
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
              log.error(e.getMessage());
          }
      }

      return attrs;
  }
  
  public List<AnalysisResults> getCohortAnalysesForDataCompleteness(final int id, String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/datacompleteness/getCohortDataCompleteness.sql");

    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, "tableQualifier",
      resultsTableQualifier, "cohortDefinitionId", id);

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new AnalysisResultsMapper());
  }

  /**
   * Provides a data completeness report for a cohort
   * 
   * @summary Get data completeness report
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return List<DataCompletenessAttr>
   */
  @GET
  @Path("{sourceKey}/{id}/datacompleteness")
  @Produces(MediaType.APPLICATION_JSON)
  public List<DataCompletenessAttr> getDataCompleteness(@PathParam("id") final int id,
          @PathParam("sourceKey") String sourceKey) {
      List<AnalysisResults> arl = this.getCohortAnalysesForDataCompleteness(id, sourceKey);
      
      List<DataCompletenessAttr> dcal = new ArrayList<>();
      
      Map<Integer, AnalysisResults> resultMap = new HashMap<>();

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

    public List<AnalysisResults> getCohortAnalysesEntropy(final int id, String sourceKey, int entroppAnalysisId) {

        String sql = ResourceHelper.GetResourceAsString("/resources/cohortresults/sql/entropy/getEntropy.sql");
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        
        String[] searchStringNames = new String[] { "tableQualifier" };
        String[] replacementNames = new String[] { resultsTableQualifier };
        
        String[] variableNames = new String[] { "cohortDefinitionId", "entroppAnalysisId" };
        Object[] variableValues = new Object[] { id, entroppAnalysisId };
        
        PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, searchStringNames, replacementNames,
                variableNames, variableValues);
        return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new AnalysisResultsMapper());
    }
    
    /**
     * Provide an entropy report for a cohort
     * 
     * @summary Get entropy report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return List<EntropyAttr>
     */
    @GET
    @Path("{sourceKey}/{id}/entropy")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EntropyAttr> getEntropy(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {
        List<AnalysisResults> arl = this.getCohortAnalysesEntropy(id, sourceKey, 2031);
        
        List<EntropyAttr> el = new ArrayList<>();
        
        for (AnalysisResults ar : arl) {
            EntropyAttr ea = new EntropyAttr();
            ea.setDate(ar.getStratum1());
            ea.setEntropy(Float.parseFloat(ar.getStratum2()));
            el.add(ea);
        }
        
        return el;
    }
    
    /**
     * Provide a full entropy report for a cohort
     * 
     * @summary Get full entropy report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return List<EntropyAttr>
     */
    @GET
    @Path("{sourceKey}/{id}/allentropy")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EntropyAttr> getAllEntropy(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey) {
        List<AnalysisResults> arl = this.getCohortAnalysesEntropy(id, sourceKey, 2031);
        
        List<EntropyAttr> el = new ArrayList<EntropyAttr>();
        
        for (AnalysisResults ar : arl) {
            EntropyAttr ea = new EntropyAttr();
            ea.setDate(ar.getStratum1());
            ea.setEntropy(Float.parseFloat(ar.getStratum2()));
            ea.setInsitution("All sites");
            el.add(ea);
        }
        
        arl = this.getCohortAnalysesEntropy(id, sourceKey, 2032);
        
        for (AnalysisResults ar : arl) {
            EntropyAttr ea = new EntropyAttr();
            String careSite = ar.getStratum2() != null && !ar.getStratum2().trim().equals("")
                    ? ar.getStratum1() + ":" + ar.getStratum2().trim() : ar.getStratum1();
            ea.setInsitution(careSite);
            ea.setDate(ar.getStratum3());
            ea.setEntropy(Float.parseFloat(ar.getStratum4()));
            el.add(ea);
        }
        
        return el;
    }

    /**
     * Get the healthcare utilization exposure report for a specific window
     * 
     * @summary Get healthcare utilization report for selected time window
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @param periodType The period type
     * @return HealthcareExposureReport
     */
    @GET
    @Path("{sourceKey}/{id}/healthcareutilization/exposure/{window}")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthcareExposureReport getHealthcareUtilizationExposureReport(@PathParam("id") final int id, @PathParam("sourceKey") String sourceKey
            , @PathParam("window") final WindowType window
            , @DefaultValue("ww") @QueryParam("periodType") final PeriodType periodType) {
            Source source = getSourceRepository().findBySourceKey(sourceKey);
            HealthcareExposureReport exposureReport = queryRunner.getHealthcareExposureReport(getSourceJdbcTemplate(source), id, window, periodType, source);
            return exposureReport;
    }

    /**
     * Get the healthcare utilization periods
     * 
     * @summary Get healthcare utilization periods
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @return A list of the periods
     */
    @GET
    @Path("{sourceKey}/{id}/healthcareutilization/periods/{window}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getHealthcareUtilizationPeriods(
                                    @PathParam("id") final int id
                                    , @PathParam("sourceKey") final String sourceKey
                                    , @PathParam("window") final WindowType window) {
            final Source source = getSourceRepository().findBySourceKey(sourceKey);
            final List<String> periodTypes = queryRunner.getHealthcarePeriodTypes(getSourceJdbcTemplate(source), id, window, source);
            return periodTypes;
    }

    /**
     * Get the healthcare utilization report by window, visit status,
     * period type, visit concept, visit type concept and cost type concept.
     * 
     * @summary Get healthcare utilization visit report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @param visitStat The visit status
     * @param periodType The period type
     * @param visitConcept The visit concept ID
     * @param visitTypeConcept The visit type concept ID
     * @param costTypeConcept The cost type concept ID
     * @return HealthcareVisitUtilizationReport
     */
    @GET
    @Path("{sourceKey}/{id}/healthcareutilization/visit/{window}/{visitStat}")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthcareVisitUtilizationReport getHealthcareUtilizationVisitReport(@PathParam("id") final int id
		, @PathParam("sourceKey") String sourceKey
		, @PathParam("window") final WindowType window
		, @PathParam("visitStat") final VisitStatType visitStat
		, @DefaultValue("ww") @QueryParam("periodType") final PeriodType periodType
		, @QueryParam("visitConcept") final Long visitConcept
		, @QueryParam("visitTypeConcept") final Long visitTypeConcept
		, @DefaultValue("31968") @QueryParam("costTypeConcept") final Long costTypeConcept) {
		Source source = getSourceRepository().findBySourceKey(sourceKey);
		HealthcareVisitUtilizationReport visitUtilizationReport = queryRunner.getHealthcareVisitReport(getSourceJdbcTemplate(source), id, window, visitStat, periodType, visitConcept, visitTypeConcept, costTypeConcept, source);
		return visitUtilizationReport;
	}	

    /**
     * Get the healthcare utilization summary report by drug and 
     * cost type concept
     * 
     * @summary Get healthcare utilization drug summary report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @param drugTypeConceptId The drug type concept ID
     * @param costTypeConceptId The cost type concept ID
     * @return HealthcareDrugUtilizationSummary
     */
    @GET
    @Path("{sourceKey}/{id}/healthcareutilization/drug/{window}")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthcareDrugUtilizationSummary getHealthcareUtilizationDrugSummaryReport(@PathParam("id") final int id
		, @PathParam("sourceKey") String sourceKey
		, @PathParam("window") final WindowType window 
		, @QueryParam("drugType") final Long drugTypeConceptId
		, @DefaultValue("31968") @QueryParam("costType") final Long costTypeConceptId
		
	) {
		Source source = getSourceRepository().findBySourceKey(sourceKey);
		HealthcareDrugUtilizationSummary report = queryRunner.getHealthcareDrugUtilizationSummary(getSourceJdbcTemplate(source), id, window, drugTypeConceptId, costTypeConceptId, source);
		return report;
	}	

    /**
     * Get the healthcare utilization detail report by drug and 
     * cost type concept
     * 
     * @summary Get healthcare utilization drug detail report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @param drugConceptId The drug concept ID
     * @param periodType The period type
     * @param drugTypeConceptId The drug type concept ID
     * @param costTypeConceptId The cost type concept ID
     * @return HealthcareDrugUtilizationDetail
     */
    @GET
    @Path("{sourceKey}/{id}/healthcareutilization/drug/{window}/{drugConceptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthcareDrugUtilizationDetail getHealthcareUtilizationDrugDetailReport(@PathParam("id") final int id
		, @PathParam("sourceKey") String sourceKey
		, @PathParam("window") final WindowType window
		, @PathParam("drugConceptId") final Long drugConceptId
		, @DefaultValue("ww") @QueryParam("periodType") final PeriodType periodType
		, @QueryParam("drugType") final Long drugTypeConceptId
		, @DefaultValue("31968") @QueryParam("costType") final Long costTypeConceptId
	) {	
		Source source = getSourceRepository().findBySourceKey(sourceKey);
		HealthcareDrugUtilizationDetail report = queryRunner.getHealthcareDrugUtilizationReport(getSourceJdbcTemplate(source), id, window, drugConceptId, drugTypeConceptId, periodType, costTypeConceptId, source);
		return report;
	}

    /**
     * Get the drug type concepts for the selected drug concept ID
     * 
     * @summary Get drug types for healthcare utilization report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param drugConceptId The drug concept ID
     * @return A list of concepts of drug types
     */
    @GET
    @Path("{sourceKey}/{id}/healthcareutilization/drugtypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Concept> getDrugTypes(@PathParam("id") final int id
		, @PathParam("sourceKey") String sourceKey
		, @QueryParam("drugConceptId") final Long drugConceptId) 
	{	
		Source source = getSourceRepository().findBySourceKey(sourceKey);
		return queryRunner.getDrugTypes(getSourceJdbcTemplate(source), id, drugConceptId, source);
	}	
	
  protected PreparedStatementRenderer prepareGetExposureOutcomeCohortPredictors(
    ExposureCohortSearch search, Source source) {


    String path = "/resources/cohortresults/sql/cohortSpecific/getExposureOutcomePredictors.sql";
    String resultsQualName = "ohdsi_database_schema";
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmQualName = "cdm_schema";
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    String[] searchFor = new String[]{resultsQualName, cdmQualName};
    String[] replace = new String[]{resultsTableQualifier, cdmTableQualifier};
    String[] names = new String[]{"exposure_cohort_definition_id", "outcome_cohort_definition_id", "minCellCount"};
    Object[] values = new Object[]{search.exposureCohortList, search.outcomeCohortList, search.minCellCount};

    return new PreparedStatementRenderer(source, path, searchFor, replace, names, values);
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
      mapAnalysis(cohortAnalysis, rs);
      cohortAnalysis.setAnalysisComplete(rs.getInt(CohortAnalysis.ANALYSIS_COMPLETE) == 1);
      cohortAnalysis.setCohortDefinitionId(rs.getInt(CohortAnalysis.COHORT_DEFINITION_ID));
      cohortAnalysis.setLastUpdateTime(rs.getTimestamp(CohortAnalysis.LAST_UPDATE_TIME));
      return cohortAnalysis;
    }
  };

  private void mapAnalysis(final Analysis analysis, final ResultSet rs) throws SQLException {

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
