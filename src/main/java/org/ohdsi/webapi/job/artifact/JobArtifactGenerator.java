package org.ohdsi.webapi.job.artifact;

import org.springframework.batch.core.JobExecution;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Interface for generating downloadable artifacts from job executions
 */
public interface JobArtifactGenerator {

	/**
	 * Check if this generator supports the given job execution
	 *
	 * @param jobExecution the job execution to check
	 * @return true if this generator can handle this job type
	 */
	boolean supports(JobExecution jobExecution);

	/**
	 * Check if an artifact exists for the given job execution
	 *
	 * @param jobExecution the job execution
	 * @return true if an artifact is available
	 */
	boolean hasArtifact(JobExecution jobExecution);

	/**
	 * Get the artifact as a Spring Resource
	 *
	 * @param jobExecution the job execution
	 * @return the artifact resource
	 * @throws IOException if artifact cannot be retrieved
	 */
	Resource getArtifact(JobExecution jobExecution) throws IOException;

	/**
	 * Get the filename for the artifact
	 *
	 * @param jobExecution the job execution
	 * @return the suggested filename for download
	 */
	String getArtifactFilename(JobExecution jobExecution);

	/**
	 * Get the content type of the artifact
	 *
	 * @return MIME type string (e.g., "application/zip", "text/csv")
	 */
	String getContentType();


}