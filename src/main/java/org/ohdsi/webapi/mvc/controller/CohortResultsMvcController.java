package org.ohdsi.webapi.mvc.controller;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ohdsi.webapi.cohortanalysis.CohortAnalysis;
import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTask;
import org.ohdsi.webapi.cohortanalysis.CohortSummary;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortresults.*;
import org.ohdsi.webapi.model.results.AnalysisResults;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.CohortResultsService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring MVC version of CohortResultsService
 *
 * Migration Status: Replaces /service/CohortResultsService.java (Jersey)
 * Endpoints: 40+ endpoints for cohort analysis results (Heracles Results)
 * Complexity: High - extensive analysis reporting, caching, multiple data types
 *
 * REST Services related to retrieving cohort analysis (a.k.a Heracles Results) analyses results.
 * More information on the Heracles project can be found at
 * {@link https://www.ohdsi.org/web/wiki/doku.php?id=documentation:software:heracles}.
 * The implementation found in WebAPI represents a migration of the functionality
 * from the stand-alone HERACLES application to integrate it into WebAPI and ATLAS.
 *
 * @summary Cohort Analysis Results (a.k.a Heracles Results)
 */
@RestController
@RequestMapping("/cohortresults")
public class CohortResultsMvcController extends AbstractMvcController {

    @Autowired
    private CohortResultsService cohortResultsService;

    @Autowired
    private CohortDefinitionService cohortDefinitionService;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Queries for cohort analysis results for the given cohort definition id
     *
     * @summary Get results for analysis group
     * @param id cohort_definition id
     * @param analysisGroup Name of the analysisGrouping under the /resources/cohortresults/sql/ directory
     * @param analysisName Name of the analysis, currently the same name as the sql file under analysisGroup
     * @param sourceKey the source to retrieve results
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @return List of key, value pairs
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/raw/{analysis_group}/{analysis_name}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/raw/{analysis_group}/{analysis_name}
     */
    @GetMapping(value = "/{sourceKey}/{id}/raw/{analysisGroup}/{analysisName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> getCohortResultsRaw(
            @PathVariable("id") int id,
            @PathVariable("analysisGroup") String analysisGroup,
            @PathVariable("analysisName") String analysisName,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam) {

        List<Map<String, String>> results = cohortResultsService.getCohortResultsRaw(
                id, analysisGroup, analysisName, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey);
        return ok(results);
    }

    /**
     * Export the cohort analysis results to a ZIP file
     *
     * @summary Export cohort analysis results
     * @param id The cohort ID
     * @param sourceKey The source Key
     * @return A response containing the .ZIP file of results
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/export.zip
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/export.zip
     */
    @GetMapping(value = "/{sourceKey}/{id}/export.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> exportCohortResults(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        jakarta.ws.rs.core.Response jerseyResponse = cohortResultsService.exportCohortResults(id, sourceKey);
        ByteArrayOutputStream baos = (ByteArrayOutputStream) jerseyResponse.getEntity();

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
     *
     * Jersey: POST /WebAPI/cohortresults/warmup
     * Spring MVC: POST /WebAPI/v2/cohortresults/warmup
     */
    @PostMapping(value = "/warmup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> warmUpVisualizationData(@RequestBody CohortAnalysisTask task) {
        int result = cohortResultsService.warmUpVisualizationData(task);
        return ok(result);
    }

    /**
     * Provides a list of cohort analysis visualizations that are completed
     *
     * @summary Get completed cohort analysis visualizations
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return A list of visualization keys that are complete
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/completed
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/completed
     */
    @GetMapping(value = "/{sourceKey}/{id}/completed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<String>> getCompletedVisualization(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        Collection<String> result = cohortResultsService.getCompletedVisualiztion(id, sourceKey);
        return ok(result);
    }

    /**
     * Retrieves the tornado plot
     *
     * @summary Get the tornado plot
     * @param sourceKey The source key
     * @param cohortDefinitionId The cohort definition id
     * @return The tornado plot data
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/tornado
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/tornado
     */
    @GetMapping(value = "/{sourceKey}/{id}/tornado", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TornadoReport> getTornadoReport(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") int cohortDefinitionId) {

        TornadoReport result = cohortResultsService.getTornadoReport(sourceKey, cohortDefinitionId);
        return ok(result);
    }

    /**
     * Queries for cohort analysis dashboard for the given cohort definition id
     *
     * @summary Get the dashboard
     * @param id The cohort definition id
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param demographicsOnly only render gender and age
     * @param refresh Boolean - refresh visualization data
     * @return CohortDashboard
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/dashboard
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/dashboard
     */
    @GetMapping(value = "/{sourceKey}/{id}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortDashboard> getDashboard(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "demographics_only", defaultValue = "false") boolean demographicsOnly,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortDashboard result = cohortResultsService.getDashboard(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, demographicsOnly, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis condition treemap results for the given cohort definition id
     *
     * @summary Get condition treemap
     * @param sourceKey The source key
     * @param id The cohort ID
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/condition/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/condition/
     */
    @GetMapping(value = "/{sourceKey}/{id}/condition/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getConditionTreemap(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") int id,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getConditionTreemap(
                sourceKey, id, minCovariatePersonCountParam, minIntervalPersonCountParam, refresh);
        return ok(result);
    }

    /**
     * Get the distinct person count for a cohort
     *
     * @summary Get distinct person count
     * @param sourceKey The source key
     * @param id The cohort ID
     * @param refresh Boolean - refresh visualization data
     * @return Distinct person count as integer
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/distinctPersonCount/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/distinctPersonCount/
     */
    @GetMapping(value = "/{sourceKey}/{id}/distinctPersonCount/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getRawDistinctPersonCount(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") String id,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        Integer result = cohortResultsService.getRawDistinctPersonCount(sourceKey, id, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis condition drilldown results for the given cohort definition id and condition id
     *
     * @summary Get condition drilldown report
     * @param sourceKey The source key
     * @param id The cohort ID
     * @param conditionId The condition concept ID
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return The CohortConditionDrilldown detail object
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/condition/{conditionId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/condition/{conditionId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/condition/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortConditionDrilldown> getConditionResults(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") int id,
            @PathVariable("conditionId") int conditionId,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortConditionDrilldown result = cohortResultsService.getConditionResults(
                sourceKey, id, conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis condition era treemap results for the given cohort definition id
     *
     * @summary Get condition era treemap
     * @param sourceKey The source key
     * @param id The cohort ID
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/conditionera/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/conditionera/
     */
    @GetMapping(value = "/{sourceKey}/{id}/conditionera/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getConditionEraTreemap(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") int id,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getConditionEraTreemap(
                sourceKey, id, minCovariatePersonCountParam, minIntervalPersonCountParam, refresh);
        return ok(result);
    }

    /**
     * Get the completed analyses IDs for the selected cohort and source key
     *
     * @summary Get completed analyses IDs
     * @param sourceKey The source key
     * @param id The cohort ID
     * @return A list of completed analysis IDs
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/analyses
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/analyses
     */
    @GetMapping(value = "/{sourceKey}/{id}/analyses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Integer>> getCompletedAnalyses(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") String id) {

        List<Integer> result = cohortResultsService.getCompletedAnalyses(sourceKey, id);
        return ok(result);
    }

    /**
     * Get the analysis generation progress
     *
     * @summary Get analysis progress
     * @param sourceKey The source key
     * @param id The cohort ID
     * @return The generation progress information
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/info
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/info
     */
    @GetMapping(value = "/{sourceKey}/{id}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAnalysisProgress(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("id") Integer id) {

        Object result = cohortResultsService.getAnalysisProgress(sourceKey, id);
        return ok(result);
    }

    /**
     * Queries for cohort analysis condition era drilldown results for the given cohort definition id and condition id
     *
     * @summary Get condition era drilldown report
     * @param id The cohort ID
     * @param conditionId The condition ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return The CohortConditionEraDrilldown object
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/conditionera/{conditionId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/conditionera/{conditionId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/conditionera/{conditionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortConditionEraDrilldown> getConditionEraDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conditionId") int conditionId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortConditionEraDrilldown result = cohortResultsService.getConditionEraDrilldown(
                id, conditionId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for drug analysis treemap results for the given cohort definition id
     *
     * @summary Get drug treemap
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/drug/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/drug/
     */
    @GetMapping(value = "/{sourceKey}/{id}/drug/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getDrugTreemap(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getDrugTreemap(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis drug drilldown results for the given cohort definition id and drug id
     *
     * @summary Get drug drilldown report
     * @param id The cohort ID
     * @param drugId The drug concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortDrugDrilldown
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/drug/{drugId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/drug/{drugId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/drug/{drugId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortDrugDrilldown> getDrugResults(
            @PathVariable("id") int id,
            @PathVariable("drugId") int drugId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortDrugDrilldown result = cohortResultsService.getDrugResults(
                id, drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis drug era treemap results for the given cohort definition id
     *
     * @summary Get drug era treemap report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/drugera/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/drugera/
     */
    @GetMapping(value = "/{sourceKey}/{id}/drugera/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getDrugEraTreemap(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getDrugEraTreemap(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis drug era drilldown results for the given cohort definition id and drug id
     *
     * @summary Get drug era drilldown report
     * @param id The cohort ID
     * @param drugId The drug concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortDrugEraDrilldown
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/drugera/{drugId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/drugera/{drugId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/drugera/{drugId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortDrugEraDrilldown> getDrugEraResults(
            @PathVariable("id") int id,
            @PathVariable("drugId") int drugId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortDrugEraDrilldown result = cohortResultsService.getDrugEraResults(
                id, drugId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis person results for the given cohort definition id
     *
     * @summary Get the person report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortPersonSummary
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/person
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/person
     */
    @GetMapping(value = "/{sourceKey}/{id}/person", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortPersonSummary> getPersonResults(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortPersonSummary result = cohortResultsService.getPersonResults(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis cohort specific results for the given cohort definition id
     *
     * @summary Get cohort specific results
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortSpecificSummary
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/cohortspecific
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/cohortspecific
     */
    @GetMapping(value = "/{sourceKey}/{id}/cohortspecific", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortSpecificSummary> getCohortSpecificResults(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortSpecificSummary result = cohortResultsService.getCohortSpecificResults(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis cohort specific treemap results for the given cohort definition id
     *
     * @summary Get cohort specific treemap
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortSpecificTreemap
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/cohortspecifictreemap
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/cohortspecifictreemap
     */
    @GetMapping(value = "/{sourceKey}/{id}/cohortspecifictreemap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortSpecificTreemap> getCohortSpecificTreemapResults(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortSpecificTreemap result = cohortResultsService.getCohortSpecificTreemapResults(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis procedure drilldown results for the given cohort definition id and concept id
     *
     * @summary Get procedure drilldown report
     * @param id The cohort ID
     * @param conceptId The procedure concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<ScatterplotRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/cohortspecificprocedure/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/cohortspecificprocedure/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/cohortspecificprocedure/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScatterplotRecord>> getCohortProcedureDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<ScatterplotRecord> result = cohortResultsService.getCohortProcedureDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis drug drilldown results for the given cohort definition id and concept id
     *
     * @summary Get drug drilldown report for specific concept
     * @param id The cohort ID
     * @param conceptId The drug concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<ScatterplotRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/cohortspecificdrug/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/cohortspecificdrug/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/cohortspecificdrug/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScatterplotRecord>> getCohortDrugDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<ScatterplotRecord> result = cohortResultsService.getCohortDrugDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis condition drilldown results for the given cohort definition id and concept id
     *
     * @summary Get condition drilldown report by concept ID
     * @param id The cohort ID
     * @param conceptId The condition concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<ScatterplotRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/cohortspecificcondition/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/cohortspecificcondition/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/cohortspecificcondition/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScatterplotRecord>> getCohortConditionDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<ScatterplotRecord> result = cohortResultsService.getCohortConditionDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis for observation treemap
     *
     * @summary Get observation treemap report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/observation
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/observation
     */
    @GetMapping(value = "/{sourceKey}/{id}/observation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getCohortObservationResults(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getCohortObservationResults(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis observation drilldown results for the given cohort definition id and observation concept id
     *
     * @summary Get observation drilldown report for a concept ID
     * @param id The cohort ID
     * @param conceptId The observation concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortObservationDrilldown
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/observation/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/observation/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/observation/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortObservationDrilldown> getCohortObservationResultsDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortObservationDrilldown result = cohortResultsService.getCohortObservationResultsDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis for measurement treemap
     *
     * @summary Get measurement treemap report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/measurement
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/measurement
     */
    @GetMapping(value = "/{sourceKey}/{id}/measurement", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getCohortMeasurementResults(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getCohortMeasurementResults(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis measurement drilldown results for the given cohort definition id and measurement concept id
     *
     * @summary Get measurement drilldown report for concept ID
     * @param id The cohort ID
     * @param conceptId The measurement concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortMeasurementDrilldown
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/measurement/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/measurement/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/measurement/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortMeasurementDrilldown> getCohortMeasurementResultsDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortMeasurementDrilldown result = cohortResultsService.getCohortMeasurementResultsDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis observation period for the given cohort definition id
     *
     * @summary Get observation period report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortObservationPeriod
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/observationperiod
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/observationperiod
     */
    @GetMapping(value = "/{sourceKey}/{id}/observationperiod", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortObservationPeriod> getCohortObservationPeriod(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortObservationPeriod result = cohortResultsService.getCohortObservationPeriod(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis data density for the given cohort definition id
     *
     * @summary Get data density report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortDataDensity
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/datadensity
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/datadensity
     */
    @GetMapping(value = "/{sourceKey}/{id}/datadensity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortDataDensity> getCohortDataDensity(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortDataDensity result = cohortResultsService.getCohortDataDensity(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis procedure treemap results for the given cohort definition id
     *
     * @summary Get procedure treemap report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/procedure/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/procedure/
     */
    @GetMapping(value = "/{sourceKey}/{id}/procedure/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getProcedureTreemap(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getProcedureTreemap(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis procedures for the given cohort definition id and concept id
     *
     * @summary Get procedure drilldown report by concept ID
     * @param id The cohort ID
     * @param conceptId The procedure concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortProceduresDrillDown
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/procedure/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/procedure/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/procedure/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortProceduresDrillDown> getCohortProceduresDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortProceduresDrillDown result = cohortResultsService.getCohortProceduresDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis visit treemap results for the given cohort definition id
     *
     * @summary Get visit treemap report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return List<HierarchicalConceptRecord>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/visit/
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/visit/
     */
    @GetMapping(value = "/{sourceKey}/{id}/visit/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HierarchicalConceptRecord>> getVisitTreemap(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<HierarchicalConceptRecord> result = cohortResultsService.getVisitTreemap(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Queries for cohort analysis visits for the given cohort definition id and concept id
     *
     * @summary Get visit drilldown for a visit concept ID
     * @param id The cohort ID
     * @param conceptId The visit concept ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortVisitsDrilldown
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/visit/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/visit/{conceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/visit/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortVisitsDrilldown> getCohortVisitsDrilldown(
            @PathVariable("id") int id,
            @PathVariable("conceptId") int conceptId,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortVisitsDrilldown result = cohortResultsService.getCohortVisitsDrilldown(
                id, conceptId, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Returns the summary for the cohort
     *
     * @summary Get cohort summary
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return CohortSummary
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/summarydata
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/summarydata
     */
    @GetMapping(value = "/{sourceKey}/{id}/summarydata", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortSummary> getCohortSummaryData(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        CohortSummary result = cohortResultsService.getCohortSummaryData(id, sourceKey);
        return ok(result);
    }

    /**
     * Queries for cohort analysis death data for the given cohort definition id
     *
     * @summary Get death report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param minCovariatePersonCountParam The minimum number of covariates per person
     * @param minIntervalPersonCountParam The minimum interval person count
     * @param refresh Boolean - refresh visualization data
     * @return CohortDeathData
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/death
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/death
     */
    @GetMapping(value = "/{sourceKey}/{id}/death", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortDeathData> getCohortDeathData(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "min_covariate_person_count", required = false) Integer minCovariatePersonCountParam,
            @RequestParam(value = "min_interval_person_count", required = false) Integer minIntervalPersonCountParam,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        CohortDeathData result = cohortResultsService.getCohortDeathData(
                id, minCovariatePersonCountParam, minIntervalPersonCountParam, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Returns the summary for the cohort
     *
     * @summary Get cohort summary analyses
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return CohortSummary
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/summaryanalyses
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/summaryanalyses
     */
    @GetMapping(value = "/{sourceKey}/{id}/summaryanalyses", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortSummary> getCohortSummaryAnalyses(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        CohortSummary result = cohortResultsService.getCohortSummaryAnalyses(id, sourceKey);
        return ok(result);
    }

    /**
     * Returns breakdown with counts about people in cohort
     *
     * @summary Get cohort breakdown report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return Collection<CohortBreakdown>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/breakdown
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/breakdown
     */
    @GetMapping(value = "/{sourceKey}/{id}/breakdown", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<CohortBreakdown>> getCohortBreakdown(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        Collection<CohortBreakdown> result = cohortResultsService.getCohortBreakdown(id, sourceKey);
        return ok(result);
    }

    /**
     * Returns the count of all members of a generated cohort definition identifier
     *
     * @summary Get cohort member count
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return The cohort count
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/members/count
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/members/count
     */
    @GetMapping(value = "/{sourceKey}/{id}/members/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> getCohortMemberCount(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        Long result = cohortResultsService.getCohortMemberCount(id, sourceKey);
        return ok(result);
    }

    /**
     * Returns all cohort analyses in the results/OHDSI schema for the given cohort_definition_id
     *
     * @summary Get the cohort analysis list for a cohort
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param retrieveFullDetail Boolean - when TRUE, the full analysis details are returned
     * @return List of all cohort analyses and their statuses for the given cohort_definition_id
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}
     */
    @GetMapping(value = "/{sourceKey}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CohortAnalysis>> getCohortAnalysesForCohortDefinition(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "fullDetail", defaultValue = "true") boolean retrieveFullDetail) {

        List<CohortAnalysis> result = cohortResultsService.getCohortAnalysesForCohortDefinition(id, sourceKey, retrieveFullDetail);
        return ok(result);
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
     *
     * Jersey: POST /WebAPI/cohortresults/{sourceKey}/exposurecohortrates
     * Spring MVC: POST /WebAPI/v2/cohortresults/{sourceKey}/exposurecohortrates
     */
    @PostMapping(value = "/{sourceKey}/exposurecohortrates", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<List<ExposureCohortResult>> getExposureOutcomeCohortRates(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ExposureCohortSearch search) {

        List<ExposureCohortResult> result = cohortResultsService.getExposureOutcomeCohortRates(sourceKey, search);
        return ok(result);
    }

    /**
     * Provides a time to event calculation but it is unclear how this works.
     *
     * @summary DO NOT USE
     * @deprecated
     * @param sourceKey The source key
     * @param search The exposure cohort search
     * @return List<TimeToEventResult>
     *
     * Jersey: POST /WebAPI/cohortresults/{sourceKey}/timetoevent
     * Spring MVC: POST /WebAPI/v2/cohortresults/{sourceKey}/timetoevent
     */
    @PostMapping(value = "/{sourceKey}/timetoevent", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<List<TimeToEventResult>> getTimeToEventDrilldown(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ExposureCohortSearch search) {

        List<TimeToEventResult> result = cohortResultsService.getTimeToEventDrilldown(sourceKey, search);
        return ok(result);
    }

    /**
     * Provides a predictor calculation but it is unclear how this works.
     *
     * @summary DO NOT USE
     * @deprecated
     * @param sourceKey The source key
     * @param search The exposure cohort search
     * @return List<PredictorResult>
     *
     * Jersey: POST /WebAPI/cohortresults/{sourceKey}/predictors
     * Spring MVC: POST /WebAPI/v2/cohortresults/{sourceKey}/predictors
     */
    @PostMapping(value = "/{sourceKey}/predictors", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<List<PredictorResult>> getExposureOutcomeCohortPredictors(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody ExposureCohortSearch search) {

        List<PredictorResult> result = cohortResultsService.getExposureOutcomeCohortPredictors(sourceKey, search);
        return ok(result);
    }

    /**
     * Returns heracles heel results (data quality issues) for the given cohort definition id
     *
     * @summary Get HERACLES heel report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param refresh Boolean - refresh visualization data
     * @return List<CohortAttribute>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/heraclesheel
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/heraclesheel
     */
    @GetMapping(value = "/{sourceKey}/{id}/heraclesheel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CohortAttribute>> getHeraclesHeel(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {

        List<CohortAttribute> result = cohortResultsService.getHeraclesHeel(id, sourceKey, refresh);
        return ok(result);
    }

    /**
     * Provides a data completeness report for a cohort
     *
     * @summary Get data completeness report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return List<DataCompletenessAttr>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/datacompleteness
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/datacompleteness
     */
    @GetMapping(value = "/{sourceKey}/{id}/datacompleteness", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DataCompletenessAttr>> getDataCompleteness(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        List<DataCompletenessAttr> result = cohortResultsService.getDataCompleteness(id, sourceKey);
        return ok(result);
    }

    /**
     * Provide an entropy report for a cohort
     *
     * @summary Get entropy report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return List<EntropyAttr>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/entropy
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/entropy
     */
    @GetMapping(value = "/{sourceKey}/{id}/entropy", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntropyAttr>> getEntropy(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        List<EntropyAttr> result = cohortResultsService.getEntropy(id, sourceKey);
        return ok(result);
    }

    /**
     * Provide a full entropy report for a cohort
     *
     * @summary Get full entropy report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @return List<EntropyAttr>
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/allentropy
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/allentropy
     */
    @GetMapping(value = "/{sourceKey}/{id}/allentropy", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntropyAttr>> getAllEntropy(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {

        List<EntropyAttr> result = cohortResultsService.getAllEntropy(id, sourceKey);
        return ok(result);
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
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/healthcareutilization/exposure/{window}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/healthcareutilization/exposure/{window}
     */
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/exposure/{window}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthcareExposureReport> getHealthcareUtilizationExposureReport(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") WindowType window,
            @RequestParam(value = "periodType", defaultValue = "ww") PeriodType periodType) {

        HealthcareExposureReport result = cohortResultsService.getHealthcareUtilizationExposureReport(id, sourceKey, window, periodType);
        return ok(result);
    }

    /**
     * Get the healthcare utilization periods
     *
     * @summary Get healthcare utilization periods
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @return A list of the periods
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/healthcareutilization/periods/{window}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/healthcareutilization/periods/{window}
     */
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/periods/{window}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getHealthcareUtilizationPeriods(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") WindowType window) {

        List<String> result = cohortResultsService.getHealthcareUtilizationPeriods(id, sourceKey, window);
        return ok(result);
    }

    /**
     * Get the healthcare utilization report by window, visit status, period type, visit concept, visit type concept and cost type concept.
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
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/healthcareutilization/visit/{window}/{visitStat}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/healthcareutilization/visit/{window}/{visitStat}
     */
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/visit/{window}/{visitStat}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthcareVisitUtilizationReport> getHealthcareUtilizationVisitReport(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") WindowType window,
            @PathVariable("visitStat") VisitStatType visitStat,
            @RequestParam(value = "periodType", defaultValue = "ww") PeriodType periodType,
            @RequestParam(value = "visitConcept", required = false) Long visitConcept,
            @RequestParam(value = "visitTypeConcept", required = false) Long visitTypeConcept,
            @RequestParam(value = "costTypeConcept", defaultValue = "31968") Long costTypeConcept) {

        HealthcareVisitUtilizationReport result = cohortResultsService.getHealthcareUtilizationVisitReport(
                id, sourceKey, window, visitStat, periodType, visitConcept, visitTypeConcept, costTypeConcept);
        return ok(result);
    }

    /**
     * Get the healthcare utilization summary report by drug and cost type concept
     *
     * @summary Get healthcare utilization drug summary report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param window The time window
     * @param drugTypeConceptId The drug type concept ID
     * @param costTypeConceptId The cost type concept ID
     * @return HealthcareDrugUtilizationSummary
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/healthcareutilization/drug/{window}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/healthcareutilization/drug/{window}
     */
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/drug/{window}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthcareDrugUtilizationSummary> getHealthcareUtilizationDrugSummaryReport(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") WindowType window,
            @RequestParam(value = "drugType", required = false) Long drugTypeConceptId,
            @RequestParam(value = "costType", defaultValue = "31968") Long costTypeConceptId) {

        HealthcareDrugUtilizationSummary result = cohortResultsService.getHealthcareUtilizationDrugSummaryReport(
                id, sourceKey, window, drugTypeConceptId, costTypeConceptId);
        return ok(result);
    }

    /**
     * Get the healthcare utilization detail report by drug and cost type concept
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
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/healthcareutilization/drug/{window}/{drugConceptId}
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/healthcareutilization/drug/{window}/{drugConceptId}
     */
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/drug/{window}/{drugConceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthcareDrugUtilizationDetail> getHealthcareUtilizationDrugDetailReport(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("window") WindowType window,
            @PathVariable("drugConceptId") Long drugConceptId,
            @RequestParam(value = "periodType", defaultValue = "ww") PeriodType periodType,
            @RequestParam(value = "drugType", required = false) Long drugTypeConceptId,
            @RequestParam(value = "costType", defaultValue = "31968") Long costTypeConceptId) {

        HealthcareDrugUtilizationDetail result = cohortResultsService.getHealthcareUtilizationDrugDetailReport(
                id, sourceKey, window, drugConceptId, periodType, drugTypeConceptId, costTypeConceptId);
        return ok(result);
    }

    /**
     * Get the drug type concepts for the selected drug concept ID
     *
     * @summary Get drug types for healthcare utilization report
     * @param id The cohort ID
     * @param sourceKey The source key
     * @param drugConceptId The drug concept ID
     * @return A list of concepts of drug types
     *
     * Jersey: GET /WebAPI/cohortresults/{sourceKey}/{id}/healthcareutilization/drugtypes
     * Spring MVC: GET /WebAPI/v2/cohortresults/{sourceKey}/{id}/healthcareutilization/drugtypes
     */
    @GetMapping(value = "/{sourceKey}/{id}/healthcareutilization/drugtypes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Concept>> getDrugTypes(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey,
            @RequestParam(value = "drugConceptId", required = false) Long drugConceptId) {

        List<Concept> result = cohortResultsService.getDrugTypes(id, sourceKey, drugConceptId);
        return ok(result);
    }
}
