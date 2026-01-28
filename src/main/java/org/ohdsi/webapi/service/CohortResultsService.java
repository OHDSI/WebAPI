package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSetMetaData;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


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
@RestController
@RequestMapping("/cohortresults")
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
  @GetMapping(value = "/{sourceKey}/{id}/raw/{analysisGroup}/{analysisName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getCohortResultsRaw(
          @PathVariable("id") final int id,
          @PathVariable("analysisGroup") final String analysisGroup,
          @PathVariable("analysisName") final String analysisName,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") String sourceKey) {
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
  @GetMapping(value = "/{sourceKey}/{id}/export.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<Resource> exportCohortResults(
          @PathVariable("id") int id,
          @PathVariable("sourceKey") String sourceKey) {
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

    ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDispositionFormData("attachment", "cohort_" + id + "_export.zip");
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

    return ResponseEntity.ok()
            .headers(headers)
            .contentLength(resource.contentLength())
            .body(resource);
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
  @PostMapping(value = "/warmup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public int warmUpVisualizationData(@RequestBody CohortAnalysisTask task) {
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
  @GetMapping(value = "/{sourceKey}/{id}/completed", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection<String> getCompletedVisualiztion(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") final String sourceKey) {
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
  @GetMapping(value = "/{sourceKey}/{id}/tornado", produces = MediaType.APPLICATION_JSON_VALUE)
  public TornadoReport getTornadoReport(
          @PathVariable("sourceKey") final String sourceKey,
          @PathVariable("id") final int cohortDefinitionId) {
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
  @GetMapping(value = "/{sourceKey}/{id}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortDashboard getDashboard(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @RequestParam(value = "demographics_only", defaultValue = "false") final boolean demographicsOnly,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/condition/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getConditionTreemap(
          @PathVariable("sourceKey") String sourceKey,
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/distinctPersonCount/", produces = MediaType.APPLICATION_JSON_VALUE)
  public Integer getRawDistinctPersonCount(
          @PathVariable("sourceKey") String sourceKey,
          @PathVariable("id") String id,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
   * @summary Get condition drilldown report
   * @param sourceKey The source key
   * @param id The cohort ID
   * @param conditionId The condition concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param refresh Boolean - refresh visualization data
   * @return The CohortConditionDrilldown detail object
   */
  @GetMapping(value = "/{sourceKey}/{id}/condition/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortConditionDrilldown getConditionResults(
          @PathVariable("sourceKey") String sourceKey,
          @PathVariable("id") final int id,
          @PathVariable("conditionId") final int conditionId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
   * @summary Get condition era treemap
   * @param sourceKey The source key
   * @param id The cohort ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param refresh Boolean - refresh visualization data
   * @return List<HierarchicalConceptRecord>
   */
  @GetMapping(value = "/{sourceKey}/{id}/conditionera/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getConditionEraTreemap(
          @PathVariable("sourceKey") final String sourceKey,
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/analyses", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Integer> getCompletedAnalyses(
          @PathVariable("sourceKey") String sourceKey,
          @PathVariable("id") String id) {
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
  @GetMapping(value = "/{sourceKey}/{id}/info", produces = MediaType.APPLICATION_JSON_VALUE)
  public GenerationInfoDTO getAnalysisProgress(
          @PathVariable("sourceKey") String sourceKey,
          @PathVariable("id") Integer id) {

    return getTransactionTemplateRequiresNew().execute(status -> {
      org.ohdsi.webapi.cohortdefinition.CohortDefinition def = cohortDefinitionRepository.findById(id).orElse(null);
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      return def.getCohortAnalysisGenerationInfoList().stream()
              .filter(cd -> Objects.equals(cd.getSourceId(), source.getSourceId()))
              .findFirst().map(gen -> new GenerationInfoDTO(sourceKey, id, gen.getProgress()))
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
  @GetMapping(value = "/{sourceKey}/{id}/conditionera/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortConditionEraDrilldown getConditionEraDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conditionId") final int conditionId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/drug/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getDrugTreemap(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
   * @return CohortDrugDrilldown
   */
  @GetMapping(value = "/{sourceKey}/{id}/drug/{drugId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortDrugDrilldown getDrugResults(
          @PathVariable("id") final int id,
          @PathVariable("drugId") final int drugId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/drugera/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getDrugEraTreemap(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/drugera/{drugId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortDrugEraDrilldown getDrugEraResults(
          @PathVariable("id") final int id,
          @PathVariable("drugId") final int drugId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
   * @return CohortPersonSummary
   */
  @GetMapping(value = "/{sourceKey}/{id}/person", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortPersonSummary getPersonResults(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/cohortspecific", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortSpecificSummary getCohortSpecificResults(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/cohortspecifictreemap", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortSpecificTreemap getCohortSpecificTreemapResults(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/cohortspecificprocedure/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ScatterplotRecord> getCohortProcedureDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/cohortspecificdrug/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ScatterplotRecord> getCohortDrugDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
   * @summary Get condition drilldown report by concept ID
   * @param id The cohort ID
   * @param conceptId The condition concept ID
   * @param minCovariatePersonCountParam The minimum number of covariates per person
   * @param minIntervalPersonCountParam The minimum interval person count
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<ScatterplotRecord>
   */
  @GetMapping(value = "/{sourceKey}/{id}/cohortspecificcondition/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ScatterplotRecord> getCohortConditionDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/observation", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getCohortObservationResults(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/observation/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortObservationDrilldown getCohortObservationResultsDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/measurement", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getCohortMeasurementResults(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/measurement/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortMeasurementDrilldown getCohortMeasurementResultsDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/observationperiod", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortObservationPeriod getCohortObservationPeriod(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/datadensity", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortDataDensity getCohortDataDensity(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/procedure/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getProcedureTreemap(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
  @GetMapping(value = "/{sourceKey}/{id}/procedure/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortProceduresDrillDown getCohortProceduresDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/visit/", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<HierarchicalConceptRecord> getVisitTreemap(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

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
   * @return CohortVisitsDrilldown
   */
  @GetMapping(value = "/{sourceKey}/{id}/visit/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortVisitsDrilldown getCohortVisitsDrilldown(
          @PathVariable("id") final int id,
          @PathVariable("conceptId") final int conceptId,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/summarydata", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortSummary getCohortSummaryData(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") String sourceKey) {

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
  @GetMapping(value = "/{sourceKey}/{id}/death", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortDeathData getCohortDeathData(
          @PathVariable("id") final int id,
          @RequestParam(value = "min_covariate_person_count", required = false) final Integer minCovariatePersonCountParam,
          @RequestParam(value = "min_interval_person_count", required = false) final Integer minIntervalPersonCountParam,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
   * @summary Get cohort summary analyses
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return CohortSummary
   */
  @GetMapping(value = "/{sourceKey}/{id}/summaryanalyses", produces = MediaType.APPLICATION_JSON_VALUE)
  public CohortSummary getCohortSummaryAnalyses(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") String sourceKey) {

    CohortSummary summary = new CohortSummary();
    try {
      summary.setAnalyses(getCohortAnalysesForCohortDefinition(whitelist(id), sourceKey, true));
    } catch (Exception e) {
      log.error("unable to get cohort summary", e);
    }

    return summary;
  }


  /**
   * Returns breakdown with counts about people in cohort
   *
   * @summary Get cohort breakdown report
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return Collection<CohortBreakdown>
   */
  @GetMapping(value = "/{sourceKey}/{id}/breakdown", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection<CohortBreakdown> getCohortBreakdown(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") String sourceKey) {
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
   * Returns the count of all members of a generated cohort
   * definition identifier
   *
   * @summary Get cohort member count
   * @param id The cohort ID
   * @param sourceKey The source key
   * @return The cohort count
   */
  @GetMapping(value = "/{sourceKey}/{id}/members/count", produces = MediaType.APPLICATION_JSON_VALUE)
  public Long getCohortMemberCount(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") String sourceKey) {
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
  @GetMapping(value = "/{sourceKey}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CohortAnalysis> getCohortAnalysesForCohortDefinition(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") String sourceKey,
          @RequestParam(value = "fullDetail", defaultValue = "true") boolean retrieveFullDetail) {

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
  @PostMapping(value = "/{sourceKey}/exposurecohortrates", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  public List<ExposureCohortResult> getExposureOutcomeCohortRates(
          @PathVariable("sourceKey") String sourceKey,
          @RequestBody ExposureCohortSearch search) {

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
  @PostMapping(value = "/{sourceKey}/timetoevent", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  public List<TimeToEventResult> getTimeToEventDrilldown(
          @PathVariable("sourceKey") String sourceKey,
          @RequestBody ExposureCohortSearch search) {

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
  @PostMapping(value = "/{sourceKey}/predictors", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Deprecated
  public List<PredictorResult> getExposureOutcomeCohortPredictors(
          @PathVariable("sourceKey") String sourceKey,
          @RequestBody ExposureCohortSearch search) {

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
   * Returns heracles heel results (data quality issues) for the given cohort
   * definition id
   *
   * @summary Get HERACLES heel report
   * @param id The cohort iD
   * @param sourceKey The source key
   * @param refresh Boolean - refresh visualization data
   * @return List<CohortAttribute>
   */
  @GetMapping(value = "/{sourceKey}/{id}/heraclesheel", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<CohortAttribute> getHeraclesHeel(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") final String sourceKey,
          @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
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
  @GetMapping(value = "/{sourceKey}/{id}/datacompleteness", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataCompletenessAttr> getDataCompleteness(
          @PathVariable("id") final int id,
          @PathVariable("sourceKey") String sourceKey) {
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
    @GetMapping(value = "/{sourceKey}/{id}/entropy", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EntropyAttr> getEntropy(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey) {
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
    @GetMapping(value = "/{sourceKey}/{id}/allentropy", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EntropyAttr> getAllEntropy(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey) {
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
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/exposure/{window}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthcareExposureReport getHealthcareUtilizationExposureReport(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") final WindowType window,
            @RequestParam(value = "periodType", defaultValue = "ww") final PeriodType periodType) {
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
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/periods/{window}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getHealthcareUtilizationPeriods(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") final String sourceKey,
            @PathVariable("window") final WindowType window) {
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
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/visit/{window}/{visitStat}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthcareVisitUtilizationReport getHealthcareUtilizationVisitReport(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") final WindowType window,
            @PathVariable("visitStat") final VisitStatType visitStat,
            @RequestParam(value = "periodType", defaultValue = "ww") final PeriodType periodType,
            @RequestParam(value = "visitConcept", required = false) final Long visitConcept,
            @RequestParam(value = "visitTypeConcept", required = false) final Long visitTypeConcept,
            @RequestParam(value = "costTypeConcept", defaultValue = "31968") final Long costTypeConcept) {
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
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/drug/{window}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthcareDrugUtilizationSummary getHealthcareUtilizationDrugSummaryReport(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") final WindowType window,
            @RequestParam(value = "drugType", required = false) final Long drugTypeConceptId,
            @RequestParam(value = "costType", defaultValue = "31968") final Long costTypeConceptId) {
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
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/drug/{window}/{drugConceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthcareDrugUtilizationDetail getHealthcareUtilizationDrugDetailReport(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") final WindowType window,
            @PathVariable("drugConceptId") final Long drugConceptId,
            @RequestParam(value = "periodType", defaultValue = "ww") final PeriodType periodType,
            @RequestParam(value = "drugType", required = false) final Long drugTypeConceptId,
            @RequestParam(value = "costType", defaultValue = "31968") final Long costTypeConceptId) {	
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
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/drugtypes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Concept> getDrugTypes(
            @PathVariable("id") final int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "drugConceptId", required = false) final Long drugConceptId) {	
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
