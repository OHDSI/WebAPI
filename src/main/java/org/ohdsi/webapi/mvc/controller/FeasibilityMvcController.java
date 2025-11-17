package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.feasibility.FeasibilityReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.service.FeasibilityService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Spring MVC version of FeasibilityService
 *
 * Migration Status: Replaces /service/FeasibilityService.java (Jersey)
 * Endpoints: 9 endpoints (4 GET, 2 PUT, 2 DELETE, 1 GET with generation)
 * Complexity: High - complex business logic, all endpoints marked as deprecated
 */
@RestController
@RequestMapping("/feasibility")
public class FeasibilityMvcController extends AbstractMvcController {

    private final FeasibilityService feasibilityService;

    public FeasibilityMvcController(FeasibilityService feasibilityService) {
        this.feasibilityService = feasibilityService;
    }

    /**
     * DO NOT USE
     *
     * Jersey: GET /WebAPI/feasibility/
     * Spring MVC: GET /WebAPI/v2/feasibility
     *
     * @summary DO NOT USE
     * @deprecated
     * @return List<FeasibilityService.FeasibilityStudyListItem>
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<List<FeasibilityService.FeasibilityStudyListItem>> getFeasibilityStudyList() {
        List<FeasibilityService.FeasibilityStudyListItem> studies = feasibilityService.getFeasibilityStudyList();
        return ok(studies);
    }

    /**
     * Creates the feasibility study
     *
     * Jersey: PUT /WebAPI/feasibility/
     * Spring MVC: PUT /WebAPI/v2/feasibility
     *
     * @summary DO NOT USE
     * @deprecated
     * @param study The feasibility study
     * @return Feasibility study
     */
    @PutMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    @Deprecated
    public ResponseEntity<FeasibilityService.FeasibilityStudyDTO> createStudy(
            @RequestBody FeasibilityService.FeasibilityStudyDTO study) {
        FeasibilityService.FeasibilityStudyDTO createdStudy = feasibilityService.createStudy(study);
        return ok(createdStudy);
    }

    /**
     * Get the feasibility study by ID
     *
     * Jersey: GET /WebAPI/feasibility/{id}
     * Spring MVC: GET /WebAPI/v2/feasibility/{id}
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id The study ID
     * @return Feasibility study
     */
    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional(readOnly = true)
    @Deprecated
    public ResponseEntity<FeasibilityService.FeasibilityStudyDTO> getStudy(@PathVariable("id") int id) {
        FeasibilityService.FeasibilityStudyDTO study = feasibilityService.getStudy(id);
        return ok(study);
    }

    /**
     * Update the feasibility study
     *
     * Jersey: PUT /WebAPI/feasibility/{id}
     * Spring MVC: PUT /WebAPI/v2/feasibility/{id}
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id The study ID
     * @param study The study information
     * @return The updated study information
     */
    @PutMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    @Deprecated
    public ResponseEntity<FeasibilityService.FeasibilityStudyDTO> saveStudy(
            @PathVariable("id") int id,
            @RequestBody FeasibilityService.FeasibilityStudyDTO study) {
        FeasibilityService.FeasibilityStudyDTO savedStudy = feasibilityService.saveStudy(id, study);
        return ok(savedStudy);
    }

    /**
     * Generate the feasibility study
     *
     * Jersey: GET /WebAPI/feasibility/{study_id}/generate/{sourceKey}
     * Spring MVC: GET /WebAPI/v2/feasibility/{study_id}/generate/{sourceKey}
     *
     * @summary DO NOT USE
     * @deprecated
     * @param study_id The study ID
     * @param sourceKey The source key
     * @return JobExecutionResource
     */
    @GetMapping(
        value = "/{study_id}/generate/{sourceKey}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Deprecated
    public ResponseEntity<JobExecutionResource> performStudy(
            @PathVariable("study_id") int study_id,
            @PathVariable("sourceKey") String sourceKey) {
        JobExecutionResource jobExecution = feasibilityService.performStudy(study_id, sourceKey);
        return ok(jobExecution);
    }

    /**
     * Get simulation information
     *
     * Jersey: GET /WebAPI/feasibility/{id}/info
     * Spring MVC: GET /WebAPI/v2/feasibility/{id}/info
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id The study ID
     * @return List<StudyInfoDTO>
     */
    @GetMapping(
        value = "/{id}/info",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional(readOnly = true)
    @Deprecated
    public ResponseEntity<List<FeasibilityService.StudyInfoDTO>> getSimulationInfo(@PathVariable("id") int id) {
        List<FeasibilityService.StudyInfoDTO> info = feasibilityService.getSimulationInfo(id);
        return ok(info);
    }

    /**
     * Get simulation report
     *
     * Jersey: GET /WebAPI/feasibility/{id}/report/{sourceKey}
     * Spring MVC: GET /WebAPI/v2/feasibility/{id}/report/{sourceKey}
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id The study ID
     * @param sourceKey The source key
     * @return FeasibilityReport
     */
    @GetMapping(
        value = "/{id}/report/{sourceKey}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    @Deprecated
    public ResponseEntity<FeasibilityReport> getSimulationReport(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {
        FeasibilityReport report = feasibilityService.getSimulationReport(id, sourceKey);
        return ok(report);
    }

    /**
     * Copies the specified cohort definition
     *
     * Jersey: GET /WebAPI/feasibility/{id}/copy
     * Spring MVC: GET /WebAPI/v2/feasibility/{id}/copy
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id - the Cohort Definition ID to copy
     * @return the copied feasibility study as a FeasibilityStudyDTO
     */
    @GetMapping(
        value = "/{id}/copy",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @jakarta.transaction.Transactional
    @Deprecated
    public ResponseEntity<FeasibilityService.FeasibilityStudyDTO> copy(@PathVariable("id") int id) {
        FeasibilityService.FeasibilityStudyDTO copiedStudy = feasibilityService.copy(id);
        return ok(copiedStudy);
    }

    /**
     * Deletes the specified feasibility study
     *
     * Jersey: DELETE /WebAPI/feasibility/{id}
     * Spring MVC: DELETE /WebAPI/v2/feasibility/{id}
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id The study ID
     */
    @DeleteMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Deprecated
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        feasibilityService.delete(id);
        return ok();
    }

    /**
     * Deletes the specified study for the selected source
     *
     * Jersey: DELETE /WebAPI/feasibility/{id}/info/{sourceKey}
     * Spring MVC: DELETE /WebAPI/v2/feasibility/{id}/info/{sourceKey}
     *
     * @summary DO NOT USE
     * @deprecated
     * @param id The study ID
     * @param sourceKey The source key
     */
    @DeleteMapping(
        value = "/{id}/info/{sourceKey}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    @Deprecated
    public ResponseEntity<Void> deleteInfo(
            @PathVariable("id") int id,
            @PathVariable("sourceKey") String sourceKey) {
        feasibilityService.deleteInfo(id, sourceKey);
        return ok();
    }
}
