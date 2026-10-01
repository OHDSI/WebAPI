package org.ohdsi.webapi.job;

import org.ohdsi.webapi.job.artifact.JobArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller for managing job execution artifacts (reports, results, etc.)
 */
@Path("/job")
@Component
public class JobArtifactController {

	private static final Logger logger = LoggerFactory.getLogger(JobArtifactController.class);

	private final JobArtifactService artifactService;

	public JobArtifactController(JobArtifactService artifactService) {
		this.artifactService = artifactService;
	}

	/**
	 * Download artifact for a specific job execution
	 *
	 * @param executionId the job execution ID
	 * @return Response containing the artifact file
	 */
	@GET
	@Path("/{executionId}/artifact")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Transactional
	public Response downloadArtifact(@PathParam("executionId") Long executionId) {
		logger.info("Artifact download requested for job execution ID: {}", executionId);
		return artifactService.downloadArtifact(executionId);
	}
}