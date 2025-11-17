package org.ohdsi.webapi.user.importer;

import org.ohdsi.webapi.arachne.scheduler.exception.JobNotFoundException;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.user.importer.dto.JobHistoryItemDTO;
import org.ohdsi.webapi.user.importer.dto.UserImportJobDTO;
import org.ohdsi.webapi.user.importer.exception.JobAlreadyExistException;
import org.ohdsi.webapi.user.importer.model.UserImportJob;
import org.ohdsi.webapi.user.importer.service.UserImportJobService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.JOB_IS_ALREADY_SCHEDULED;

/**
 * REST Services related to importing user information
 * from an external source (i.e. Active Directory)
 *
 * Spring MVC version of UserImportJobController
 *
 * Migration Status: Replaces /user/importer/UserImportJobController.java (Jersey)
 * Endpoints: 5 endpoints (GET, POST, PUT, DELETE)
 * Complexity: Medium - user import job management with history
 */
@RestController
@RequestMapping("/user/import/job")
@Transactional
public class UserImportJobMvcController extends AbstractMvcController {

    private final UserImportJobService jobService;
    private final GenericConversionService conversionService;

    public UserImportJobMvcController(
            UserImportJobService jobService,
            @Qualifier("conversionService") GenericConversionService conversionService) {
        this.jobService = jobService;
        this.conversionService = conversionService;
    }

    /**
     * Create a user import job
     *
     * Jersey: POST /WebAPI/user/import/job/
     * Spring MVC: POST /WebAPI/v2/user/import/job/
     *
     * @param jobDTO The user import information
     * @return The job information
     */
    @PostMapping(
        value = "/",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserImportJobDTO> createJob(@RequestBody UserImportJobDTO jobDTO) {
        UserImportJob job = conversionService.convert(jobDTO, UserImportJob.class);
        try {
            UserImportJob created = jobService.createJob(job);
            return ok(conversionService.convert(created, UserImportJobDTO.class));
        } catch (JobAlreadyExistException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    String.format(JOB_IS_ALREADY_SCHEDULED, job.getProviderType()));
        }
    }

    /**
     * Update a user import job
     *
     * Jersey: PUT /WebAPI/user/import/job/{id}
     * Spring MVC: PUT /WebAPI/v2/user/import/job/{id}
     *
     * @param jobId The job ID
     * @param jobDTO The user import information
     * @return The job information
     */
    @PutMapping(
        value = "/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserImportJobDTO> updateJob(
            @PathVariable("id") Long jobId,
            @RequestBody UserImportJobDTO jobDTO) {
        UserImportJob job = conversionService.convert(jobDTO, UserImportJob.class);
        try {
            job.setId(jobId);
            UserImportJob updated = jobService.updateJob(job);
            return ok(conversionService.convert(updated, UserImportJobDTO.class));
        } catch (JobNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get the user import job list
     *
     * Jersey: GET /WebAPI/user/import/job/
     * Spring MVC: GET /WebAPI/v2/user/import/job/
     *
     * @return The list of user import jobs
     */
    @GetMapping(
        value = "/",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Transactional
    public ResponseEntity<List<UserImportJobDTO>> listJobs() {
        return ok(jobService.getJobs().stream()
                .map(job -> conversionService.convert(job, UserImportJobDTO.class))
                .peek(job -> jobService.getLatestHistoryItem(job.getId())
                        .ifPresent(item -> job.setLastExecuted(item.getEndTime())))
                .collect(Collectors.toList()));
    }

    /**
     * Get user import job by ID
     *
     * Jersey: GET /WebAPI/user/import/job/{id}
     * Spring MVC: GET /WebAPI/v2/user/import/job/{id}
     *
     * @param id The job ID
     * @return The user import job
     */
    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserImportJobDTO> getJob(@PathVariable("id") Long id) {
        return jobService.getJob(id)
                .map(job -> conversionService.convert(job, UserImportJobDTO.class))
                .map(this::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Delete user import job by ID
     *
     * Jersey: DELETE /WebAPI/user/import/job/{id}
     * Spring MVC: DELETE /WebAPI/v2/user/import/job/{id}
     *
     * @param id The job ID
     */
    @DeleteMapping(
        value = "/{id}"
    )
    public ResponseEntity<Void> deleteJob(@PathVariable("id") Long id) {
        UserImportJob job = jobService.getJob(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        jobService.delete(job);
        return ok();
    }

    /**
     * Get the user import job history
     *
     * Jersey: GET /WebAPI/user/import/job/{id}/history
     * Spring MVC: GET /WebAPI/v2/user/import/job/{id}/history
     *
     * @param id The job ID
     * @return The job history
     */
    @GetMapping(
        value = "/{id}/history",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<JobHistoryItemDTO>> getImportHistory(@PathVariable("id") Long id) {
        return ok(jobService.getJobHistoryItems(id)
                .map(item -> conversionService.convert(item, JobHistoryItemDTO.class))
                .collect(Collectors.toList()));
    }
}
