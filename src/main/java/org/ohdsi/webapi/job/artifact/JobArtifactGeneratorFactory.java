package org.ohdsi.webapi.job.artifact;

import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory for retrieving the appropriate JobArtifactGenerator for a given job
 */
@Component
public class JobArtifactGeneratorFactory {

	private final List<JobArtifactGenerator> generators;

	public JobArtifactGeneratorFactory(List<JobArtifactGenerator> generators) {
		this.generators = generators;
	}

	/**
	 * Get the appropriate generator for the given job execution
	 *
	 * @param jobExecution the job execution
	 * @return the matching generator, or null if none found
	 */
	public JobArtifactGenerator getGenerator(JobExecution jobExecution) {
		return generators.stream()
			.filter(generator -> generator.supports(jobExecution))
			.findFirst()
			.orElse(null);
	}
}