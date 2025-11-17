package org.ohdsi.webapi.mvc.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.report.CDMDashboard;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.ohdsi.webapi.report.CDMDeath;
import org.ohdsi.webapi.report.CDMObservationPeriod;
import org.ohdsi.webapi.report.CDMPersonSummary;
import org.ohdsi.webapi.service.CDMResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

/**
 * Spring MVC version of CDMResultsService
 *
 * Migration Status: Replaces /service/CDMResultsService.java (Jersey)
 * Endpoints: 11 endpoints (9 GET, 2 POST)
 * Complexity: Medium - delegates to existing Jersey service for business logic
 *
 * This controller delegates to the existing Jersey CDMResultsService to preserve
 * all business logic, caching, and job execution functionality. The Jersey service
 * remains as a @Component for dependency injection but endpoints are now exposed
 * via this Spring MVC controller.
 */
@RestController
@RequestMapping("/cdmresults")
public class CDMResultsMvcController extends AbstractMvcController {

    @Autowired
    private CDMResultsService cdmResultsService;

    /**
     * Get the record count and descendant record count for one or more concepts in a single CDM database
     *
     * <p>
     *     This POST request accepts a json array containing one or more concept IDs. (e.g. [201826, 437827])
     * </p>
     *
     * @param sourceKey The unique identifier for a CDM source (e.g. SYNPUF5PCT)
     * @param identifiers List of concept IDs
     * @return A list of concept IDs with their record counts and descendant record counts
     *
     * <p>
     *     [
     *     {
     *         "201826": [
     *             612861,
     *             653173
     *         ]
     *     },
     *     {
     *         "437827": [
     *             224421,
     *             224421
     *         ]
     *     }
     * ]
     * </p>
     *
     * Jersey: POST /WebAPI/cdmresults/{sourceKey}/conceptRecordCount
     * Spring MVC: POST /WebAPI/v2/cdmresults/{sourceKey}/conceptRecordCount
     */
    @PostMapping(value = "/{sourceKey}/conceptRecordCount",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SimpleEntry<Integer, List<Long>>>> getConceptRecordCount(
            @PathVariable("sourceKey") String sourceKey,
            @RequestBody List<Integer> identifiers) {
        List<SimpleEntry<Integer, List<Long>>> result = cdmResultsService.getConceptRecordCount(sourceKey, identifiers);
        return ok(result);
    }

    /**
     * Queries for dashboard report for the sourceKey
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/dashboard
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/dashboard
     *
     * @param sourceKey The source key
     * @return CDMDashboard
     */
    @GetMapping(value = "/{sourceKey}/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CDMDashboard> getDashboard(@PathVariable("sourceKey") String sourceKey) {
        CDMDashboard dashboard = cdmResultsService.getDashboard(sourceKey);
        return ok(dashboard);
    }

    /**
     * Queries for person report for the sourceKey
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/person
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/person
     *
     * @param sourceKey The source key
     * @return CDMPersonSummary
     */
    @GetMapping(value = "/{sourceKey}/person", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CDMPersonSummary> getPerson(@PathVariable("sourceKey") String sourceKey) {
        CDMPersonSummary person = cdmResultsService.getPerson(sourceKey);
        return ok(person);
    }

    /**
     * Warm the results cache for a selected source
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/warmCache
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/warmCache
     *
     * @summary Warm cache for source key
     * @param sourceKey The source key
     * @return The job execution information
     */
    @GetMapping(value = "/{sourceKey}/warmCache", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobExecutionResource> warmCache(@PathVariable("sourceKey") String sourceKey) {
        JobExecutionResource jobExecution = cdmResultsService.warmCache(sourceKey);
        return ok(jobExecution);
    }

    /**
     * Refresh the results cache for a selected source
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/refreshCache
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/refreshCache
     *
     * @summary Refresh results cache
     * @param sourceKey The source key
     * @return The job execution resource
     */
    @GetMapping(value = "/{sourceKey}/refreshCache", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobExecutionResource> refreshCache(@PathVariable("sourceKey") String sourceKey) {
        JobExecutionResource jobExecution = cdmResultsService.refreshCache(sourceKey);
        return ok(jobExecution);
    }

    /**
     * Clear the cdm_cache and achilles_cache for a specific source
     *
     * Jersey: POST /WebAPI/cdmresults/{sourceKey}/clearCache
     * Spring MVC: POST /WebAPI/v2/cdmresults/{sourceKey}/clearCache
     *
     * @summary Clear the cdm_cache and achilles_cache for a source
     * @param sourceKey The source key
     * @return void
     */
    @PostMapping(value = "/{sourceKey}/clearCache")
    public ResponseEntity<Void> clearCacheForSource(@PathVariable("sourceKey") String sourceKey) {
        if (!isSecured() || !isAdmin()) {
            return forbidden();
        }
        cdmResultsService.clearCacheForSource(sourceKey);
        return ok();
    }

    /**
     * Clear the cdm_cache and achilles_cache for all sources
     *
     * Jersey: POST /WebAPI/cdmresults/clearCache
     * Spring MVC: POST /WebAPI/v2/cdmresults/clearCache
     *
     * @summary Clear the cdm_cache and achilles_cache for all sources
     * @return void
     */
    @PostMapping(value = "/clearCache")
    public ResponseEntity<Void> clearCache() {
        if (!isSecured() || !isAdmin()) {
            return forbidden();
        }
        cdmResultsService.clearCache();
        return ok();
    }

    /**
     * Queries for data density report for the given sourceKey
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/datadensity
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/datadensity
     *
     * @param sourceKey The source key
     * @return CDMDataDensity
     */
    @GetMapping(value = "/{sourceKey}/datadensity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CDMDataDensity> getDataDensity(@PathVariable("sourceKey") String sourceKey) {
        CDMDataDensity dataDensity = cdmResultsService.getDataDensity(sourceKey);
        return ok(dataDensity);
    }

    /**
     * Queries for death report for the given sourceKey
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/death
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/death
     *
     * @param sourceKey The source key
     * @return CDMDeath
     */
    @GetMapping(value = "/{sourceKey}/death", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CDMDeath> getDeath(@PathVariable("sourceKey") String sourceKey) {
        CDMDeath death = cdmResultsService.getDeath(sourceKey);
        return ok(death);
    }

    /**
     * Queries for observation period report for the given sourceKey
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/observationPeriod
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/observationPeriod
     *
     * @param sourceKey The source key
     * @return CDMObservationPeriod
     */
    @GetMapping(value = "/{sourceKey}/observationPeriod", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CDMObservationPeriod> getObservationPeriod(@PathVariable("sourceKey") String sourceKey) {
        CDMObservationPeriod observationPeriod = cdmResultsService.getObservationPeriod(sourceKey);
        return ok(observationPeriod);
    }

    /**
     * Queries for domain treemap results
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/{domain}/
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/{domain}/
     *
     * @param sourceKey The source key
     * @param domain The domain
     * @return List<ArrayNode>
     */
    @GetMapping(value = "/{sourceKey}/{domain}/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayNode> getTreemap(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("domain") String domain) {
        ArrayNode treemap = cdmResultsService.getTreemap(domain, sourceKey);
        return ok(treemap);
    }

    /**
     * Queries for drilldown results
     *
     * Jersey: GET /WebAPI/cdmresults/{sourceKey}/{domain}/{conceptId}
     * Spring MVC: GET /WebAPI/v2/cdmresults/{sourceKey}/{domain}/{conceptId}
     *
     * @param sourceKey The source key
     * @param domain The domain for the drilldown
     * @param conceptId The concept ID
     * @return The JSON results
     */
    @GetMapping(value = "/{sourceKey}/{domain}/{conceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> getDrilldown(
            @PathVariable("sourceKey") String sourceKey,
            @PathVariable("domain") String domain,
            @PathVariable("conceptId") int conceptId) {
        JsonNode drilldown = cdmResultsService.getDrilldown(domain, conceptId, sourceKey);
        return ok(drilldown);
    }
}
