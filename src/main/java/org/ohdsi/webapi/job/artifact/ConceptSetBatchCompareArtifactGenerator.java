package org.ohdsi.webapi.job.artifact;

import org.ohdsi.webapi.job.artifact.cscompare.CsvFileGenerator;
import org.ohdsi.webapi.job.artifact.cscompare.ReadmeFileGenerator;
import org.ohdsi.webapi.job.artifact.cscompare.SummaryFileGenerator;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobEntity;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobDiffRepository;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobRepository;
import org.ohdsi.webapi.util.GenericFileWriter;
import org.ohdsi.webapi.util.TempFileUtils;
import org.ohdsi.webapi.util.archive.ArchiveStrategies;
import org.ohdsi.webapi.util.archive.ArchiveStrategy;
import org.ohdsi.webapi.util.archive.TemporaryArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Artifact generator for Concept Set Batch Compare jobs.
 * Orchestrates the generation of comparison reports in ZIP format.
 */
@Component
public class ConceptSetBatchCompareArtifactGenerator implements JobArtifactGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ConceptSetBatchCompareArtifactGenerator.class);
	private static final String JOB_NAME = "conceptSetBatchCompareJob";
	private static final String CONTENT_TYPE = "application/zip";
	private static final String DATE_FORMAT = "yyyy-MM-dd_HHmmss";
	private static final String TEMP_DIR_PREFIX = "concept-set-compare-";
	private static final String ARCHIVE_PREFIX = "cs_compare_";
	private static final String ARCHIVE_SUFFIX = ".zip";

	private final GenericFileWriter fileWriter;
	private final ArchiveStrategy archiveStrategy;
	private final ConceptSetCompareJobRepository compareJobRepository;
	private final ConceptSetCompareJobDiffRepository compareJobDiffRepository;

	// Specialized generators
	private final ReadmeFileGenerator readmeGenerator;
	private final SummaryFileGenerator summaryGenerator;
	private final CsvFileGenerator csvGenerator;

	public ConceptSetBatchCompareArtifactGenerator(
		GenericFileWriter fileWriter,
		ConceptSetCompareJobRepository compareJobRepository,
		ConceptSetCompareJobDiffRepository compareJobDiffRepository,
		ReadmeFileGenerator readmeGenerator,
		SummaryFileGenerator summaryGenerator,
		CsvFileGenerator csvGenerator) {

		this.fileWriter = fileWriter;
		this.archiveStrategy = ArchiveStrategies.zip(ARCHIVE_PREFIX, ARCHIVE_SUFFIX);
		this.compareJobRepository = compareJobRepository;
		this.compareJobDiffRepository = compareJobDiffRepository;
		this.readmeGenerator = readmeGenerator;
		this.summaryGenerator = summaryGenerator;
		this.csvGenerator = csvGenerator;
	}

	@Override
	public boolean supports(JobExecution jobExecution) {
		return jobExecution.getJobInstance() != null &&
			JOB_NAME.equals(jobExecution.getJobInstance().getJobName());
	}

	@Override
	public boolean hasArtifact(JobExecution jobExecution) {
		if (!"COMPLETED".equals(jobExecution.getStatus().toString())) {
			logger.debug("Job execution {} is not completed, no artifact available", jobExecution.getId());
			return false;
		}
		Long executionId = jobExecution.getId();
		Optional<ConceptSetCompareJobEntity> compareJobOpt = compareJobRepository.findByExecutionId(executionId);

		if (!compareJobOpt.isPresent()) {
			logger.warn("No compare job found for execution ID: {}", executionId);
			return false;
		}

		ConceptSetCompareJobEntity compareJob = compareJobOpt.get();
		logger.debug("Found compare job with ID: {}, conceptSetsAnalyzed: {}",
			compareJob.getId(), compareJob.getConceptSetsAnalyzed());

		// Check if there are any diffs (can be zero if no differences were found)
		Long diffCount = compareJobDiffRepository.countByCompareJobId(compareJob.getId());

		logger.info("Job execution {} has {} diff results in database", executionId, diffCount);
		return true;
	}

	@Override
	public Resource getArtifact(JobExecution jobExecution) throws IOException {
		logger.info("Generating artifact for job execution: {}", jobExecution.getId());
		try {
			TemporaryArchive archive = generateReport(jobExecution);
			return new FileSystemResource(archive.getArchivePath().toFile());
		} catch (Exception e) {
			logger.error("Failed to generate artifact for execution {}", jobExecution.getId(), e);
			throw new IOException("Failed to generate artifact", e);
		}
	}

	@Override
	public String getArtifactFilename(JobExecution jobExecution) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String timestamp = sdf.format(new Date());
		return String.format("concept_set_batch_compare_%d_%s.zip",
			jobExecution.getId(), timestamp);
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	/**
	 * Generate the comparison report as a ZIP file
	 */
	private TemporaryArchive generateReport(JobExecution jobExecution) {
		return TempFileUtils.doInDirectory(TEMP_DIR_PREFIX, workDir -> {
			try {
				logger.info("Creating report structure in temporary directory");

				JobParameters jobParams = jobExecution.getJobParameters();
				Long executionId = jobExecution.getId();

				Optional<ConceptSetCompareJobEntity> compareJobOpt =
					compareJobRepository.findByExecutionId(executionId);

				if (!compareJobOpt.isPresent()) {
					logger.warn("No compare job found for execution ID: {}", executionId);
					createMinimalReport(workDir, jobExecution, jobParams);
				} else {
					createFullReport(workDir, jobExecution, jobParams, compareJobOpt.get());
				}

				// Package everything into a ZIP
				logger.info("Packaging report into ZIP archive...");
				Path archivePath = archiveStrategy.apply(workDir);
				String filename = getArtifactFilename(jobExecution);

				logger.info("Successfully created artifact: {} at {}", filename, archivePath);
				return new TemporaryArchive(filename, archivePath);

			} catch (IOException e) {
				logger.error("Failed to create report structure", e);
				throw new RuntimeException("Failed to generate report", e);
			}
		});
	}

	/**
	 * Create minimal report when no compare job is found
	 */
	private void createMinimalReport(Path workDir, JobExecution jobExecution,
																	 JobParameters jobParams) throws IOException {
		logger.info("Creating minimal report (no compare job found)");
		readmeGenerator.generate(workDir, jobExecution, jobParams, null, null);
		createNoResultsFile(workDir);
	}

	/**
	 * Create full report with all comparison results
	 */
	private void createFullReport(Path workDir, JobExecution jobExecution, JobParameters jobParams,
																ConceptSetCompareJobEntity compareJob) throws IOException {
		logger.info("Creating full report for compare job ID: {}", compareJob.getId());

		List<ConceptSetCompareJobDiffEntity> allDiffs =
			compareJobDiffRepository.findByCompareJobIdOrdered(compareJob.getId());

		logger.info("Found {} diff entities for compare job", allDiffs.size());

		// Generate all report files
		readmeGenerator.generate(workDir, jobExecution, jobParams, compareJob, allDiffs);
		summaryGenerator.generate(workDir, compareJob, allDiffs);

		if (!allDiffs.isEmpty()) {
			// Generate consolidated CSVs - separate files for included codes and source codes
			csvGenerator.generateIncludedCodesConsolidatedCsv(workDir, allDiffs);
			csvGenerator.generateSourceCodesConsolidatedCsv(workDir, allDiffs);

			// Generate per-concept-set CSVs
			csvGenerator.generatePerConceptSetCsvs(workDir, allDiffs, false); // Included concepts

			// Generate source code CSVs if applicable
			if (compareJob.getCompareSourceCodes() != null && compareJob.getCompareSourceCodes()) {
				csvGenerator.generatePerConceptSetCsvs(workDir, allDiffs, true); // Source codes
			}
		} else {
			logger.info("No diffs found, creating no-differences report");
			createNoResultsFile(workDir);
		}
	}

	/**
	 * Create a file indicating no results were found
	 */
	private void createNoResultsFile(Path workDir) throws IOException {
		StringBuilder content = new StringBuilder();
		content.append("No Differences Found\n");
		content.append("====================\n\n");
		content.append("The batch comparison completed successfully but found no differences\n");
		content.append("between the concept sets in the two sources.\n");

		Path noResultsPath = workDir.resolve("no_results.txt");
		fileWriter.writeTextFile(noResultsPath, pw -> pw.print(content.toString()));
		logger.debug("Created no results file at {}", noResultsPath);
	}
}