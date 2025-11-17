package org.ohdsi.webapi.mvc.controller;

import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobInstanceResource;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Spring MVC version of JobService
 *
 * Migration Status: Replaces /service/JobService.java (Jersey)
 * Endpoints: 6 GET endpoints
 * Complexity: Simple - mostly delegation to service layer
 */
@RestController
@RequestMapping("/job")
public class JobMvcController extends AbstractMvcController {

    private final org.ohdsi.webapi.service.JobService jobService;

    public JobMvcController(org.ohdsi.webapi.service.JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Get the job information by job ID
     *
     * Jersey: GET /WebAPI/job/{jobId}
     * Spring MVC: GET /WebAPI/v2/job/{jobId}
     *
     * @summary Get job by ID
     * @param jobId The job ID
     * @return The job information
     */
    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobInstanceResource> findJob(@PathVariable("jobId") Long jobId) {
        JobInstanceResource job = jobService.findJob(jobId);
        if (job == null) {
            return notFound();
        }
        return ok(job);
    }

    /**
     * Get the job execution information by job type and name
     *
     * Jersey: GET /WebAPI/job/type/{jobType}/name/{jobName}
     * Spring MVC: GET /WebAPI/v2/job/type/{jobType}/name/{jobName}
     *
     * @summary Get job by name and type
     * @param jobName The job name
     * @param jobType The job type
     * @return JobExecutionResource
     */
    @GetMapping(value = "/type/{jobType}/name/{jobName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobExecutionResource> findJobByName(
            @PathVariable("jobName") String jobName,
            @PathVariable("jobType") String jobType) {
        JobExecutionResource jobExecution = jobService.findJobByName(jobName, jobType);
        if (jobExecution == null) {
            return notFound();
        }
        return ok(jobExecution);
    }

    /**
     * Get the job execution information by execution ID and job ID
     *
     * Jersey: GET /WebAPI/job/{jobId}/execution/{executionId}
     * Spring MVC: GET /WebAPI/v2/job/{jobId}/execution/{executionId}
     *
     * @summary Get job by job ID and execution ID
     * @param jobId The job ID
     * @param executionId The execution ID
     * @return JobExecutionResource
     */
    @GetMapping(value = "/{jobId}/execution/{executionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobExecutionResource> findJobExecutionByJobId(
            @PathVariable("jobId") Long jobId,
            @PathVariable("executionId") Long executionId) {
        JobExecutionResource jobExecution = jobService.findJobExecution(jobId, executionId);
        if (jobExecution == null) {
            return notFound();
        }
        return ok(jobExecution);
    }

    /**
     * Find job execution by execution ID
     *
     * Jersey: GET /WebAPI/job/execution/{executionId}
     * Spring MVC: GET /WebAPI/v2/job/execution/{executionId}
     *
     * @summary Get job by execution ID
     * @param executionId The job execution ID
     * @return JobExecutionResource
     */
    @GetMapping(value = "/execution/{executionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobExecutionResource> findJobExecution(@PathVariable("executionId") Long executionId) {
        JobExecutionResource jobExecution = jobService.findJobExecution(executionId);
        if (jobExecution == null) {
            return notFound();
        }
        return ok(jobExecution);
    }

    /**
     * Get job names (unique names). Note: this path (GET /job) should really
     * return pages of job instances. This could be implemented should the need
     * arise.
     *
     * Jersey: GET /WebAPI/job
     * Spring MVC: GET /WebAPI/v2/job
     *
     * @summary Get list of jobs
     * @return A list of jobs
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> findJobNames() {
        List<String> jobNames = jobService.findJobNames();
        return ok(jobNames);
    }

    /**
     * Return a paged collection of job executions. Filter for a given job.
     * Returned in pages.
     *
     * Jersey: GET /WebAPI/job/execution?jobName={jobName}&pageIndex={pageIndex}&pageSize={pageSize}&comprehensivePage={comprehensivePage}
     * Spring MVC: GET /WebAPI/v2/job/execution?jobName={jobName}&pageIndex={pageIndex}&pageSize={pageSize}&comprehensivePage={comprehensivePage}
     *
     * @summary Get job executions with filters
     * @param jobName name of the job
     * @param pageIndex start index for the job execution list
     * @param pageSize page size for the list
     * @param comprehensivePage boolean if true returns a comprehensive resultset
     * as a page (i.e. pageRequest(0,resultset.size()))
     * @return collection of JobExecutionInfo
     * @throws NoSuchJobException
     */
    @GetMapping(value = "/execution", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<JobExecutionResource>> list(
            @RequestParam(value = "jobName", required = false) String jobName,
            @RequestParam(value = "pageIndex", defaultValue = "0") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "comprehensivePage", required = false, defaultValue = "false") boolean comprehensivePage)
            throws NoSuchJobException {
        Page<JobExecutionResource> page = jobService.list(jobName, pageIndex, pageSize, comprehensivePage);
        return ok(page);
    }
}
