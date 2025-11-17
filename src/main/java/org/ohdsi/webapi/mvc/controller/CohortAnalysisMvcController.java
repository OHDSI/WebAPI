package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.cohortanalysis.*;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.model.results.Analysis;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Spring MVC version of CohortAnalysisService
 *
 * Migration Status: Replaces /service/CohortAnalysisService.java (Jersey)
 * Endpoints: 5 endpoints (3 GET, 2 POST)
 * Complexity: Medium - business logic delegated to original service
 */
@RestController
@RequestMapping("/cohortanalysis")
public class CohortAnalysisMvcController extends AbstractMvcController {

    private final org.ohdsi.webapi.service.CohortAnalysisService cohortAnalysisService;

    public CohortAnalysisMvcController(org.ohdsi.webapi.service.CohortAnalysisService cohortAnalysisService) {
        this.cohortAnalysisService = cohortAnalysisService;
    }

    /**
     * Returns all cohort analyses in the WebAPI database
     *
     * Jersey: GET /WebAPI/cohortanalysis/
     * Spring MVC: GET /WebAPI/v2/cohortanalysis
     *
     * @summary Get all cohort analyses
     * @return List of all cohort analyses
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Analysis>> getCohortAnalyses() {
        List<Analysis> analyses = cohortAnalysisService.getCohortAnalyses();
        return ok(analyses);
    }

    /**
     * Returns all cohort analyses in the WebAPI database
     * for the given cohort_definition_id
     *
     * Jersey: GET /WebAPI/cohortanalysis/{id}
     * Spring MVC: GET /WebAPI/v2/cohortanalysis/{id}
     *
     * @summary Get cohort analyses by cohort ID
     * @param id The cohort definition identifier
     * @return List of all cohort analyses and their statuses
     * for the given cohort_definition_id
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CohortAnalysis>> getCohortAnalysesForCohortDefinition(@PathVariable("id") int id) {
        List<CohortAnalysis> analyses = cohortAnalysisService.getCohortAnalysesForCohortDefinition(id);
        return ok(analyses);
    }

    /**
     * Returns the summary for the cohort
     *
     * Jersey: GET /WebAPI/cohortanalysis/{id}/summary
     * Spring MVC: GET /WebAPI/v2/cohortanalysis/{id}/summary
     *
     * @summary Cohort analysis summary
     * @param id - the cohort_definition id
     * @return Summary which includes the base cohort_definition, the cohort analyses list and their
     *         statuses for this cohort, and a base set of common cohort results that may or may not
     *         yet have been ran
     */
    @GetMapping(value = "/{id}/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CohortSummary> getCohortSummary(@PathVariable("id") int id) {
        CohortSummary summary = cohortAnalysisService.getCohortSummary(id);
        return ok(summary);
    }

    /**
     * Generates a preview of the cohort analysis SQL used to run
     * the Cohort Analysis Job
     *
     * Jersey: POST /WebAPI/cohortanalysis/preview
     * Spring MVC: POST /WebAPI/v2/cohortanalysis/preview
     *
     * @summary Cohort analysis SQL preview
     * @param task - the CohortAnalysisTask, be sure to have a least one
     * analysis_id and one cohort_definition id
     * @return - SQL for the given CohortAnalysisTask translated and rendered to
     * the current dialect
     */
    @PostMapping(
        value = "/preview",
        produces = MediaType.TEXT_PLAIN_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getRunCohortAnalysisSql(@RequestBody CohortAnalysisTask task) {
        String sql = cohortAnalysisService.getRunCohortAnalysisSql(task);
        return ok(sql);
    }

    /**
     * Queues up a cohort analysis task, that generates and translates SQL for the
     * given cohort definitions, analysis ids and concept ids
     *
     * Jersey: POST /WebAPI/cohortanalysis/
     * Spring MVC: POST /WebAPI/v2/cohortanalysis
     *
     * @summary Queue cohort analysis job
     * @param task The cohort analysis task to be ran
     * @return information about the Cohort Analysis Job
     * @throws Exception
     */
    @PostMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<JobExecutionResource> queueCohortAnalysisJob(@RequestBody CohortAnalysisTask task) throws Exception {
        JobExecutionResource jobExecution = cohortAnalysisService.queueCohortAnalysisJob(task);
        if (jobExecution == null) {
            return notFound();
        }
        return ok(jobExecution);
    }
}
