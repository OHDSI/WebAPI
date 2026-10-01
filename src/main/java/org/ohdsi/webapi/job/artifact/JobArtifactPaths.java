package org.ohdsi.webapi.job.artifact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for managing job artifact file locations
 */
public class JobArtifactPaths {

	private static final Logger logger = LoggerFactory.getLogger(JobArtifactPaths.class);
	private static final String ARTIFACT_DIR_NAME = "atlas-job-artifacts";

	/**
	 * Get the base directory for storing job artifacts
	 * Creates the directory if it doesn't exist
	 */
	public static Path getArtifactBaseDirectory() throws IOException {
		String tempDir = System.getProperty("java.io.tmpdir");
		Path baseDir = Paths.get(tempDir, ARTIFACT_DIR_NAME);

		logger.debug("Artifact base directory path: {}", baseDir.toAbsolutePath());

		if (!Files.exists(baseDir)) {
			logger.info("Creating artifact base directory: {}", baseDir.toAbsolutePath());
			Files.createDirectories(baseDir);
			logger.info("Successfully created artifact base directory");
		} else {
			logger.debug("Artifact base directory already exists: {}", baseDir.toAbsolutePath());
		}

		return baseDir;
	}

	/**
	 * Get the path for a specific job's artifact file
	 * @param jobExecutionId the job execution ID
	 * @return the path where the artifact should be stored
	 */
	public static Path getArtifactPath(Long jobExecutionId) throws IOException {
		Path baseDir = getArtifactBaseDirectory();
		Path artifactPath = baseDir.resolve(jobExecutionId + ".zip");
		logger.debug("Artifact path for job {}: {}", jobExecutionId, artifactPath.toAbsolutePath());
		return artifactPath;
	}

	/**
	 * Check if a pre-generated artifact exists for a job
	 * @param jobExecutionId the job execution ID
	 * @return true if the artifact file exists
	 */
	public static boolean artifactExists(Long jobExecutionId) {
		try {
			Path artifactPath = getArtifactPath(jobExecutionId);
			boolean exists = Files.exists(artifactPath) && Files.isRegularFile(artifactPath);

			if (exists) {
				long fileSize = Files.size(artifactPath);
				logger.debug("Artifact exists for job {} at {} ({} bytes)",
					jobExecutionId, artifactPath, fileSize);
			} else {
				logger.debug("Artifact does not exist for job {} at {}",
					jobExecutionId, artifactPath);
			}

			return exists;
		} catch (IOException e) {
			logger.warn("Error checking for artifact existence for job {}: {}",
				jobExecutionId, e.getMessage());
			return false;
		}
	}

	/**
	 * Delete the artifact file for a job if it exists
	 * @param jobExecutionId the job execution ID
	 * @return true if the file was deleted, false otherwise
	 */
	public static boolean deleteArtifact(Long jobExecutionId) {
		try {
			Path artifactPath = getArtifactPath(jobExecutionId);
			if (Files.exists(artifactPath)) {
				Files.delete(artifactPath);
				logger.info("Deleted artifact file for job {} at {}", jobExecutionId, artifactPath);
				return true;
			} else {
				logger.debug("No artifact file to delete for job {}", jobExecutionId);
			}
		} catch (IOException e) {
			logger.error("Failed to delete artifact for job {}: {}",
				jobExecutionId, e.getMessage(), e);
		}
		return false;
	}
}