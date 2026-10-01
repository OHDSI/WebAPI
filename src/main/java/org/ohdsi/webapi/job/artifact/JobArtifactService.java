package org.ohdsi.webapi.job.artifact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for handling job artifact operations including pre-generated artifact downloads
 */
@Service
public class JobArtifactService {

	private static final Logger logger = LoggerFactory.getLogger(JobArtifactService.class);
	private static final int BUFFER_SIZE = 8192;

	private final JobExplorer jobExplorer;
	private final JobArtifactGeneratorFactory artifactGeneratorFactory;

	public JobArtifactService(
		JobExplorer jobExplorer,
		JobArtifactGeneratorFactory artifactGeneratorFactory) {
		this.jobExplorer = jobExplorer;
		this.artifactGeneratorFactory = artifactGeneratorFactory;
	}

	/**
	 * Main entry point for downloading job artifacts
	 * Checks for pre-generated artifacts first, falls back to on-demand generation
	 *
	 * @param executionId the job execution ID
	 * @return Response containing the artifact or appropriate error status
	 */
	public Response downloadArtifact(Long executionId) {
		try {
			logger.info("Processing artifact download for job execution ID: {}", executionId);

			// First, check if a pre-generated artifact exists
			if (hasPreGeneratedArtifact(executionId)) {
				logger.info("Found pre-generated artifact for job execution {}", executionId);
				return downloadPreGeneratedArtifact(executionId);
			}

			logger.info("No pre-generated artifact found, generating on-demand for job execution {}", executionId);
			return generateAndDownloadArtifact(executionId);

		} catch (Exception e) {
			logger.error("Unexpected error while processing artifact for execution {}", executionId, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Generate artifact on-demand and stream to client
	 */
	private Response generateAndDownloadArtifact(Long executionId) {
		try {
			// Retrieve job execution
			JobExecution jobExecution = jobExplorer.getJobExecution(executionId);

			if (jobExecution == null) {
				logger.warn("Job execution not found: {}", executionId);
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			// Get appropriate artifact generator for this job
			JobArtifactGenerator generator = artifactGeneratorFactory.getGenerator(jobExecution);

			if (generator == null) {
				logger.warn("No artifact generator found for job: {}",
					jobExecution.getJobInstance().getJobName());
				return Response.status(Response.Status.NOT_IMPLEMENTED).build();
			}

			// Check if artifact exists
			if (!generator.hasArtifact(jobExecution)) {
				logger.info("No artifact available for job execution: {}", executionId);
				return Response.noContent().build();
			}

			// Generate/retrieve the artifact
			Resource resource = generator.getArtifact(jobExecution);

			if (resource == null || !resource.exists()) {
				logger.warn("Artifact resource not found or does not exist for execution: {}", executionId);
				return Response.noContent().build();
			}

			String filename = generator.getArtifactFilename(jobExecution);
			String contentType = generator.getContentType();

			logger.info("Successfully prepared on-demand artifact '{}' for download", filename);

			return createStreamingResponse(resource, filename, contentType, "on-demand").build();

		} catch (IOException e) {
			logger.error("IOException while generating artifact for execution {}", executionId, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Download a pre-generated artifact from the file system
	 */
	private Response downloadPreGeneratedArtifact(Long executionId) {
		try {
			logger.info("Attempting to download pre-generated artifact for job execution {}", executionId);

			Path artifactPath = JobArtifactPaths.getArtifactPath(executionId);

			if (!Files.exists(artifactPath) || !Files.isRegularFile(artifactPath)) {
				logger.warn("Pre-generated artifact does not exist or is not a file: {}", artifactPath);
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			long fileSize = Files.size(artifactPath);
			logger.info("Found pre-generated artifact at {} with size {} bytes", artifactPath, fileSize);

			Resource resource = new FileSystemResource(artifactPath.toFile());

			if (!resource.exists() || !resource.isReadable()) {
				logger.error("Artifact resource exists but is not readable: {}", artifactPath);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

			String filename = String.format("job_%d_artifact.zip", executionId);
			String contentType = "application/zip";

			logger.info("Serving pre-generated artifact '{}' from {}", filename, artifactPath);

		return 	Response
				.ok(Files.newInputStream(artifactPath))
				.header(HttpHeaders.CONTENT_TYPE, "application/zip")
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.build();
//			byte[] data = Files.readAllBytes(artifactPath);
//
//			return Response.ok(data, contentType)
//				.header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
//				.header("Content-Length", data.length)
//				.header("X-Atlas-Artifact-Source", "pre-generated-direct")
//				.build();
//			return createStreamingResponse(resource, filename, contentType, "pre-generated")
//				.header("Content-Length", fileSize)
//				.build();

		} catch (IOException e) {
			logger.error("IOException while accessing pre-generated artifact for execution {}", executionId, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Create a streaming response for the artifact resource
	 */
	private Response.ResponseBuilder createStreamingResponse(
		Resource resource,
		String filename,
		String contentType,
		String source) {

		StreamingOutput stream = output -> {
			try (InputStream input = resource.getInputStream()) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead;
				long totalBytesWritten = 0;

				while ((bytesRead = input.read(buffer)) != -1) {
					output.write(buffer, 0, bytesRead);
					totalBytesWritten += bytesRead;
				}
				output.flush();

				logger.debug("Streamed {} bytes for artifact {}", totalBytesWritten, filename);
			} catch (IOException e) {
				logger.error("Error streaming artifact {}: {}", filename, e.getMessage(), e);
				throw e;
			}
		};

		return Response.ok(stream, contentType)
			.header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
			.header("X-Atlas-Artifact-Source", source);
	}

	/**
	 * Check if a pre-generated artifact is available for download
	 */
	public boolean hasPreGeneratedArtifact(Long executionId) {
		return JobArtifactPaths.artifactExists(executionId);
	}

	/**
	 * Get information about a pre-generated artifact
	 *
	 * @param executionId the job execution ID
	 * @return artifact info or null if not found
	 */
	public ArtifactInfo getArtifactInfo(Long executionId) {
		try {
			if (!JobArtifactPaths.artifactExists(executionId)) {
				return null;
			}

			Path artifactPath = JobArtifactPaths.getArtifactPath(executionId);
			long fileSize = Files.size(artifactPath);
			long lastModified = Files.getLastModifiedTime(artifactPath).toMillis();

			return new ArtifactInfo(executionId, fileSize, lastModified, artifactPath.toString());
		} catch (IOException e) {
			logger.error("Error getting artifact info for job {}: {}", executionId, e.getMessage());
			return null;
		}
	}

	/**
	 * Container for artifact metadata
	 */
	public static class ArtifactInfo {
		private final Long executionId;
		private final long fileSizeBytes;
		private final long lastModifiedMillis;
		private final String path;

		public ArtifactInfo(Long executionId, long fileSizeBytes, long lastModifiedMillis, String path) {
			this.executionId = executionId;
			this.fileSizeBytes = fileSizeBytes;
			this.lastModifiedMillis = lastModifiedMillis;
			this.path = path;
		}

		public Long getExecutionId() {
			return executionId;
		}

		public long getFileSizeBytes() {
			return fileSizeBytes;
		}

		public long getLastModifiedMillis() {
			return lastModifiedMillis;
		}

		public String getPath() {
			return path;
		}
	}
}