package org.ohdsi.webapi.service.cscompare;

import org.ohdsi.webapi.job.artifact.ConceptSetBatchCompareArtifactGenerator;
import org.ohdsi.webapi.job.artifact.JobArtifactPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class ConceptSetBatchCompareJobListener implements JobExecutionListener {

	private static final Logger log = LoggerFactory.getLogger(ConceptSetBatchCompareJobListener.class);

	private final ConceptSetBatchCompareArtifactGenerator artifactGenerator;

	public ConceptSetBatchCompareJobListener(ConceptSetBatchCompareArtifactGenerator artifactGenerator) {
		this.artifactGenerator = artifactGenerator;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("Starting batch compare job with execution ID: {}", jobExecution.getId());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		Long executionId = jobExecution.getId();

		log.info("=== ARTIFACT GENERATION START for execution {} ===", executionId);
		log.info("Job Status: {}, Exit Status: {}",
			jobExecution.getStatus(),
			jobExecution.getExitStatus().getExitCode());

		// Only generate artifact for successful jobs
		if (jobExecution.getStatus().isUnsuccessful()) {
			log.warn("Skipping artifact generation for unsuccessful job execution {}", executionId);
			log.info("=== ARTIFACT GENERATION END for execution {} ===", executionId);
			return;
		}

		try {
			generateArtifact(jobExecution);
		} catch (Exception e) {
			log.error("Failed to generate artifact for job execution {}", executionId, e);
			// Don't throw - let the job complete successfully even if artifact generation fails
		}

		log.info("=== ARTIFACT GENERATION END for execution {} ===", executionId);
	}

	private void generateArtifact(JobExecution jobExecution) throws Exception {
		Long executionId = jobExecution.getId();

		if (!artifactGenerator.supports(jobExecution)) {
			log.warn("Artifact generator does not support job execution {}", executionId);
			return;
		}

		if (!artifactGenerator.hasArtifact(jobExecution)) {
			log.info("No artifact to generate for job execution {}", executionId);
			return;
		}

		Path targetPath = JobArtifactPaths.getArtifactPath(executionId);
		log.info("Target artifact path: {}", targetPath.toAbsolutePath());

		// Ensure parent directory exists
		Path parentDir = targetPath.getParent();
		if (parentDir != null && !Files.exists(parentDir)) {
			Files.createDirectories(parentDir);
			log.info("Created artifact directory: {}", parentDir.toAbsolutePath());
		}

		// Generate the artifact
		log.info("Generating artifact...");
		long startTime = System.currentTimeMillis();

		Resource artifactResource = artifactGenerator.getArtifact(jobExecution);

		if (artifactResource == null || !artifactResource.exists()) {
			log.error("Generated artifact resource is null or does not exist");
			return;
		}

		// Copy the artifact to the target location
		log.info("Copying artifact to target location...");
		try (InputStream inputStream = artifactResource.getInputStream()) {
			Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

			if (Files.exists(targetPath)) {
				long fileSize = Files.size(targetPath);
				long totalTime = System.currentTimeMillis() - startTime;
				log.info("SUCCESS: Artifact created at: {} ({} bytes, {} ms)",
					targetPath.toAbsolutePath(), fileSize, totalTime);
			} else {
				log.error("FAILURE: Artifact file was NOT created at: {}",
					targetPath.toAbsolutePath());
			}
		}
	}
}